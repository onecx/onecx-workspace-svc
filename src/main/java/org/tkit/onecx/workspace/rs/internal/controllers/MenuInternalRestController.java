package org.tkit.onecx.workspace.rs.internal.controllers;

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;

import java.util.*;

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

    @Inject
    MenuItemService menuItemService;

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
    public Response patchMenuItems(String id, UpdateMenuItemsRequestDTO updateMenuItemsRequestDTO) {
        // list of IDs
        List<Object> ids = new ArrayList<>(updateMenuItemsRequestDTO.getItems().keySet());
        if (ids.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }
        // load menu items
        var items = dao.findByIds(ids).toList();
        if (items.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }

        if (items.size() != ids.size()) {
            return Response.status(NOT_FOUND).entity("Menu Items specified in request body do not exist in db").build();
        }

        List<MenuItem> toUpdate = new ArrayList<>();
        for (Map.Entry<String, UpdateMenuItemRequestDTO> entry : updateMenuItemsRequestDTO.getItems().entrySet()) {
            var menuItem = update(entry.getKey(), entry.getValue());
            toUpdate.add(menuItem);
        }

        var result = dao.update(toUpdate);
        return Response.ok(mapper.map(result)).build();
    }

    @Override
    public Response updateMenuItem(String id, String menuItemId, UpdateMenuItemRequestDTO menuItemDTO) {
        var menuItem = update(menuItemId, menuItemDTO);
        if (menuItem == null) {
            return Response.status(NOT_FOUND).build();
        }
        menuItem = dao.update(menuItem);
        return Response.ok(mapper.map(menuItem)).build();
    }

    private MenuItem update(String menuItemId, UpdateMenuItemRequestDTO menuItemDTO) {
        var result = menuItemService.updateMenuItem(menuItemId, menuItemDTO.getParentItemId());
        if (result == null) {
            return null;
        }
        var menuItem = result.menuItem;
        mapper.update(menuItemDTO, menuItem);
        if (result.parentChange) {
            menuItem.setParent(result.parent);
        }
        return menuItem;
    }

    @Override
    @Transactional
    public Response uploadMenuStructureForWorkspaceId(String id, WorkspaceMenuItemStructureDTO menuItemStructureDTO) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            throw new ConstraintException("Given workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        if (menuItemStructureDTO.getMenuItems() == null || menuItemStructureDTO.getMenuItems().isEmpty()) {
            throw new ConstraintException("menuItems cannot be null", MenuItemErrorKeys.MENU_ITEMS_NULL, null);
        }

        List<MenuItem> items = new LinkedList<>();
        mapper.recursiveMappingTreeStructure(menuItemStructureDTO.getMenuItems(), workspace, null, items);

        dao.deleteAllMenuItemsByWorkspaceId(id);
        dao.create(items);

        return Response.noContent().build();

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

        WORKSPACE_DIFFERENT,

        MENU_ITEMS_NULL,

    }
}
