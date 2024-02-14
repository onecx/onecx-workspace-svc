package org.tkit.onecx.workspace.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.rs.external.v1.mappers.ProductMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.external.v1.ProductExternalV1Api;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
class ProductsExternalV1RestController implements ProductExternalV1Api {
    @Inject
    ProductMapper mapper;

    @Inject
    ProductDAO productDAO;

    @Override
    public Response getProducts(String name) {
        var result = productDAO.getProductsForWorkspaceName(name);
        return Response.ok(mapper.map(result)).build();
    }
}
