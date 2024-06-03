package org.tkit.onecx.workspace.rs.exim.v1.controllers;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.daos.*;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.onecx.workspace.domain.services.MenuService;
import org.tkit.onecx.workspace.domain.services.WorkspaceService;
import org.tkit.onecx.workspace.rs.exim.v1.mappers.ExportImportExceptionMapperV1;
import org.tkit.onecx.workspace.rs.exim.v1.mappers.ExportImportMapperV1;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.jpa.models.TraceableEntity;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.exim.v1.WorkspaceExportImportApi;
import gen.org.tkit.onecx.workspace.rs.exim.v1.model.*;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
class ExportImportRestControllerV1 implements WorkspaceExportImportApi {

    @Inject
    WorkspaceDAO dao;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    ExportImportExceptionMapperV1 exceptionMapper;

    @Inject
    ExportImportMapperV1 mapper;

    @Inject
    MenuService menuService;

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    ImageDAO imageDAO;

    @Inject
    WorkspaceService service;

    @Override
    public Response exportMenuByWorkspaceName(String name) {
        var workspace = dao.findByName(name);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var criteria = new MenuItemLoadCriteria();
        criteria.setWorkspaceId(workspace.getId());
        var menu = menuItemDAO.loadAllMenuItemsByCriteria(criteria);

        var ma = assignmentDAO.findAssignmentMenuForWorkspace(workspace.getId());
        Map<String, Set<String>> roles = ma.stream()
                .collect(groupingBy(AssignmentMenu::menuItemId, mapping(AssignmentMenu::roleName, toSet())));

        return Response.ok(mapper.mapTree(menu, roles)).build();
    }

