package io.github.onecx.workspace.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.github.onecx.workspace.domain.models.Product;
import io.github.onecx.workspace.domain.models.Product_;
import io.github.onecx.workspace.domain.models.Workspace_;

@ApplicationScoped
public class ProductDAO extends AbstractDAO<Product> {

    public List<Product> getProductsForWorkspaceId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);

            cq.where(cb.equal(root.get(Product_.WORKSPACE).get(Workspace_.ID), id));
            return this.getEntityManager().createQuery(cq).getResultList();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ProductDAO.ErrorKeys.ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_FIND_PRODUCTS_BY_WORKSPACE_ID,

    }
}
