package org.tkit.onecx.workspace.rs.legacy.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.rs.legacy.mappers.PortalLegacyMapper;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.legacy.PortalLegacyApi;
import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class PortalLegacyRestController implements PortalLegacyApi {

    @Inject
    MenuItemDAO dao;

    @Inject
    PortalLegacyMapper mapper;

    @Override
    public Response getMenuStructureForPortalIdAndApplicationId(String applicationId, String portalId) {
        return getMenuStructureForPortalName(portalId);
    }

    @Override
    public Response getMenuStructureForPortalName(String portalName) {
        var items = dao.loadAllMenuItemsByWorkspaceName(portalName);
        return Response.ok(mapper.mapToTree(items)).build();
    }

    @ServerExceptionMapper
    public RestResponse<RestExceptionDTO> exception(Exception ex) {
        log.error("Processing portal legacy rest controller error: {}", ex.getMessage());

        if (ex instanceof DAOException de) {
            return RestResponse.status(Response.Status.BAD_REQUEST,
                    mapper.exception(de.getMessageKey().name(), ex.getMessage(), de.parameters));
        }
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR,
                mapper.exception("UNDEFINED_ERROR_CODE", ex.getMessage()));

    }
}
