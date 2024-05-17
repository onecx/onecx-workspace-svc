package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.*;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.domain.template.models.WorkspaceCreateTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class WorkspaceService {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    MenuService menuService;

    @Inject
    ImageDAO imageDAO;

    @Inject
    RoleDAO roleDAO;

    @Inject
    SlotDAO slotDAO;

    @Inject
    ProductDAO productDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    ObjectMapper mapper;

    @Transactional
    public void deleteWorkspace(String id) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            return;
        }
        if (Boolean.TRUE.equals(workspace.getMandatory())) {
            return;
        }
        imageDAO.deleteQueryByRefId(workspace.getId());
        menuService.deleteAllMenuItemsForWorkspace(workspace.getId());
        workspaceDAO.delete(workspace);
    }

    @Transactional
    public Workspace createWorkspace(Workspace workspace, WorkspaceCreateTemplate template) {
        workspace = workspaceDAO.create(workspace);

        // template is disabled
        if (template == null) {
            return workspace;
        }

        productDAO.create(template.products());
        slotDAO.create(template.slots());
        roleDAO.create(template.roles());
        menuItemDAO.create(template.menus());
        assignmentDAO.create(template.assignments());

        return workspace;
    }
}
