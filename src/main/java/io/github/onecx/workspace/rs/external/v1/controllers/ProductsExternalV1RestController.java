package io.github.onecx.workspace.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.external.v1.ProductExternalV1Api;
import io.github.onecx.workspace.domain.daos.ProductDAO;
import io.github.onecx.workspace.rs.external.v1.mappers.ProductMapper;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
class ProductsExternalV1RestController implements ProductExternalV1Api {
    @Inject
    ProductMapper mapper;

    @Inject
    ProductDAO productDAO;

    @Override
    public Response getProductsForWorkspaceId(String id) {
        var result = productDAO.getProductsForWorkspaceId(id);
        return Response.ok(mapper.map(result)).build();
    }
}
