package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.onecx.workspace.domain.models.MenuItem.*;
import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class MenuItemDAO extends AbstractDAO<MenuItem> {

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteAllMenuItemsByWorkspaceAndAppId(String workspaceId, String appId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE).get(Workspace_.ID), workspaceId),
                    cb.equal(root.get(MenuItem_.APPLICATION_ID), appId)));

            var items = getEntityManager().createQuery(cq).getResultList();
            delete(items);

        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID, ex);
        }
    }

    @Transactional
    public void deleteAllMenuItemsByWorkspace(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE).get(Workspace_.ID), id),
                    cb.isNull(root.get(MenuItem_.PARENT))));

            var items = getEntityManager().createQuery(cq).getResultList();
            delete(items);

        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE, ex);
        }
    }

    /**
     * This method delete all menu items by workspace id.
     *
     * @param id - workspace id
     */
    @Transactional
    public void deleteAllMenuItemsByWorkspaceId(String id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE).get(TraceableEntity_.ID), id),
                    cb.isNull(root.get(MenuItem_.PARENT))));

            var items = getEntityManager().createQuery(cq).getResultList();
            delete(items);

        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID, ex);
        }
    }

    public MenuItem loadMenuItemByWorkspaceAndKey(String workspaceName, String itemKey) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE).get(Workspace_.NAME), workspaceName),
                    cb.equal(root.get(MenuItem_.key), itemKey)));

            return getEntityManager()
                    .createQuery(cq)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_MENU_BY_ID_AND_KEY, ex);
        }
    }

    public List<MenuItem> loadAllMenuItemsByCriteria(MenuItemLoadCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            List<Predicate> predicates = new ArrayList<>();
            addSearchStringPredicate(predicates, cb, root.get(MenuItem_.WORKSPACE).get(Workspace_.ID),
                    criteria.getWorkspaceId());

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[] {}));
            }

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS))
                    .getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_CRITERIA, ex);
        }
    }

    public MenuItem loadAllChildren(String id) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.equal(root.get(MenuItem_.ID), id));

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
        predicates.add(cb.notEqual(root.get(MenuItem_.ID), menuId));
        if (parentId == null) {
            predicates.add(cb.isNull(root.get(MenuItem_.PARENT_ID)));
        } else {
            predicates.add(cb.equal(root.get(MenuItem_.PARENT_ID), parentId));
        }

        uq.set(MenuItem_.POSITION, cb.sum(root.get(MenuItem_.POSITION), sum))
                .set(MenuItem_.MODIFICATION_COUNT, cb.sum(root.get(MenuItem_.MODIFICATION_COUNT), 1))
                .where(cb.and(predicates.toArray(new Predicate[0])));

        this.getEntityManager().createQuery(uq).executeUpdate();
    }

    public enum ErrorKeys {

        ERROR_UPDATE_MENU_ITEM,
        ERROR_LOAD_MENU_BY_ID_AND_KEY,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_CRITERIA,

        ERROR_LOAD_ALL_CHILDREN,

        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID,
    }
}
