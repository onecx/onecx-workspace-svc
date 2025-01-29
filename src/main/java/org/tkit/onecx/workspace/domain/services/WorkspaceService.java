package org.tkit.onecx.workspace.domain.services;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.*;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.onecx.workspace.domain.template.models.WorkspaceCreateTemplate;

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

    @Transactional
    public void deleteWorkspace(String id) {
        var workspace = workspaceDAO.findById(id);
        if (workspace == null) {
            return;
        }
        if (Boolean.TRUE.equals(workspace.getMandatory())) {
            return;
        }
        imageDAO.deleteQueryByRefId(workspace.getName());
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

    @Transactional
    public void importWorkspace(List<Workspace> createWorkspaces, List<Image> createImages, List<Slot> createSlots,
            List<Product> createProducts, List<MenuItem> menuItems, List<Assignment> assignments) {
        imageDAO.deleteQueryByRefIds(createImages.stream()
                .map(Image::getRefId).toList());
        imageDAO.create(createImages);
        workspaceDAO.create(createWorkspaces);
        productDAO.create(createProducts);
        slotDAO.create(createSlots);
        menuItemDAO.create(menuItems);
        assignmentDAO.create(assignments);
    }

    @Transactional
    public void importOperator(List<Workspace> workspaces, List<Image> images, List<Slot> slots,
            List<Product> products, List<MenuItem> menuItems, List<Assignment> assignments) {
        if (workspaces.isEmpty()) {
            return;
        }
        var names = workspaces.stream().map(Workspace::getName).collect(Collectors.toSet());

        // delete existing data
        var tmp = workspaceDAO.findByNames(names);
        if (!tmp.isEmpty()) {
            var workspaceIds = tmp.stream().map(Workspace::getId).collect(Collectors.toSet());
            assignmentDAO.deleteAllByWorkspaceIds(workspaceIds);
            menuItemDAO.deleteAllMenuItemsByWorkspaceIds(workspaceIds);
            imageDAO.deleteQueryByRefIds(names);
            workspaceDAO.delete(tmp);
        }

        // create new data
        workspaceDAO.create(workspaces);
        imageDAO.create(images);
        productDAO.create(products);
        slotDAO.create(slots);
        menuItemDAO.create(menuItems);
        assignmentDAO.create(assignments);
    }

}
