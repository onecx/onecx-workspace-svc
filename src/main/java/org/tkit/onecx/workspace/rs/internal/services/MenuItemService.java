package org.tkit.onecx.workspace.rs.internal.services;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.org.tkit.onecx.workspace.rs.internal.model.MenuItemDTO;

@ApplicationScoped
public class MenuItemService {

    @Inject
    MenuItemDAO dao;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public UpdateResult updateMenuItem(String id, String menuItemId, MenuItemDTO dto) {
        var menuItem = dao.findById(menuItemId);
        if (menuItem == null) {
            return null;
        }
        menuItem = dao.loadById(menuItemId);

        if (dto.getParentItemId() == null) {
            menuItem.setParent(null);
            return UpdateResult.of(menuItem, null, true);
        }

        // check parent change
        if (menuItem.getParent() != null && dto.getParentItemId().equals(menuItem.getParent().getId())) {
            return UpdateResult.of(menuItem, null, false);
        }

        // checking if request parent id is the same as current id
        if (dto.getParentItemId().equals(menuItem.getId())) {
            throw new ConstraintException("Menu Item " + menuItem.getId() + " id and parentItem id are the same",
                    MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM, null);
        }

        // checking if parent exists
        var parent = dao.findById(dto.getParentItemId());
        if (parent == null) {
            throw new ConstraintException("Parent menu item " + dto.getParentItemId() + " does not exists",
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
        public boolean change;

        public static UpdateResult of() {
            return new UpdateResult();
        }

        public static UpdateResult of(MenuItem menuItem, MenuItem parent) {
            var r = new UpdateResult();
            r.menuItem = menuItem;
            r.parent = parent;
            return r;
        }

        public static UpdateResult of(MenuItem menuItem, MenuItem parent, boolean change) {
            var r = new UpdateResult();
            r.menuItem = menuItem;
            r.parent = parent;
            r.change = change;
            return r;
        }

    }

    private void children(MenuItem menuItem, Set<String> result) {
        menuItem.getChildren().forEach(c -> {
            result.add(c.getId());
            children(c, result);
        });
    }

    enum MenuItemErrorKeys {
        PARENT_MENU_DOES_NOT_EXIST,

        PARENT_MENU_SAME_AS_MENU_ITEM,

        PARENT_ASSIGNED_TO_DIFFERENT_PORTAL,

        CYCLE_DEPENDENCY;

    }
}
