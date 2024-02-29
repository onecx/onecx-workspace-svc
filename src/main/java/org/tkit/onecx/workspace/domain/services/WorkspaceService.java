package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.ImageDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;

@ApplicationScoped
public class WorkspaceService {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    MenuService menuService;

    @Inject
    ImageDAO imageDAO;

    @Transactional
    public void deleteWorkspace(String id) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            return;
        }
        imageDAO.deleteQueryByRefId(workspace.getId());
        menuService.deleteAllMenuItemsForWorkspace(workspace.getId());
        workspaceDAO.delete(workspace);
    }
}
