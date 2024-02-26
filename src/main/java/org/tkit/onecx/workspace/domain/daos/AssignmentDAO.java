package org.tkit.onecx.workspace.domain.daos;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.AssignmentSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Assignment;
import org.tkit.onecx.workspace.domain.models.Assignment_;
import org.tkit.onecx.workspace.domain.models.MenuItem_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

@ApplicationScoped
public class AssignmentDAO extends AbstractDAO<Assignment> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Assignment findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Assignment.class);
            var root = cq.from(Assignment.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public PageResult<Assignment> findByCriteria(AssignmentSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Assignment.class);
            var root = cq.from(Assignment.class);

            List<Predicate> predicates = new ArrayList<>();
            QueryCriteriaUtil.addSearchStringPredicate(predicates, cb, root.get(Assignment_.menuItemId),
                    criteria.getMenuItemId());
            QueryCriteriaUtil.addSearchStringPredicate(predicates, cb,
                    root.get(Assignment_.menuItem).get(MenuItem_.workspaceId), criteria.getWorkspaceId());

            cq.orderBy(cb.asc(root.get(AbstractTraceableEntity_.creationDate)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ASSIGNMENT_BY_CRITERIA, ex);
        }
    }

    @Transactional
    public void deleteAllByWorkspaceId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.deleteQuery();
            var root = cq.from(Assignment.class);
            cq.where(cb.equal(root.get(Assignment_.MENU_ITEM).get(MenuItem_.WORKSPACE_ID), id));
            getEntityManager().createQuery(cq).executeUpdate();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ITEMS_BY_WORKSPACE_ID, ex);
        }
    }

    @Transactional
    public void deleteAllByMenuId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.deleteQuery();
            var root = cq.from(Assignment.class);
            cq.where(cb.equal(root.get(Assignment_.MENU_ITEM_ID), id));
            getEntityManager().createQuery(cq).executeUpdate();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ITEMS_BY_MENU_ID, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_DELETE_ITEMS_BY_MENU_ID,

        ERROR_DELETE_ITEMS_BY_WORKSPACE_ID,

        ERROR_FIND_ASSIGNMENT_BY_CRITERIA,

        FIND_ENTITY_BY_ID_FAILED,

    }
}