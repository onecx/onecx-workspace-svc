package org.tkit.onecx.workspace.domain.daos;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.models.MenuItem_;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.domain.models.Product_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

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
            throw this.handleConstraint(ex, ProductDAO.ErrorKeys.ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteProduct(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);

            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            var product = this.getEntityManager().createQuery(cq).getSingleResult();
            this.getEntityManager().remove(product);
        } catch (NoResultException nre) {
            // do nothing on No result
        } catch (Exception ex) {
            throw this.handleConstraint(ex, ProductDAO.ErrorKeys.ERROR_DELETE_PRODUCT_ID);
        }
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

    public Product loadById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);
            root.fetch(Product_.WORKSPACE);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw this.handleConstraint(e, ProductDAO.ErrorKeys.LOAD_ENTITY_BY_ID_FAILED);
        }
    }

    public Stream<Product> findByName(String name) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);
            cq.where(cb.equal(root.get(Product_.PRODUCT_NAME), name));
            return this.getEntityManager().createQuery(cq).getResultStream();
        } catch (Exception e) {
            throw this.handleConstraint(e, ProductDAO.ErrorKeys.FIND_ENTITY_BY_NAME_FAILED);
        }
    }

    public enum ErrorKeys {

        LOAD_ENTITY_BY_ID_FAILED,

        ERROR_DELETE_PRODUCT_ID,

        ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID,
        FIND_ENTITY_BY_NAME_FAILED,

    }
}
