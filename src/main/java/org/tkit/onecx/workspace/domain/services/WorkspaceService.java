package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;

@ApplicationScoped
public class WorkspaceService {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    MenuService menuService;

    @Transactional
    public void deleteWorkspace(Workspace workspace) {
        menuService.deleteAllMenuItemsForWorkspace(workspace.getId());
        workspaceDAO.delete(workspace);
    }
}
