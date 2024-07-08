package org.tkit.onecx.workspace.rs.legacy.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.rs.legacy.mappers.PortalExceptionMapper;
import org.tkit.onecx.workspace.rs.legacy.mappers.PortalMenuItemMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.legacy.V2MenuStructureApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class PortalV2RestController implements V2MenuStructureApi {

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    PortalMenuItemMapper mapper;

    @Inject
    PortalExceptionMapper exceptionMapper;

    @Override
    public Response getMenuStructureV2ForPortalId(String portalName, Boolean interpolate) {
        var workspace = workspaceDAO.findByName(portalName);
        if (workspace == null) {
            return Response.ok(mapper.mapToEmptyTree()).build();
        }

        var criteria = new MenuItemLoadCriteria();
        criteria.setWorkspaceId(workspace.getId());
        var menuItems = menuItemDAO.loadAllMenuItemsByCriteria(criteria);

        if (interpolate != null && interpolate) {
            for (MenuItem item : menuItems) {
                if (StringUtils.isNotBlank(item.getUrl())) {
                    item.setUrl(StringSubstitutor.replace(item.getUrl(), System.getenv()));
                }
            }
        }

        return Response.ok(mapper.mapToTree(menuItems, workspace.getName())).build();
    }

    @ServerExceptionMapper
    public Response exception(Exception ex) {
        log.error("Processing tkit portal rest controller error: {}", ex.getMessage());
        return exceptionMapper.create(ex);
    }

    enum ErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,
        MENU_ITEMS_EMPTY,

    }
}
