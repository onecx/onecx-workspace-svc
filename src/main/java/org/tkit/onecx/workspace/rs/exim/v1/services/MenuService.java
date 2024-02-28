package org.tkit.onecx.workspace.rs.exim.v1.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.quarkus.log.cdi.LogService;

@ApplicationScoped
public class MenuService {

    @Inject
    MenuItemDAO dao;

    @LogService
    @Transactional
    public int importMenuItems(String workspaceId, List<MenuItem> items) {
        var deleted = dao.deleteAllMenuItemsByWorkspaceId(workspaceId);
        dao.create(items);
        return deleted;
    }
}
