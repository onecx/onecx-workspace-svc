package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.RoleSearchCriteria;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class RoleDAO extends AbstractDAO<Role> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Role findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Role.class);
            var root = cq.from(Role.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public PageResult<Role> findByCriteria(RoleSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Role.class);
            var root = cq.from(Role.class);

            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getWorkspaceId() != null && !criteria.getWorkspaceId().isBlank()) {
                predicates.add(cb.equal(root.get(Role_.workspaceId), criteria.getWorkspaceId()));
            }
            addSearchStringPredicate(predicates, cb, root.get(Role_.name), criteria.getName());
            addSearchStringPredicate(predicates, cb, root.get(Role_.description), criteria.getDescription());

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[] {}));
            }

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ROLE_BY_CRITERIA, ex);
        }
    }

    public List<Role> findRolesByWorkspaceAndNames(String workspaceId, Set<String> names) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Role.class);
            var root = cq.from(Role.class);
            cq.where(cb.and(
                    cb.equal(root.get(Role_.WORKSPACE_ID), workspaceId),
                    root.get(Role_.name).in(names)));
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FILTER_ROLE_NAMES, ex);
        }
    }

    public Role loadById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Role.class);
            var root = cq.from(Role.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(Role.ROLE_LOAD))
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.LOAD_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public enum ErrorKeys {

        ERROR_FILTER_ROLE_NAMES,
        ERROR_FIND_ROLE_BY_CRITERIA,
        FIND_ENTITY_BY_ID_FAILED,

        LOAD_ENTITY_BY_ID_FAILED,
    }
}
