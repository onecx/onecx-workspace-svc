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
import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.MenuItemMapper;
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
    AssignmentDAO assignmentDAO;

    @Override
    public Response createMenuItem(CreateMenuItemDTO menuItemDTO) {
        var workspace = workspaceDAO.findById(menuItemDTO.getWorkspaceId());

        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        MenuItem parentItem = null;
        if (menuItemDTO.getParentItemId() != null) {
            parentItem = dao.findById(menuItemDTO.getParentItemId());
            if (parentItem == null) {
                throw new ConstraintException("Parent menu item does not exist", MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST,
                        null);
            }

            // check if parent's portal and child's portal are the same
            if (!workspace.getId().equals(parentItem.getWorkspaceId())) {
                throw new ConstraintException("Parent menu item and menu item does not have the same workspace",
                        MenuItemErrorKeys.WORKSPACE_DIFFERENT, null);
            }
        }

        var menuItem = mapper.create(menuItemDTO, workspace, parentItem);
        menuItem = dao.create(menuItem);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(menuItem.getId()).build()).entity(mapper.map(menuItem))
                .build();
    }

    @Override
    @Transactional
    public Response deleteAllMenuItemsForWorkspace(String id) {
        assignmentDAO.deleteAllByWorkspaceId(id);
        dao.deleteAllMenuItemsByWorkspaceId(id);
        return Response.noContent().build();
    }

    @Override
    @Transactional
    public Response deleteMenuItemById(String menuItemId) {
        assignmentDAO.deleteAllByMenuId(menuItemId);
        dao.deleteQueryById(menuItemId);
        return Response.noContent().build();
    }

    @Override
    public Response getMenuItemById(String menuItemId) {
        var result = dao.findById(menuItemId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(result)).build();
    }

    @Override
    public Response getMenuStructure(MenuStructureSearchCriteriaDTO menuStructureSearchCriteriaDTO) {
        var criteria = mapper.map(menuStructureSearchCriteriaDTO);
        var result = dao.loadAllMenuItemsByCriteria(criteria);
        return Response.ok(mapper.mapTree(result)).build();
    }

    @Override
    public Response searchMenuItemsByCriteria(MenuItemSearchCriteriaDTO menuItemSearchCriteriaDTO) {
        var criteria = mapper.map(menuItemSearchCriteriaDTO);
        var result = dao.findByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
    }

    @Override
    public Response updateMenuItem(String menuItemId, UpdateMenuItemRequestDTO menuItemDTO) {

        var menuItem = dao.loadAllChildren(menuItemId);
        if (menuItem == null) {
            return Response.status(NOT_FOUND).build();
        }

        var oldPosition = menuItem.getPosition();
        var oldParentId = menuItem.getParentId();
        var newParentId = menuItemDTO.getParentItemId();
        var newPosition = menuItemDTO.getPosition();

        mapper.update(menuItemDTO, menuItem);
        var changeParent = updateMenuItemParent(menuItem, menuItemDTO.getParentItemId());
        menuItem = dao.updateMenuItem(menuItem, oldParentId, oldPosition, newParentId, newPosition, changeParent);

        return Response.ok(mapper.map(menuItem)).build();
    }

    private boolean updateMenuItemParent(MenuItem menuItem, String parentItemId) {
        if (parentItemId == null) {
            boolean result = menuItem.getParentId() != null;
            menuItem.setParent(null);
            return result;
        }
        // checking if request parent id is the same as current id
        if (menuItem.getId().equals(parentItemId)) {
            throw new ConstraintException("Menu Item " + menuItem.getId() + " id and parentItem id are the same",
                    MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM, null);
        }

        if (parentItemId.equals(menuItem.getParentId())) {
            return false;
        }

        // load and check parent
        var parentItem = dao.findById(parentItemId);
        if (parentItem == null) {
            throw new ConstraintException("Parent menu item does not exist",
                    MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST,
                    null);
        }

        // checking if parent menu item is in correct workspace
        if (!parentItem.getWorkspaceId().equals(menuItem.getWorkspaceId())) {
            throw new ConstraintException("Menu item does have different workspace",
                    MenuItemErrorKeys.WORKSPACE_DIFFERENT, null);
        }

        // check for cycle
        var children = children(menuItem, new HashSet<>());
        if (children.contains(parentItem.getId())) {
            throw new ConstraintException(
                    "One of the items try to set one of its children to the new parent. Cycle dependency can not be created in tree structure",
                    MenuItemErrorKeys.CYCLE_DEPENDENCY, null);
        }

        menuItem.setParent(parentItem);
        return true;
    }

    @Override
    public Response updateMenuItemParent(String menuItemId,
            UpdateMenuItemParentRequestDTO updateMenuItemParentRequestDTO) {

        var menuItem = dao.loadAllChildren(menuItemId);
        if (menuItem == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var oldPosition = menuItem.getPosition();
        var oldParentId = menuItem.getParentId();
        var newParentId = updateMenuItemParentRequestDTO.getParentItemId();
        var newPosition = updateMenuItemParentRequestDTO.getPosition();

        menuItem = mapper.update(menuItem, updateMenuItemParentRequestDTO);
        var changeParent = updateMenuItemParent(menuItem, updateMenuItemParentRequestDTO.getParentItemId());
        menuItem = dao.updateMenuItem(menuItem, oldParentId, oldPosition, newParentId, newPosition, changeParent);

        return Response.ok(mapper.map(menuItem)).build();
    }

    private Set<String> children(MenuItem menuItem, Set<String> result) {
        menuItem.getChildren().forEach(c -> {
            result.add(c.getId());
            children(c, result);
        });
        return result;
    }

    @Override
    public Response uploadMenuStructure(MenuItemStructureDTO menuItemStructureDTO) {
        var workspace = workspaceDAO.findById(menuItemStructureDTO.getWorkspaceId());
        if (workspace == null) {
            throw new ConstraintException("Given workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }

        List<MenuItem> items = new LinkedList<>();
        mapper.recursiveMappingTreeStructure(menuItemStructureDTO.getMenuItems(), workspace, null, items);

        dao.deleteAllMenuItemsByWorkspaceId(menuItemStructureDTO.getWorkspaceId());
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

        PARENT_MENU_SAME_AS_MENU_ITEM,
        CYCLE_DEPENDENCY,
        WORKSPACE_DOES_NOT_EXIST,
        PARENT_MENU_DOES_NOT_EXIST,

        WORKSPACE_DIFFERENT

    }
}
