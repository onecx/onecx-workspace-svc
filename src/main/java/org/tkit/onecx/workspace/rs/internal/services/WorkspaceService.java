package org.tkit.onecx.workspace.rs.internal.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;

@ApplicationScoped
public class WorkspaceService {
    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Transactional
    public void updateWorkspace(boolean updateMenuItem, Workspace workspace, String newWorkspaceName, String oldWorkspaceName,
            String baseUrl) {
        if (updateMenuItem) {
            menuItemDAO.updateMenuItems(newWorkspaceName, oldWorkspaceName, baseUrl);
        }
        workspaceDAO.update(workspace);
    }
}
