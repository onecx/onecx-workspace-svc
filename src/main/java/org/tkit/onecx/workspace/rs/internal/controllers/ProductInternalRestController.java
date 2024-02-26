package org.tkit.onecx.workspace.rs.internal.controllers;

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;

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
import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.ProductMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.ProductInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateProductRequestDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProductSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateProductRequestDTO;

@LogService
@ApplicationScoped
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
    public Response createProduct(CreateProductRequestDTO createProductRequestDTO) {
        var workspace = workspaceDAO.findById(createProductRequestDTO.getWorkspaceId());
        if (workspace == null) {
            throw new ConstraintException("Product does not exist",
                    ProductInternalRestController.ProductErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }
        var product = mapper.create(createProductRequestDTO, workspace);
        product = dao.create(product);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(product.getId()).build())
                .entity(mapper.map(product))
                .build();
    }

    @Override
    public Response deleteProductById(String productId) {
        dao.deleteProduct(productId);
        return Response.noContent().build();
    }

    @Override
    public Response getProductById(String productId) {
        var product = dao.findById(productId);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(product)).build();
    }

    @Override
    public Response searchProducts(ProductSearchCriteriaDTO productSearchCriteriaDTO) {
        var criteria = mapper.map(productSearchCriteriaDTO);
        var result1 = dao.findByCriteria(criteria);
        var items = result1.getStream().toList();
        var result = dao.findByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
    }

    @Override
    public Response updateProductById(String productId, UpdateProductRequestDTO updateProductRequestDTO) {
        var product = dao.loadById(productId);
        if (product == null) {
            return Response.status(NOT_FOUND).build();
        }
        mapper.update(updateProductRequestDTO, product);
        product = dao.update(product);
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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

    enum ProductErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,

    }
}
