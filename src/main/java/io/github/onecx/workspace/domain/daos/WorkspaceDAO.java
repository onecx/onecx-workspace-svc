package io.github.onecx.workspace.domain.daos;

import static io.github.onecx.workspace.domain.models.Workspace_.THEME;
import static io.github.onecx.workspace.domain.models.Workspace_.WORKSPACE_NAME;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

import io.github.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.domain.models.Workspace_;

@ApplicationScoped
public class WorkspaceDAO extends AbstractDAO<Workspace> {

    /**
     * Methods count number of portal registered for the micro-frontend.
     *
     * @param id micro-frontend id
     * @return the number of portal registered for the micro-frontend.
     */
    // public long countPortalsForRegMfeId(String id) {
    //     try {
    //         var cb = this.getEntityManager().getCriteriaBuilder();
    //         CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    //         var root = cq.from(Workspace.class);
    //         cq.select(cb.count(root));
    //         cq.where(cb.equal(root.get(W.MICROFRONTEND_REGISTRATIONS).get(MicrofrontendRegistration_.MFE_ID), id));
    //         return this.getEntityManager().createQuery(cq).getSingleResult();

    //     } catch (NoResultException nre) {
    //         return 0;
    //     } catch (Exception ex) {
    //         throw new DAOException(ErrorKeys.ERROR_COUNT_PORTALS_FOR_MFE_ID, ex);
    //     }
    // }

    /**
     * This method fetches a portal with
     * portalName provided as a param and
     * tenantId provided as a param
     *
     * @return Workspace entity if exists otherwise null
     */
    public Workspace findByWorkspaceName(String workspaceName) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.equal(root.get(WORKSPACE_NAME), workspaceName));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_WORKSPACE_NAME, ex);
        }
    }

    /**
     * This method fetches the whole workspace with all his lazy load objects
     * workspaceName provided as a param and
     * tenantId provided as a param
     *
     * @return Workspace entity if exists otherwise null
     */
    public Workspace loadByWorkspaceName(String workspaceName) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.equal(root.get(WORKSPACE_NAME), workspaceName));

            var workspaceQuery = this.getEntityManager().createQuery(cq);
            workspaceQuery.setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Workspace.WORKSPACE_FULL));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_WORKSPACE_NAME, ex);
        }
    }

    public Workspace findByBaseUrl(String baseUrl) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);

            cq.where(cb.like(cb.literal(baseUrl), cb.concat(root.get(Workspace_.BASE_URL), "%")));
            cq.orderBy(cb.desc(cb.length(root.get(Workspace_.BASE_URL))));
            var results = this.getEntityManager().createQuery(cq).getResultList();

            if (results.isEmpty()) {
                return null;
            }
            return results.get(0);
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_BASE_URL, ex);
        }
    }

    public PageResult<Workspace> findBySearchCriteria(WorkspaceSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var workspaceTable = cq.from(Workspace.class);

            if (criteria != null) {
                List<Predicate> predicates = new ArrayList<>();
                if (criteria.getWorkspaceName() != null && !criteria.getWorkspaceName().isEmpty()) {
                    predicates.add(
                            cb.like(workspaceTable.get(WORKSPACE_NAME),
                                    QueryCriteriaUtil.wildcard(criteria.getWorkspaceName(), false)));
                }
                if (criteria.getThemeName() != null && !criteria.getThemeName().isEmpty()) {
                    predicates.add(
                            cb.like(workspaceTable.get(THEME), QueryCriteriaUtil.wildcard(criteria.getThemeName(), false)));
                }
                if (!predicates.isEmpty()) {
                    cq.where(cb.and(predicates.toArray(new Predicate[0])));
                }
            }

            cq.orderBy(cb.asc(workspaceTable.get(WORKSPACE_NAME)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_CRITERIA, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_FIND_BY_BASE_URL,

        ERROR_FIND_BY_CRITERIA,
        ERROR_FIND_WORKSPACE_NAME,
        ERROR_COUNT_PORTALS_FOR_MFE_ID,
    }

}
