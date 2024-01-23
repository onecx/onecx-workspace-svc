package io.github.onecx.workspace.domain.daos;

import static io.github.onecx.workspace.domain.models.Workspace_.THEME;
import static io.github.onecx.workspace.domain.models.Workspace_.WORKSPACE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

import io.github.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.domain.models.WorkspaceInfo;
import io.github.onecx.workspace.domain.models.Workspace_;

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

    /**
     * This method fetches a workspace with
     * workspaceName provided as a param and
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
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_WORKSPACE_NAME);
        }
    }

    /**
     * This method fetches multiple workspaces with
     * workspaceNames provided as a param and
     * tenantId provided as a param
     *
     * @return Stream containing Workspace entities if exists otherwise empty
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Stream<Workspace> findByWorkspaceNames(Set<String> workspaceNames) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var root = cq.from(Workspace.class);
            cq.where(root.get(WORKSPACE_NAME).in(workspaceNames));
            return this.getEntityManager().createQuery(cq).getResultStream();
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_WORKSPACE_NAME);
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
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_WORKSPACE_NAME);
        }
    }

    public PageResult<Workspace> findBySearchCriteria(WorkspaceSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Workspace.class);
            var workspaceTable = cq.from(Workspace.class);

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

            cq.orderBy(cb.asc(workspaceTable.get(WORKSPACE_NAME)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_BY_CRITERIA);
        }
    }

    /**
     * This method fetches a workspace info with
     * themeName provided as a param and
     * tenantId provided as a param
     *
     * @return WorkspaceInfo entity if exists otherwise null
     */
    public Stream<WorkspaceInfo> findByThemeName(String themeName) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(WorkspaceInfo.class);
            var root = cq.from(Workspace.class);

            cq.select(cb.construct(WorkspaceInfo.class, root.get(WORKSPACE_NAME), root.get(Workspace_.DESCRIPTION)));
            cq.where(cb.equal(root.get(THEME), themeName));

            return this.getEntityManager().createQuery(cq).getResultStream();
        } catch (Exception ex) {
            throw handleConstraint(ex, ErrorKeys.ERROR_FIND_BY_THEME_NAME);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_BY_BASE_URL,

        ERROR_FIND_BY_CRITERIA,
        ERROR_FIND_WORKSPACE_NAME,

        ERROR_FIND_BY_THEME_NAME,
    }

}
