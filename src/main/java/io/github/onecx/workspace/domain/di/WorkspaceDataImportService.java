package io.github.onecx.workspace.domain.di;

import java.util.LinkedList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.io.github.onecx.workspace.di.workspace.v1.model.MenuItemStructureDTOV1;
import gen.io.github.onecx.workspace.di.workspace.v1.model.WorkspaceDataImportDTOV1;
import io.github.onecx.workspace.domain.daos.MenuItemDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.di.mappers.WorkspaceDataImportMapperV1;
import io.github.onecx.workspace.domain.models.MenuItem;
import io.github.onecx.workspace.domain.models.Workspace;

@DataImport("workspace")
public class WorkspaceDataImportService implements DataImportService {

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WorkspaceDataImportMapperV1 mapper;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importData(DataImportConfig config) {
        try {
            var operation = config.getMetadata().getOrDefault("operation", "NONE");
            if ("NONE".equals(operation)) {
                return;
            }
            if ("CLEAN_INSERT".equals(operation)) {
                var data = objectMapper.readValue(config.getData(), WorkspaceDataImportDTOV1.class);
                cleanInsert(data);
            }
        } catch (Exception ex) {
            throw new ImportException(ex.getMessage(), ex);
        }
    }

    public void cleanInsert(WorkspaceDataImportDTOV1 data) {
        if (data == null) {
            return;
        }

        // clean data
        menuItemDAO.deleteAll();
        workspaceDAO.deleteAll();

        // import portals
        importRequests(data);
    }

    public void importRequests(WorkspaceDataImportDTOV1 data) {
        if (data.getRequests() == null) {
            return;
        }

        for (var request : data.getRequests()) {
            try {
                importRequest(request);
            } catch (Exception ex) {
                if (request.getWorkspace() != null && request.getWorkspace().getWorkspaceName() != null) {
                    throw new ImportException("Error import portal " + request.getWorkspace().getWorkspaceName(), ex);
                } else {
                    throw new ImportException("Error import portal", ex);
                }
            }
        }
    }

    public void importRequest(ImportRequestDTOV1 importRequestDTO) {

        var dto = importRequestDTO.getWorkspace();
        var workspace = mapper.createWorkspace(dto);

        workspace = workspaceDAO.create(workspace);

        if (importRequestDTO.getMenuItems() != null && !importRequestDTO.getMenuItems().isEmpty()) {
            menuItemDAO.deleteAllMenuItemsByWorkspaceId(workspace.getId());
            List<MenuItem> menus = new LinkedList<>();
            recursiveMappingTreeStructure(importRequestDTO.getMenuItems(), workspace, null, menus);
            menuItemDAO.create(menus);
        }

    }

    public void recursiveMappingTreeStructure(List<MenuItemStructureDTOV1> items, Workspace workspace, MenuItem parent,
            List<MenuItem> mappedItems) {
        int position = 0;
        for (MenuItemStructureDTOV1 item : items) {
            if (item != null) {
                MenuItem menu = mapper.mapMenu(item);
                menu.setWorkspace(workspace);
                menu.setWorkspaceName(workspace.getWorkspaceName());
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

    public static class ImportException extends RuntimeException {

        public ImportException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
