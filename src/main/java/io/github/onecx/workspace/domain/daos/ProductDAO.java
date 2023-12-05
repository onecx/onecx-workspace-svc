package io.github.onecx.workspace.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import io.github.onecx.workspace.domain.models.Product;
import io.github.onecx.workspace.domain.models.Product_;

@ApplicationScoped
public class ProductDAO extends AbstractDAO<Product> {

    public List<Product> getProductsForWorkspaceId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);

            cq.where(cb.equal(root.get(Product_.WORKSPACE).get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ProductDAO.ErrorKeys.ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID, ex);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteProduct(String id) {
        var cb = this.getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Product.class);
        var root = cq.from(Product.class);

        cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
        var product = this.getEntityManager().createQuery(cq).getSingleResult();
        this.getEntityManager().remove(product);
    }

    public enum ErrorKeys {

        ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID,

    }
}
