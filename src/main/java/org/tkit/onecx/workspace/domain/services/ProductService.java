package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.rs.internal.mappers.ProductMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.ProductPageResultDTO;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductDAO productDAO;

    @Inject
    ProductMapper mapper;

    @Transactional
    public ProductPageResultDTO findByCriteria(ProductSearchCriteria criteria) {
        var result = productDAO.findByCriteria(criteria);
        return mapper.mapPage(result);
    }
}
