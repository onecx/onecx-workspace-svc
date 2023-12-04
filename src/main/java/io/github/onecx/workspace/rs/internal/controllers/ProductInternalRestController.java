package io.github.onecx.workspace.rs.internal.controllers;

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.internal.ProductInternalApi;
import gen.io.github.onecx.workspace.rs.internal.model.CreateProductRequestDTO;
import gen.io.github.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.workspace.rs.internal.model.UpdateProductRequestDTO;
import io.github.onecx.workspace.domain.daos.ProductDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import io.github.onecx.workspace.rs.internal.mappers.ProductMapper;

@LogService
@ApplicationScoped
@Path("/internal/workspaces/{id}/products")
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ProductInternalRestController implements ProductInternalApi {

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Inject
    ProductDAO dao;

    @Inject
    ProductMapper mapper;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Override
    public Response createProductInWorkspace(String id, CreateProductRequestDTO createProductRequestDTO) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist",
                    ProductInternalRestController.ProductErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }
        var product = mapper.create(createProductRequestDTO);
        product.setWorkspace(workspace);
        product = dao.create(product);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(product.getId()).build())
                .entity(mapper.map(product))
                .build();
    }

    @Override
    public Response deleteProductById(String id, String productId) {
        dao.deleteProduct(productId);

        return Response.noContent().build();
    }

    @Override
    public Response getProductsForWorkspaceId(String id) {
        var result = dao.getProductsForWorkspaceId(id);
        return Response.ok(mapper.map(result)).build();
    }

    @Override
    public Response updateProductById(String id, String productId, UpdateProductRequestDTO updateProductRequestDTO) {
        var product = dao.findById(productId);
        if (product == null) {
            return Response.status(NOT_FOUND).build();
        }

        mapper.update(updateProductRequestDTO, product);

        return Response.ok(mapper.map(product)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    enum ProductErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,

    }
}
