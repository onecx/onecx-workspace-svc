package org.tkit.onecx.workspace.domain.daos;

import static org.tkit.onecx.workspace.domain.models.MenuItem.MENU_ITEM_LOAD_ALL;
import static org.tkit.onecx.workspace.domain.models.MenuItem.MENU_ITEM_WORKSPACE_AND_TRANSLATIONS;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

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
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME, ex);
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

    public MenuItem loadMenuItemByWorkspaceAndKey(String id, String itemKey) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.ID), id),
                    cb.equal(root.get(MenuItem_.key), itemKey)));

            return getEntityManager()
                    .createQuery(cq)
                    .getSingleResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME, ex);
        }
    }

    /**
     * This method fetches all menuItems assigned to a workspace with
     *
     * @param id - provided as a param and
     *
     * @return List of the menu items
     */
    public List<MenuItem> loadAllMenuItemsByWorkspace(String id) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.equal(root.get(MenuItem_.WORKSPACE).get(Workspace_.ID), id));

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS))
                    .getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME, ex);
        }
    }

    @Override
    public MenuItem findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(MenuItemDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public MenuItem loadById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH,
                            this.getEntityManager().getEntityGraph(MENU_ITEM_LOAD_ALL))
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(MenuItemDAO.ErrorKeys.LOAD_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,

        LOAD_ENTITY_BY_ID_FAILED,
        ERROR_UPDATE_MENU_ITEMS,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME,

        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID,
    }
}
