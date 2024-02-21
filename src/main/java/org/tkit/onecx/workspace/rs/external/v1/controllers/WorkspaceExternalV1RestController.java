package org.tkit.onecx.workspace.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.rs.external.v1.mappers.WorkspaceMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.external.v1.WorkspaceExternalV1Api;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class WorkspaceExternalV1RestController implements WorkspaceExternalV1Api {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    WorkspaceMapper mapper;

    @Inject
    ProductDAO productDAO;

    @Override
    public Response getWorkspaceProducts(String name) {
        var item = workspaceDAO.findByName(name);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var result = productDAO.getProductsForWorkspaceId(item.getId());
        return Response.ok(mapper.map(result)).build();
    }

    @Override
    public Response getWorkspaceByName(String name) {
        var item = workspaceDAO.findByName(name);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(item)).build();
    }

    @Override
    public Response searchWorkspaces(WorkspaceSearchCriteriaDTOV1 workspaceSearchCriteriaDTOV1) {
        var criteria = mapper.map(workspaceSearchCriteriaDTOV1);
        var result = workspaceDAO.findBySearchCriteria(criteria);
        return Response.ok(mapper.mapAbstractList(result)).build();
    }
}
