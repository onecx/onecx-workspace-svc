package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class ProductDAO extends AbstractDAO<Product> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
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
            throw handleConstraint(e, ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public PageResult<Product> findByCriteria(ProductSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Product.class);
            var root = cq.from(Product.class);

            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getWorkspaceId() != null) {
                predicates.add(cb.equal(root.get(Product_.workspaceId), criteria.getWorkspaceId()));
            }
            addSearchStringPredicate(predicates, cb, root.get(Product_.PRODUCT_NAME), criteria.getProductName());

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[] {}));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_PRODUCTS_BY_CRITERIA, ex);
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

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,

        LOAD_ENTITY_BY_ID_FAILED,

        ERROR_DELETE_PRODUCT_ID,

        ERROR_FIND_PRODUCTS_BY_CRITERIA,

    }
}
