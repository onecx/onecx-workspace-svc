package io.github.onecx.workspace.domain.di;

import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import gen.io.github.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.io.github.onecx.workspace.di.workspace.v1.model.MenuItemStructureDTOV1;
import io.github.onecx.workspace.domain.daos.MenuItemDAO;
import io.github.onecx.workspace.domain.daos.ProductDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.di.mappers.WorkspaceDataImportMapperV1;
import io.github.onecx.workspace.domain.models.MenuItem;
import io.github.onecx.workspace.domain.models.Workspace;
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteAll(String tenanId) {
        try {
            var ctx = Context.builder()
                    .principal("data-import")
                    .tenantId(tenanId)
                    .build();
            log.info("#### tenant to delete " + tenanId);
            ApplicationContext.start(ctx);

            // clean data
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
                    .tenantId(importRequestDTO.getWorkspace().getTenantId())
                    .build();

            ApplicationContext.start(ctx);

            var dto = importRequestDTO.getWorkspace();
            var workspace = mapper.createWorkspace(dto);

            workspace = workspaceDAO.create(workspace);

            if (importRequestDTO.getMenuItems() != null && !importRequestDTO.getMenuItems().isEmpty()) {
                menuItemDAO.deleteAllMenuItemsByWorkspaceId(workspace.getId());
                List<MenuItem> menus = new LinkedList<>();
                recursiveMappingTreeStructure(importRequestDTO.getMenuItems(), workspace, null, menus);
                menuItemDAO.create(menus);
            }
        } finally {
            ApplicationContext.close();
        }

    }

    public void recursiveMappingTreeStructure(List<MenuItemStructureDTOV1> items, Workspace workspace, MenuItem parent,
            List<MenuItem> mappedItems) {
        int position = 0;
        for (MenuItemStructureDTOV1 item : items) {
            if (item != null) {
                MenuItem menu = mapper.mapMenu(item);
                menu.setWorkspace(workspace);
                menu.setWorkspaceName(workspace.getName());
                menu.setPosition(position);
                menu.setParent(parent);
                mappedItems.add(menu);
                position++;

                if (item.getChildren() == null || item.getChildren().isEmpty()) {
                    continue;
                }

                recursiveMappingTreeStructure(item.getChildren(), workspace, menu, mappedItems);
            }
        }
    }
}
