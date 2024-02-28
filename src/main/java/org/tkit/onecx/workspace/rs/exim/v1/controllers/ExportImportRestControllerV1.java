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
import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.RoleDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.onecx.workspace.rs.exim.v1.mappers.ExportImportExceptionMapperV1;
import org.tkit.onecx.workspace.rs.exim.v1.mappers.ExportImportMapperV1;
import org.tkit.onecx.workspace.rs.exim.v1.services.MenuService;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
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
    RoleDAO roleDAO;

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

        var data = workspaces.getStream().collect(Collectors.toMap(Workspace::getName, workspace -> workspace));

        if (data.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.create(data)).build();
    }

    @Override
    @Transactional
    public Response importMenu(String name, MenuSnapshotDTOV1 menuSnapshotDTOV1) {
        var workspace = dao.findByName(name);
        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        List<MenuItem> items = new LinkedList<>();
        Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(menuSnapshotDTOV1.getMenu().getMenuItems(),
                workspace, null, items);
        var roleNames = menuRoles.values().stream().flatMap(Collection::stream).collect(toSet());

        Map<String, Role> tmp = new HashMap<>();
        List<Role> roles = new ArrayList<>();
        if (!roleNames.isEmpty()) {

            // check for existing roles
            var existingRoleNames = roleDAO.findRolesByWorkspaceAndNames(workspace.getId(), roleNames);
            if (!existingRoleNames.isEmpty()) {
                tmp = existingRoleNames.stream().collect(toMap(Role::getName, role -> role));
                roleNames.removeAll(tmp.keySet());
            }

            // create new roles
            for (String rn : roleNames) {
                Role r = new Role();
                r.setWorkspace(workspace);
                r.setName(rn);
                tmp.put(rn, r);
                roles.add(r);
            }
        }

        List<Assignment> assignments = new ArrayList<>();
        for (MenuItem menuItem : items) {
            var mr = menuRoles.get(menuItem.getId());
            if (mr != null) {

                for (String roleName : mr) {
                    var role = tmp.get(roleName);

                    var assignment = new Assignment();
                    assignment.setMenuItem(menuItem);
                    assignment.setRole(role);
                    assignments.add(assignment);
                }
            }
        }

        var deleted = menuService.importMenuItems(workspace.getId(), items, roles, assignments);

        var status = deleted == 0 ? ImportResponseStatusDTOV1.CREATED : ImportResponseStatusDTOV1.UPDATED;

        return Response.ok(mapper.create(menuSnapshotDTOV1.getId(), status)).build();
    }

    @Override
    @Transactional
    public Response importWorkspaces(WorkspaceSnapshotDTOV1 request) {
        var workspaceNames = request.getWorkspaces().keySet();

        var criteria = new WorkspaceSearchCriteria();
        criteria.setNames(workspaceNames);
        var workspaces = dao.findBySearchCriteria(criteria);

        var map = workspaces.getStream().collect(Collectors.toMap(Workspace::getName, workspace -> workspace));

        Map<String, ImportResponseStatusDTOV1> items = new HashMap<>();

        request.getWorkspaces().forEach((name, dto) -> {
            try {
                var workspace = map.get(name);
                if (workspace == null) {
                    workspace = mapper.create(dto);
                    workspace.setName(name);
                    dao.create(workspace);
                    items.put(name, ImportResponseStatusDTOV1.CREATED);
                } else {
                    items.put(name, ImportResponseStatusDTOV1.SKIPPED);
                }
            } catch (Exception ex) {
                items.put(name, ImportResponseStatusDTOV1.ERROR);
            }
        });

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
