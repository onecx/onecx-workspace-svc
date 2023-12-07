package io.github.onecx.workspace.domain.daos;

import static io.github.onecx.workspace.domain.models.MenuItem.MENU_ITEM_WORKSPACE_AND_TRANSLATIONS;
import static jakarta.persistence.criteria.JoinType.LEFT;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import io.github.onecx.workspace.domain.models.*;

@ApplicationScoped
public class MenuItemDAO extends AbstractDAO<MenuItem> {

    /**
     * This method updates menu items with new workspaceName provided as a param
     * based on oldworkspaceName provided as a param
     */
    @Transactional
    public void updateMenuItems(String newWorkspaceName, String oldWorkspaceName, String baseUrl) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var update = cb.createCriteriaUpdate(MenuItem.class);
            var root = update.from(MenuItem.class);
            update.set(MenuItem_.WORKSPACE_NAME, newWorkspaceName);
            update.set(MenuItem_.URL, baseUrl);
            var subquery = update.subquery(MenuItem.class);
            var root2 = subquery.from(MenuItem.class);
            subquery.select(root2);

            Join<MenuItem, Workspace> join = root2.join(MenuItem_.WORKSPACE, LEFT);
            subquery.where(cb.equal(join.get(Workspace_.WORKSPACE_NAME), oldWorkspaceName));

            update.where(root.in(subquery));
            this.em.createQuery(update).executeUpdate();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_UPDATE_MENU_ITEMS, ex);
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

    /**
     * This method delete all menu items by workspace name and application id.
     *
     * @param workspaceName - workspace name
     * @param appId - application id
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteAllMenuItemsByWorkspaceNameAndAppId(String workspaceName, String appId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE_NAME), workspaceName),
                    cb.equal(root.get(MenuItem_.APPLICATION_ID), appId)));

            var items = getEntityManager().createQuery(cq).getResultList();
            delete(items);

        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID, ex);
        }
    }

    /**
     * This method fetches all menuItems assigned to a workspace with
     *
     * @param workspaceName - provided as a param and
     *
     * @return List of the menu items
     */
    public MenuItem loadMenuItemByWorkspaceAndKey(String workspaceName, String itemKey) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.and(
                    cb.equal(root.get(MenuItem_.WORKSPACE_NAME), workspaceName),
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
     * @param workspaceName - provided as a param and
     *
     * @return List of the menu items
     */
    public List<MenuItem> loadAllMenuItemsByWorkspaceName(String workspaceName) {

        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var root = cq.from(MenuItem.class);

            cq.where(cb.equal(root.get(MenuItem_.WORKSPACE).get(Workspace_.WORKSPACE_NAME), workspaceName));

            return getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS))
                    .getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME, ex);
        }
    }

    /**
     * This method fetches all menuItems assigned to a workspace with just
     * id provided as a param
     *
     * @return List of the menu items
     */
    public List<MenuItem> loadAllMenuItemsByWorkspaceId(String workspaceId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(MenuItem.class);
            var menuItem = cq.from(MenuItem.class);
            cq.where(cb.equal(menuItem.get(MenuItem_.WORKSPACE).get(TraceableEntity_.ID), workspaceId));
            var menuItemsQuery = getEntityManager().createQuery(cq);
            menuItemsQuery.setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(MENU_ITEM_WORKSPACE_AND_TRANSLATIONS));
            return menuItemsQuery.getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_ID, ex);
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

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,

        ERROR_UPDATE_MENU_ITEMS,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_ID,
        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID,
        ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME,
        ERROR_LOAD_ALL_MENU_ITEM_BY_KEY,

        ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID,
    }
}
