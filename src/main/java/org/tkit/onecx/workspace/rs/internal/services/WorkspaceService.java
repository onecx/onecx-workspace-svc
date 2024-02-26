package org.tkit.onecx.workspace.rs.internal.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;

@ApplicationScoped
public class WorkspaceService {

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Transactional
    public void deleteWorkspace(Workspace workspace) {
        assignmentDAO.deleteAllByWorkspaceId(workspace.getId());
        menuItemDAO.deleteAllMenuItemsByWorkspaceId(workspace.getId());
        workspaceDAO.delete(workspace);
    }
}
