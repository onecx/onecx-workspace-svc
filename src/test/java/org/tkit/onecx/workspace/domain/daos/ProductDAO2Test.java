package org.tkit.onecx.workspace.domain.daos;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ProductDAO2Test {

    @Inject
    ProductDAO dao;

    @Test
    void methodExceptionTests() {

        var criteria = new ProductSearchCriteria();
        criteria.setWorkspaceId("11-111");
        var tmp = dao.findByCriteria(criteria);

        System.out.println("### " + tmp.getTotalElements());
        var list = tmp.getStream().toList();
        System.out.println("### " + list.size());
    }

}
