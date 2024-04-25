package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.onecx.workspace.domain.models.Workspace_.*;
import static org.tkit.quarkus.jpa.models.TraceableEntity_.ID;
import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;

import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class WorkspaceDAO extends AbstractDAO<Workspace> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Workspace findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw handleConstraint(e, ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        }
    }

    public Workspace findByName(String workspaceName) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);
            cq.where(cb.equal(root.get(NAME), workspaceName));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_WORKSPACE_BY_NAME);
        }
    }

    public Workspace findByUrl(String url) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);
            cq.where(cb.like(cb.literal(url), cb.concat(root.get(BASE_URL), "%")));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_WORKSPACE_BY_URL);
        }
    }

    /**
     * This method fetches the whole workspace with all his lazy load objects
     */
    public Workspace loadById(String id) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.equal(root.get(ID), id));

            var workspaceQuery = this.getEntityManager().createQuery(cq);
            workspaceQuery.setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Workspace.WORKSPACE_FULL));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_LOAD_WORKSPACE);
        }
    }

    public Workspace loadByNameProducts(String name) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.equal(root.get(NAME), name));
            var workspaceQuery = this.getEntityManager().createQuery(cq);
            workspaceQuery.setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Workspace.WORKSPACE_PRODUCTS_SLOTS));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_LOAD_WORKSPACE_PRODUCTS);
        }
    }

    public Workspace loadByUrlProductsSlots(String url) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.like(cb.literal(url), cb.concat(root.get(BASE_URL), "%")));
            var workspaceQuery = this.getEntityManager().createQuery(cq);
            workspaceQuery.setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Workspace.WORKSPACE_PRODUCTS_SLOTS));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_LOAD_WORKSPACE_PRODUCTS_SLOTS);
        }
    }

    public PageResult<Workspace> findBySearchCriteria(WorkspaceSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var workspaceTable = cq.from(Workspace.class);

            List<Predicate> predicates = new ArrayList<>();

            addSearchStringPredicate(predicates, cb, workspaceTable.get(NAME), criteria.getName());
            addSearchStringPredicate(predicates, cb, workspaceTable.get(THEME), criteria.getThemeName());
            addSearchStringPredicate(predicates, cb, workspaceTable.get(BASE_URL), criteria.getBaseUrl());
            if (criteria.getNames() != null && !criteria.getNames().isEmpty()) {
                predicates.add(workspaceTable.get(NAME).in(criteria.getNames()));
            }

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            cq.orderBy(cb.asc(workspaceTable.get(NAME)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_BY_CRITERIA);
        }
    }

    public enum ErrorKeys {

        ERROR_LOAD_WORKSPACE_PRODUCTS,
        ERROR_LOAD_WORKSPACE_PRODUCTS_SLOTS,
        ERROR_FIND_WORKSPACE_BY_NAME,
        ERROR_FIND_WORKSPACE_BY_URL,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_BY_CRITERIA,
        ERROR_LOAD_WORKSPACE
    }

}