    @Override
    public Response exportWorkspacesByNames(ExportWorkspacesRequestDTOV1 request) {

        var criteria = new WorkspaceSearchCriteria();
        criteria.setNames(request.getNames());
        var workspaces = dao.findBySearchCriteria(criteria);

        var data = workspaces.getStream().collect(toMap(Workspace::getName, workspace -> workspace));

        if (data.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var images = imageDAO.findByRefIds(data.keySet());

        var ids = data.values().stream().map(TraceableEntity::getId).collect(toSet());
        var menus = menuItemDAO.loadAllMenuItemsByWorkspaces(ids);

        var ma = assignmentDAO.findAssignmentMenuForWorkspaces(ids);
        Map<String, Set<String>> roles = ma.stream()
                .collect(groupingBy(AssignmentMenu::menuItemId, mapping(AssignmentMenu::roleName, toSet())));

        return Response.ok(mapper.create(data, images, menus, roles)).build();
    }

    @Override
    public Response importMenu(String name, MenuSnapshotDTOV1 menuSnapshotDTOV1) {
        var workspace = dao.findByName(name);
        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        var status = ImportResponseStatusDTOV1.SKIPPED;

        if (!menuSnapshotDTOV1.getMenu().getMenuItems().isEmpty()) {

            // convert menu dto to menu item object
            List<MenuItem> items = new LinkedList<>();
            Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(
                    menuSnapshotDTOV1.getMenu().getMenuItems(),
                    workspace, null, items);

            // validate menu items, roles and assignments
            var request = menuService.importMenuItems(workspace, items, menuRoles);

            // execute update in database
            var deleted = menuService.importMenuItemsForWorkspace(request);

            // create or update only?
            status = deleted == 0 ? ImportResponseStatusDTOV1.CREATED : ImportResponseStatusDTOV1.UPDATED;
        }

        return Response.ok(mapper.create(menuSnapshotDTOV1.getId(), status)).build();
    }

    @Override
    public Response importOperatorWorkspaces(WorkspaceSnapshotDTOV1 workspaceSnapshotDTOV1) {

        if (workspaceSnapshotDTOV1.getWorkspaces() == null) {
            return Response.ok().build();
        }

        List<Workspace> workspaces = new ArrayList<>();
        List<Image> images = new ArrayList<>();
        List<Slot> slots = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        List<MenuItem> menuItems = new ArrayList<>();
        List<Assignment> assignments = new ArrayList<>();

        workspaceSnapshotDTOV1.getWorkspaces().forEach((name, dto) -> {
            var workspace = mapper.create(dto);
            workspace.setName(name);
            workspace.setOperator(true);
            workspaces.add(workspace);

            var items = mapper.createImages(name, dto.getImages());
            items.forEach(x -> x.setOperator(true));
            images.addAll(items);
            if (!dto.getProducts().isEmpty()) {
                products.addAll(mapper.create(dto.getProducts(), workspace));
            }
            if (!dto.getSlots().isEmpty()) {
                slots.addAll(mapper.createSlots(dto.getSlots(), workspace));
            }

            // create workspace menu items
            if (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty()) {
                List<MenuItem> mis = new ArrayList<>();
                Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(dto.getMenuItems(), workspace, null,
                        mis);

                // create assignments of menus and roles
                assignments.addAll(mapper.createAssignments(workspace.getRoles(), mis, menuRoles));
                menuItems.addAll(mis);
            }
        });

        // delete and create data
        service.importOperator(workspaces, images, slots, products, menuItems, assignments);

        return Response.ok().build();
    }

    @Override
    public Response importWorkspaces(WorkspaceSnapshotDTOV1 request) {
        var workspaceNames = request.getWorkspaces().keySet();

        var criteria = new WorkspaceSearchCriteria();
        criteria.setNames(workspaceNames);
        var workspaces = dao.findBySearchCriteria(criteria);
        var map = workspaces.getStream().collect(Collectors.toMap(Workspace::getName, workspace -> workspace));

        Map<String, ImportResponseStatusDTOV1> items = new HashMap<>();
        List<Workspace> createWorkspaces = new ArrayList<>();
        List<Slot> createSlots = new ArrayList<>();
        List<Product> createProducts = new ArrayList<>();
        List<Image> createImages = new ArrayList<>();
        List<MenuItem> menuItems = new ArrayList<>();
        List<Assignment> assignments = new ArrayList<>();

        request.getWorkspaces().forEach((name, dto) -> {
            try {
                var workspace = map.get(name);

                if (workspace == null) {
                    workspace = mapper.create(dto);
                    workspace.setName(name);
                    createWorkspaces.add(workspace);
                    createImages.addAll(mapper.createImages(name, dto.getImages()));
                    if (!dto.getProducts().isEmpty()) {
                        var products = mapper.create(dto.getProducts(), workspace);
                        createProducts.addAll(products);
                    }
                    if (!dto.getSlots().isEmpty()) {
                        var slots = mapper.createSlots(dto.getSlots(), workspace);
                        createSlots.addAll(slots);
                    }

                    // create workspace menu items
                    if (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty()) {
                        List<MenuItem> mis = new ArrayList<>();
                        Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(dto.getMenuItems(), workspace,
                                null,
                                mis);

                        // create assignments of menus and roles
                        assignments.addAll(mapper.createAssignments(workspace.getRoles(), mis, menuRoles));
                        menuItems.addAll(mis);
                    }
                    items.put(name, ImportResponseStatusDTOV1.CREATED);
                } else {
                    items.put(name, ImportResponseStatusDTOV1.SKIPPED);
                }
            } catch (Exception ex) {
                items.put(name, ImportResponseStatusDTOV1.ERROR);
            }
        });

        service.importWorkspace(createWorkspaces, createImages, createSlots, createProducts, menuItems, assignments);

        return Response.ok(mapper.create(request, items)).build();
    }

    @ServerExceptionMapper
    public RestResponse<EximProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<EximProblemDetailResponseDTOV1> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    enum MenuItemErrorKeys {
        WORKSPACE_DOES_NOT_EXIST
    }
}
