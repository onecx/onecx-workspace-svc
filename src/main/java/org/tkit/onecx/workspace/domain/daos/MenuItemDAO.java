package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.onecx.workspace.domain.models.MenuItem.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.criteria.MenuItemSearchCriteria;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class MenuItemDAO extends AbstractDAO<MenuItem> {

    /**
     * This method delete all menu items by workspace id.
     *
     * @param id - workspace id
     */
    @Transactional
    public int deleteAllMenuItemsByWorkspaceId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.deleteQuery();
            var root = cq.from(MenuItem.class);

            cq.where(cb.equal(root.get(MenuItem_.WORKSPACE).get(TraceableEntity_.ID), id));
            return getEntityManager().createQuery(cq).executeUpdate();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID, ex);
        }
    }

    /**
     * This method delete all menu items by workspace id.
     *
     * @param ids - workspace ids
     */
    @Transactional
    public void deleteAllMenuItemsByWorkspaceIds(Collection<String> ids) {
        try {
            var cq = this.deleteQuery();
            var root = cq.from(MenuItem.class);
            cq.where(root.get(MenuItem_.WORKSPACE_ID).in(ids));
            getEntityManager().createQuery(cq).executeUpdate();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_IDS, ex);
        }
    }

    public List<MenuItem> loadAllMenuItemsByCriteria(MenuItemLoadCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(MenuItem_.WORKSPACE_ID), criteria.getWorkspaceId()));
            cq.where(cb.and(predicates.toArray(new Predicate[] {})));

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS))
                    .getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_CRITERIA, ex);
        }
    }

    public List<MenuItem> loadAllMenuItemsByWorkspaces(Collection<String> workspaceIds) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);
            cq.where(root.get(MenuItem_.WORKSPACE_ID).in(workspaceIds));

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS))
                    .getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACES, ex);
        }
    }

    public PageResult<MenuItem> findByCriteria(MenuItemSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getWorkspaceId() != null && !criteria.getWorkspaceId().isBlank()) {
                predicates.add(cb.equal(root.get(MenuItem_.WORKSPACE_ID), criteria.getWorkspaceId()));
            }

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[] {})));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_MENU_ITEMS_BY_CRITERIA, ex);
        }
    }

    public MenuItem loadAllChildren(String id) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_LOAD_CHILDREN))
                    .getSingleResult();
        } catch (NoResultException nex) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_CHILDREN, ex);
        }
    }

    @Override
    public MenuItem findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = criteriaQuery();
            var root = cq.from(MenuItem.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(MenuItemDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public MenuItem updateMenuItem(MenuItem menuItem, String oldParentId, int oldPosition, String newParentId,
            int newPosition, boolean changeParent) {
        try {
            // update children position in old parent
            if (changeParent) {
                updatePosition(menuItem.getId(), oldParentId, oldPosition, -1);
            }

            // update children position in new parent
            if (changeParent || newPosition != oldPosition) {

                // check position,
                int count = countChildren(newParentId);

                // over the last position
                if (count > newPosition) {
                    updatePosition(menuItem.getId(), newParentId, newPosition, 1);
                } else if (count < newPosition) {
                    newPosition = count;
                    menuItem.setPosition(newPosition);
                }
            }

            // update menu item
            return this.update(menuItem);
        } catch (OptimisticLockException oe) {
            throw oe;
        } catch (Exception e) {
            throw new DAOException(MenuItemDAO.ErrorKeys.ERROR_UPDATE_MENU_ITEM, e, entityName);
        }
    }

    private int countChildren(String parentId) {
        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(MenuItem.class);
        cq.select(cb.count(root));
        if (parentId == null) {
            cq.where(cb.isNull(root.get(MenuItem_.PARENT_ID)));
        } else {
            cq.where(cb.equal(root.get(MenuItem_.PARENT_ID), parentId));
        }
        var result = getEntityManager().createQuery(cq).getSingleResult();
        return result.intValue();
    }

    private void updatePosition(String menuId, String parentId, int position, int sum) {
        var cb = getEntityManager().getCriteriaBuilder();
        var uq = this.updateQuery();
        var root = uq.from(MenuItem.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.greaterThanOrEqualTo(root.get(MenuItem_.POSITION), position));
        predicates.add(cb.notEqual(root.get(TraceableEntity_.ID), menuId));
        if (parentId == null) {
            predicates.add(cb.isNull(root.get(MenuItem_.PARENT_ID)));
        } else {
            predicates.add(cb.equal(root.get(MenuItem_.PARENT_ID), parentId));
        }

        uq.set(MenuItem_.POSITION, cb.sum(root.get(MenuItem_.POSITION), sum))
                .set(AbstractTraceableEntity_.MODIFICATION_COUNT,
                        cb.sum(root.get(AbstractTraceableEntity_.MODIFICATION_COUNT), 1))
                .where(cb.and(predicates.toArray(new Predicate[0])));

        this.getEntityManager().createQuery(uq).executeUpdate();
    }

    public enum ErrorKeys {

        ERROR_UPDATE_MENU_ITEM,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_IDS,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_CRITERIA,

        ERROR_FIND_MENU_ITEMS_BY_CRITERIA,

        ERROR_LOAD_ALL_CHILDREN,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACES,
    }
}
