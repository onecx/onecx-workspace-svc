package org.tkit.onecx.workspace.rs.internal.services;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

@ApplicationScoped
public class MenuItemService {

    @Inject
    MenuItemDAO dao;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public UpdateResult updateMenuItem(String menuItemId, String newParentItemId) {
        var menuItem = dao.loadById(menuItemId);
        if (menuItem == null) {
            return null;
        }

        if (newParentItemId == null) {
            return UpdateResult.of(menuItem, null, true);
        }

        // check parent change
        if (menuItem.getParent() != null && newParentItemId.equals(menuItem.getParent().getId())) {
            return UpdateResult.of(menuItem, null, false);
        }

        // checking if request parent id is the same as current id
        if (newParentItemId.equals(menuItem.getId())) {
            throw new ConstraintException("Menu Item " + menuItem.getId() + " id and parentItem id are the same",
                    MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM, null);
        }

        // checking if parent exists
        var parent = dao.findById(newParentItemId);
        if (parent == null) {
            throw new ConstraintException("Parent menu item " + newParentItemId + " does not exists",
                    MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST, null);
        } else {
            // checking if parent exists in the same portal
            if (!parent.getWorkspace().getId().equals(menuItem.getWorkspace().getId())) {
                throw new ConstraintException("Parent menu item is assigned to different portal",
                        MenuItemErrorKeys.PARENT_ASSIGNED_TO_DIFFERENT_PORTAL, null);
            }

            // check for cycle
            Set<String> children = new HashSet<>();
            children(menuItem, children);
            if (children.contains(parent.getId())) {
                throw new ConstraintException(
                        "One of the items try to set one of its children to the new parent. Cycle dependency can not be created in tree structure",
                        MenuItemErrorKeys.CYCLE_DEPENDENCY, null);
            }
        }

        // set new parent
        return UpdateResult.of(menuItem, parent, true);
    }

    public static class UpdateResult {
        public MenuItem menuItem;
        public MenuItem parent;
        public boolean parentChange;

        public static UpdateResult of(MenuItem menuItem, MenuItem parent, boolean parentChange) {
            var r = new UpdateResult();
            r.menuItem = menuItem;
            r.parent = parent;
            r.parentChange = parentChange;
            return r;
        }

    }

    private void children(MenuItem menuItem, Set<String> result) {
        menuItem.getChildren().forEach(c -> {
            result.add(c.getId());
            children(c, result);
        });
    }

    public enum MenuItemErrorKeys {
        PARENT_MENU_DOES_NOT_EXIST,

        PARENT_MENU_SAME_AS_MENU_ITEM,

        PARENT_ASSIGNED_TO_DIFFERENT_PORTAL,

        CYCLE_DEPENDENCY;

    }
}
