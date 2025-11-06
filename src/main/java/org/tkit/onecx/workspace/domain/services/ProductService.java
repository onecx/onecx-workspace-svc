package org.tkit.onecx.workspace.domain.services;

import java.util.ArrayList;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.onecx.workspace.domain.daos.MicrofrontendDAO;
import org.tkit.onecx.workspace.domain.daos.ProductDAO;
import org.tkit.onecx.workspace.domain.daos.SlotDAO;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.rs.internal.mappers.ProductMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.SlotMapper;
import org.tkit.quarkus.jpa.daos.PageResult;

import gen.org.tkit.onecx.workspace.rs.internal.model.ProductPageResultDTO;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductDAO productDAO;

    @Inject
    MicrofrontendDAO microfrontendDAO;

    @Inject
    SlotDAO slotDAO;

    @Inject
    ProductMapper productMapper;

    @Inject
    SlotMapper slotMapper;

    public ProductPageResultDTO findByCriteria(ProductSearchCriteria criteria) {
        var result = productDAO.findByCriteria(criteria);
        if (result.isEmpty()) {
            return new ProductPageResultDTO();
        }

        var list = result.getStream().filter(ProductService::resetMicrofrontends).toList();

        var map = list.stream().collect(Collectors.toMap(Product::getId, x -> x));
        var items = microfrontendDAO.findByProductNames(map.keySet());
        items.forEach(mfe -> map.get(mfe.getProductId()).getMicrofrontends().add(mfe));
        var pageResult = new PageResult<>(result.getTotalElements(), list.stream(), result.getNumber(), result.getSize());
        var slots = slotDAO.findSlotsByWorkspaceId(criteria.getWorkspaceId());
        var pageResultDTO = productMapper.mapPage(pageResult);
        pageResultDTO.getStream().forEach(product -> slots.forEach(slot -> {
            if (!slot.getComponents().isEmpty()
                    && slot.getComponents().get(0).getProductName().equals(product.getProductName())) {
                product.getSlots().add(slotMapper.map(slot));
            }
        }));

        return pageResultDTO;
    }

    private static boolean resetMicrofrontends(Product p) {
        p.setMicrofrontends(new ArrayList<>());
        return true;
    }
}
