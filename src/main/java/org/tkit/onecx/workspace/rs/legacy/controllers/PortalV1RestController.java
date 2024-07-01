package org.tkit.onecx.workspace.rs.legacy.controllers;

import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.rs.legacy.mappers.PortalExceptionMapper;
import org.tkit.onecx.workspace.rs.legacy.mappers.PortalMenuItemMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.legacy.V1MenuStructureApi;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class PortalV1RestController implements V1MenuStructureApi {

    @Inject
    MenuItemDAO dao;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    PortalMenuItemMapper mapper;

    @Inject
    PortalConfig appConfig;

    @Inject
    PortalExceptionMapper exceptionMapper;

    @Override
    public Response getMenuStructureForPortalIdAndApplicationId(String applicationId, String portalId) {
        return getMenuStructureForPortalId(portalId);
    }

    @Override
    public Response getMenuStructureForPortalId(String portalName) {
        var workspace = workspaceDAO.findByName(portalName);
        if (workspace == null) {
            return Response.ok(mapper.mapToEmptyTree()).build();
        }

        var criteria = new MenuItemLoadCriteria();
        criteria.setWorkspaceId(workspace.getId());
        var items = dao.loadAllMenuItemsByCriteria(criteria);
        return Response.ok(mapper.mapToTree(items, workspace.getName())).build();
    }

    @Override
    public Response submitMenuRegistrationRequest(String portalName, String appId,
            MenuRegistrationRequestDTO menuRegistrationRequestDTO) {
        MenuRegistrationResponseDTO response = new MenuRegistrationResponseDTO();
        response.setApplicationId(appId);
        response.setRequestVersion(menuRegistrationRequestDTO.getRequestVersion());
        if (!appConfig.enableMenuAutoRegistration()) {
            log.info("Auto registration of menu requests is disabled, ignoring request from {}", appId);
            response.setApplied(false);
            response.setNotice("TKITPORTAL10003 Menu registration request has been ignored");
            return Response.ok(response).build();
        }

        try {
            if (menuRegistrationRequestDTO.getMenuItems().isEmpty()) {
                throw new ConstraintException("Menu items are empty", PortalV2RestController.ErrorKeys.MENU_ITEMS_EMPTY, null);
            }

            var workspace = workspaceDAO.findByName(portalName);
            if (workspace == null) {
                throw new ConstraintException("Workspace not found", PortalV2RestController.ErrorKeys.WORKSPACE_DOES_NOT_EXIST,
                        null);
            }

            // In the old structure just a sub part of the menu was sent, so we need to find the parent menu item if defined
            var parentKey = menuRegistrationRequestDTO.getMenuItems().get(0).getParentKey();
            MenuItem parent = null;
            if (parentKey != null) {
                parent = dao.loadMenuItemByWorkspaceAndKey(portalName, parentKey);
            }

            List<MenuItem> items = new LinkedList<>();
            mapper.recursiveMappingTreeStructure(menuRegistrationRequestDTO.getMenuItems(), workspace, parent, appId, items);

            dao.deleteAllMenuItemsByWorkspaceAndAppId(workspace.getId(), appId);
            dao.create(items);

            response.setApplied(true);
        } catch (Exception ex) {
            log.error("Failed to process menu registration request", ex);
            response.setApplied(false);
            response.setNotice("TKPORTAL100002 Menu registration request failed due to server error");
        }
        return Response.ok(response).build();
    }

    @ServerExceptionMapper
    public Response exception(Exception ex) {
        log.error("Processing portal legacy rest controller error: {}", ex.getMessage());
        return exceptionMapper.create(ex);
    }
}
