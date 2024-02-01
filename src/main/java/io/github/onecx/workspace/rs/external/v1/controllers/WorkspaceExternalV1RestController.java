package io.github.onecx.workspace.rs.external.v1.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.external.v1.WorkspaceExternalV1Api;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.github.onecx.workspace.domain.daos.ProductDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.rs.external.v1.mappers.WorkspaceMapper;

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
    public Response getAllWorkspaceNames() {
        List<String> allWorkspaceNames = workspaceDAO.getAllWorkspaceNames();
        return Response.ok(allWorkspaceNames).build();
    }

    @Override
    public Response getWorkspaceByName(String name) {
        var item = workspaceDAO.findByWorkspaceName(name);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(item)).build();
    }

    @Override
    @Transactional
    public Response getWorkspacesByProductName(String productName) {
        var products = productDAO.findByName(productName);
        List<String> workspaceNames = new ArrayList<>();
        products.forEach(product -> {
            workspaceNames.add(product.getWorkspace().getName());
        });
        return Response.ok(workspaceNames).build();
    }

    @Override
    public Response searchWorkspaces(WorkspaceSearchCriteriaDTOV1 workspaceSearchCriteriaDTOV1) {
        var criteria = mapper.map(workspaceSearchCriteriaDTOV1);
        var result = workspaceDAO.findBySearchCriteria(criteria);
        return Response.ok(mapper.mapAbstractList(result)).build();
    }
}
