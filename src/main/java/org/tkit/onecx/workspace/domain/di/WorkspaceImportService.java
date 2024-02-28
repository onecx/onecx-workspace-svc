package org.tkit.onecx.workspace.domain.di;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.*;
import org.tkit.onecx.workspace.domain.di.mappers.WorkspaceDataImportMapperV1;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.services.MenuService;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import gen.org.tkit.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.MenuItemStructureDTOV1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class WorkspaceImportService {

    @Inject
    WorkspaceDataImportMapperV1 mapper;

    @Inject
    ProductDAO productDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    RoleDAO roleDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    MenuService menuService;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteAll(String tenanId) {
        try {
            var ctx = Context.builder()
                    .principal("data-import")
                    .tenantId(tenanId)
                    .build();

            ApplicationContext.start(ctx);

            // clean data
            assignmentDAO.deleteAll();
            roleDAO.deleteAll();
            productDAO.deleteAll();
            menuItemDAO.deleteAll();
            workspaceDAO.deleteAll();
        } finally {
            ApplicationContext.close();
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importRequest(ImportRequestDTOV1 importRequestDTO) {

        try {
            var ctx = Context.builder()
                    .principal("data-import")
                    .tenantId(importRequestDTO.getTenantId())
                    .build();

            ApplicationContext.start(ctx);

            var dto = importRequestDTO.getWorkspace();
            var workspace = mapper.createWorkspace(dto);

            workspace = workspaceDAO.create(workspace);

            List<MenuItemStructureDTOV1> importMenus = importRequestDTO.getMenuItems();
            if (importMenus != null && !importMenus.isEmpty()) {

                // convert DTO to menu items
                List<MenuItem> items = new LinkedList<>();
                Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(importMenus, workspace, null, items);

                // validate menu, roles, assignments
                var request = menuService.importMenuItems(workspace, items, menuRoles);

                // execute update in database
                menuService.importMenuItemsForWorkspace(request);
            }

        } finally {
            ApplicationContext.close();
        }

    }

}
