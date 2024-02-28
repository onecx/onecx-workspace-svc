package org.tkit.onecx.workspace.domain.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.RoleDAO;
import org.tkit.onecx.workspace.domain.models.Assignment;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.quarkus.log.cdi.LogService;

@LogService
@ApplicationScoped
public class MenuService {

    @Inject
    MenuItemDAO dao;

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    RoleDAO roleDAO;

    @Transactional
    public int importMenuItems(String workspaceId, List<MenuItem> items, List<Role> roles, List<Assignment> assignments) {
        assignmentDAO.deleteAllByWorkspaceId(workspaceId);
        var deleted = dao.deleteAllMenuItemsByWorkspaceId(workspaceId);
        roleDAO.create(roles);
        dao.create(items);
        assignmentDAO.create(assignments);
        return deleted;
    }

    @Transactional
    public void deleteMenuItem(String menuItemId) {
        assignmentDAO.deleteAllByMenuId(menuItemId);
        dao.deleteQueryById(menuItemId);
    }

    @Transactional
    public void deleteAllMenuItemsForWorkspace(String workspaceId) {
        assignmentDAO.deleteAllByWorkspaceId(workspaceId);
        dao.deleteAllMenuItemsByWorkspaceId(workspaceId);
    }
}
