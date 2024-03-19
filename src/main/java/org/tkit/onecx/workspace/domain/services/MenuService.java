package org.tkit.onecx.workspace.domain.services;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.RoleDAO;
import org.tkit.onecx.workspace.domain.models.Assignment;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.onecx.workspace.domain.models.Workspace;
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

    @Transactional(Transactional.TxType.SUPPORTS)
    public ImportRequest importMenuItems(Workspace workspace, List<MenuItem> items, Map<String, Set<String>> menuRoles) {

        var roleNames = menuRoles.values().stream().flatMap(Collection::stream).collect(toSet());
        var existingRoles = roleDAO.findRolesByWorkspaceAndNames(workspace.getId(), roleNames);

        Map<String, Role> tmp = new HashMap<>();
        List<Role> roles = new ArrayList<>();
        if (!roleNames.isEmpty()) {

            // check for existing roles
            if (!existingRoles.isEmpty()) {
                tmp = existingRoles.stream().collect(toMap(Role::getName, role -> role));
                roleNames.removeAll(tmp.keySet());
            }

            // create new roles
            for (String rn : roleNames) {
                Role r = new Role();
                r.setWorkspace(workspace);
                r.setName(rn);
                tmp.put(rn, r);
                roles.add(r);
            }
        }

        List<Assignment> assignments = new ArrayList<>();
        for (MenuItem menuItem : items) {
            var mr = menuRoles.get(menuItem.getId());
            if (mr != null) {

                for (String roleName : mr) {
                    var role = tmp.get(roleName);

                    var assignment = new Assignment();
                    assignment.setMenuItem(menuItem);
                    assignment.setRole(role);
                    assignments.add(assignment);
                }
            }
        }

        return new ImportRequest(workspace.getId(), roles, items, assignments);
    }

    @Transactional
    public int importMenuItemsForWorkspace(ImportRequest request) {
        assignmentDAO.deleteAllByWorkspaceId(request.workspaceId);
        var deleted = dao.deleteAllMenuItemsByWorkspaceId(request.workspaceId);
        roleDAO.create(request.roles);
        dao.create(request.menuItems);
        assignmentDAO.create(request.assignments);
        return deleted;
    }

    public record ImportRequest(String workspaceId, List<Role> roles, List<MenuItem> menuItems, List<Assignment> assignments) {
    }

    @Transactional
    public void deleteMenuItem(MenuItem menuItem) {
        var childIds = children(menuItem);
        if (menuItem != null) {
            childIds.add(menuItem.getId());
        }
        assignmentDAO.deleteAllByMenuId(childIds);
        dao.deleteQueryByIds(childIds);
    }

    private List<Object> children(MenuItem menuItem) {
        List<Object> ids = new ArrayList<>();
        if (menuItem != null) {
            menuItem.getChildren().forEach(item -> {
                ids.add(item.getId());
                ids.addAll(children(item));
            });
        }
        return ids;
    }

    @Transactional
    public void deleteAllMenuItemsForWorkspace(String workspaceId) {
        assignmentDAO.deleteAllByWorkspaceId(workspaceId);
        dao.deleteAllMenuItemsByWorkspaceId(workspaceId);
    }
}
