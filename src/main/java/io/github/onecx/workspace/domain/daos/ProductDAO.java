package io.github.onecx.workspace.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import io.github.onecx.workspace.domain.models.MenuItem_;
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

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteProductByWorkspaceId(String workspaceId) {
        var cb = this.getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Product.class);
        var root = cq.from(Product.class);

        cq.where(cb.equal(root.get(MenuItem_.WORKSPACE).get(TraceableEntity_.ID), workspaceId));

        var products = this.getEntityManager().createQuery(cq).getResultList();
        delete(products);
    }

    @Override
    public Product findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ProductDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,

        ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID,

    }
}
