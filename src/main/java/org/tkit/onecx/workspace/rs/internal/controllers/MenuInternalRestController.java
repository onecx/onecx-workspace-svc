package org.tkit.onecx.workspace.rs.internal.controllers;

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.MenuItemMapper;
import org.tkit.onecx.workspace.rs.internal.services.MenuItemService;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.MenuInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MenuInternalRestController implements MenuInternalApi {

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Inject
    MenuItemMapper mapper;

    @Inject
    MenuItemDAO dao;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Override
    @Transactional
    public Response createMenuItemForWorkspace(String id, CreateMenuItemDTO menuItemDTO) {
        var workspace = workspaceDAO.findById(id);

        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        MenuItem parentItem = null;
        if (menuItemDTO.getParentItemId() != null) {

            parentItem = dao.findById(menuItemDTO.getParentItemId());

            if (parentItem == null) {
                throw new ConstraintException("Parent menu item does not exist", MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST,
                        null);
            } else {
                // check if parent's portal and child's portal are the same
                if (!parentItem.getWorkspace().getId().equals(id)) {
                    throw new ConstraintException("Parent menu item and menu item does not have the same workspace",
                            MenuItemErrorKeys.WORKSPACE_DIFFERENT, null);
                }
            }
        }

        var menuItem = mapper.create(menuItemDTO);
        menuItem.setWorkspace(workspace);
        menuItem.setWorkspaceName(workspace.getName());
        menuItem.setParent(parentItem);
        menuItem = dao.create(menuItem);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(menuItem.getId()).build())
                .build();
    }

    @Override
    @Transactional
    public Response deleteAllMenuItemsForWorkspace(String id) {
        dao.deleteAllMenuItemsByWorkspaceId(id);

        return Response.noContent().build();
    }

    @Override
    @Transactional
    public Response deleteMenuItemById(String id, String menuItemId) {
        dao.deleteQueryById(menuItemId);

        return Response.noContent().build();
    }

    @Override
    public Response getMenuItemById(String id, String menuItemId) {
        var result = dao.findById(menuItemId);

        return Response.ok(mapper.map(result)).build();
    }

    @Override
    public Response getMenuItemsForWorkspaceId(String id) {
        var result = dao.loadAllMenuItemsByWorkspaceId(id);

        return Response.ok(mapper.mapList(result)).build();
    }

    @Override
    public Response getMenuStructureForWorkspaceId(String id) {
        var result = dao.loadAllMenuItemsByWorkspaceId(id);

        return Response.ok(mapper.mapTree(result)).build();
    }

    @Override
    @Transactional
    public Response patchMenuItems(String id, List<MenuItemDTO> menuItemDTO) {
        // create map of <ID, DTO>
        Map<Object, MenuItemDTO> tmp = menuItemDTO.stream()
                .collect(Collectors.toMap(MenuItemDTO::getId, x -> x));

        // load menu items
        var items = dao.findByIds(Arrays.asList(tmp.keySet().toArray())).toList();
        if (items.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }

        if (items.size() != tmp.size()) {
            return Response.status(NOT_FOUND).entity("Menu Items specified in request body do not exist in db").build();
        }

        for (MenuItem item : items) {
            MenuItemDTO dto = tmp.get(item.getId());

            // update parent
            updateParent(item, dto);

            mapper.update(dto, item);
        }

        var result = dao.update(items);
        return Response.ok(mapper.map(result)).build();
    }

    @Inject
    MenuItemService menuItemService;

    @Override
    public Response updateMenuItem(String id, String menuItemId, MenuItemDTO menuItemDTO) {
        var menuItem = menuItemService.updateMenuItem(id, menuItemId, menuItemDTO);
        if (menuItem == null) {
            return Response.status(NOT_FOUND).build();
        }

        mapper.update(menuItemDTO, menuItem.menuItem);
        if (menuItem.change && menuItem.parent != null) {
            menuItem.menuItem.setParent(menuItem.parent);
        }
        var m = dao.update(menuItem.menuItem);

        return Response.ok(mapper.map(m)).build();
    }

    @Override
    @Transactional
    public Response uploadMenuStructureForWorkspaceId(String id, WorkspaceMenuItemStructrueDTO menuItemStructrueDTO) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            throw new ConstraintException("Given workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        if (menuItemStructrueDTO.getMenuItems() == null || menuItemStructrueDTO.getMenuItems().isEmpty()) {
            throw new ConstraintException("menuItems cannot be null", MenuItemErrorKeys.MENU_ITEMS_NULL, null);
        }

        List<MenuItem> items = new LinkedList<>();
        mapper.recursiveMappingTreeStructure(menuItemStructrueDTO.getMenuItems(), workspace, null, items);

        dao.deleteAllMenuItemsByWorkspaceId(id);
        dao.create(items);

        return Response.noContent().build();

    }

    private void updateParent(MenuItem menuItem, MenuItemDTO dto) {

        if (dto.getParentItemId() == null) {
            menuItem.setParent(null);
            return;
        }

        // check parent change
        if (menuItem.getParent() != null && dto.getParentItemId().equals(menuItem.getParent().getId())) {
            return;
        }

        // checking if request parent id is the same as current id
        if (dto.getParentItemId().equals(menuItem.getId())) {
            throw new ConstraintException("Menu Item " + menuItem.getId() + " id and parentItem id are the same",
                    MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM, null);
        }

        // checking if parent exists
        var parent = dao.findById(dto.getParentItemId());
        if (parent == null) {
            throw new ConstraintException("Parent menu item " + dto.getParentItemId() + " does not exists",
                    MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST, null);
        } else {
            // checking if parent exists in the same portal
            if (!parent.getWorkspace().getId().equals(menuItem.getWorkspace().getId())) {
                throw new ConstraintException("Parent menu item is assigned to different portal",
                        MenuItemErrorKeys.PARENT_ASSIGNED_TO_DIFFERENT_PORTAL, null);
            }

            // check for cycle
            Set<String> children = new HashSet<>();
            children(menuItem, children);
            if (children.contains(parent.getId())) {
                throw new ConstraintException(
                        "One of the items try to set one of its children to the new parent. Cycle dependency can not be created in tree structure",
                        MenuItemErrorKeys.CYCLE_DEPENDENCY, null);
            }
        }

        // set new parent
        menuItem.setParent(parent);
    }

    private void children(MenuItem menuItem, Set<String> result) {
        menuItem.getChildren().forEach(c -> {
            result.add(c.getId());
            children(c, result);
        });
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

    enum MenuItemErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,
        PARENT_MENU_DOES_NOT_EXIST,

        PARENT_MENU_SAME_AS_MENU_ITEM,
        WORKSPACE_DIFFERENT,

        MENU_ITEMS_NULL,

        PARENT_ASSIGNED_TO_DIFFERENT_PORTAL,

        CYCLE_DEPENDENCY

    }
}
