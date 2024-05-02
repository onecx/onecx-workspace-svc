package org.tkit.onecx.workspace.domain.services;

import java.util.ArrayList;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.onecx.workspace.domain.daos.MicrofrontendDAO;
import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.quarkus.jpa.daos.PageResult;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductDAO productDAO;

    @Inject
    MicrofrontendDAO microfrontendDAO;

    public PageResult<Product> findByCriteria(ProductSearchCriteria criteria) {
        var result = productDAO.findByCriteria(criteria);
        if (result.isEmpty()) {
            return result;
        }

        var list = result.getStream().filter(ProductService::resetMicrofrontends).toList();

        var map = list.stream().collect(Collectors.toMap(Product::getId, x -> x));
        var items = microfrontendDAO.findByProductNames(map.keySet());
        items.forEach(mfe -> map.get(mfe.getProductId()).getMicrofrontends().add(mfe));

        return new PageResult<>(result.getTotalElements(), list.stream(), result.getNumber(), result.getSize());
    }

    private static boolean resetMicrofrontends(Product p) {
        p.setMicrofrontends(new ArrayList<>());
        return true;
    }
}
