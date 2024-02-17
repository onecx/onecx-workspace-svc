package org.tkit.onecx.workspace.rs.legacy.controllers;

import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.rs.legacy.mappers.TkitPortalMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.legacy.TkitPortalApi;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationResponseDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class TkitPortalRestController implements TkitPortalApi {

    @ConfigProperty(name = "tkit.legacy.enable.menu.auto.registration", defaultValue = "false")
    boolean enableAutoRegistration;

    @Inject
    TkitLegacyAppConfig appConfig;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    TkitPortalMapper mapper;

    @Override
    public Response getMenuStructureForTkitPortalName(String portalName, Boolean interpolate) {
        var workspace = workspaceDAO.findByWorkspaceName(portalName);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var menuItems = menuItemDAO.loadAllMenuItemsByWorkspace(workspace.getId());

        if (interpolate != null && interpolate) {
            for (MenuItem item : menuItems) {
                if (StringUtils.isNotBlank(item.getUrl())) {
                    item.setUrl(StringSubstitutor.replace(item.getUrl(), System.getenv()));
                }
            }
        }

        return Response.ok(mapper.mapToTree(menuItems, workspace.getName())).build();
    }

    @Override
    @Transactional
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
            var workspace = workspaceDAO.findByWorkspaceName(portalName);
            if (workspace == null) {
                throw new ConstraintException("Workspace not found", ErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
            }

            if (menuRegistrationRequestDTO.getMenuItems() == null || menuRegistrationRequestDTO.getMenuItems().isEmpty()) {
                throw new ConstraintException("Menu items are empty", ErrorKeys.MENU_ITEMS_EMPTY, null);
            }

            // In the old structure just a sub part of the menu was sent, so we need to find the parent menu item if defined
            var parentKey = menuRegistrationRequestDTO.getMenuItems().get(0).getParentKey();
            MenuItem parent = null;
            if (parentKey != null) {
                parent = menuItemDAO.loadMenuItemByWorkspaceAndKey(portalName, parentKey);
            }

            List<MenuItem> items = new LinkedList<>();
            mapper.recursiveMappingTreeStructure(menuRegistrationRequestDTO.getMenuItems(), workspace, parent, appId, items);

            menuItemDAO.deleteAllMenuItemsByWorkspaceAndAppId(workspace.getId(), appId);
            menuItemDAO.create(items);

            response.setApplied(true);
        } catch (Exception ex) {
            log.error("Failed to process menu registration request", ex);
            response.setApplied(false);
            response.setNotice("TKPORTAL100002 Menu registration request failed due to server error");
        }
        return Response.ok(response).build();
    }

    @ServerExceptionMapper
    public RestResponse<RestExceptionDTO> exception(Exception ex) {
        log.error("Processing tkit portal rest controller error: {}", ex.getMessage());

        if (ex instanceof DAOException de) {
            return RestResponse.status(Response.Status.BAD_REQUEST,
                    mapper.exception(de.getMessageKey().name(), ex.getMessage(), de.parameters));
        }
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR,
                mapper.exception("UNDEFINED_ERROR_CODE", ex.getMessage()));

    }

    enum ErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,
        MENU_ITEMS_EMPTY,

    }
}
