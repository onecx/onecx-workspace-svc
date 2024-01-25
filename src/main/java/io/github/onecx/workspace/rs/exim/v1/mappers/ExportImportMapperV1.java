package io.github.onecx.workspace.rs.exim.v1.mappers;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.exim.v1.model.*;
import io.github.onecx.workspace.domain.models.MenuItem;
import io.github.onecx.workspace.domain.models.Workspace;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ExportImportMapperV1 {
    default WorkspaceSnapshotDTOV1 create(Map<String, Workspace> workspaces) {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        snapshot.setCreated(OffsetDateTime.now());
        snapshot.setId(UUID.randomUUID().toString());
        snapshot.setWorkspaces(map(workspaces));
        return snapshot;
    }

    Map<String, EximWorkspaceDTOV1> map(Map<String, Workspace> data);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    Workspace create(EximWorkspaceDTOV1 workspaceDTO);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "workspaces", source = "workspaces")
    ImportWorkspaceResponseDTOV1 create(WorkspaceSnapshotDTOV1 request,
            Map<String, ImportResponseStatusDTOV1> workspaces);

    @Mapping(target = "removeSubjectLinkItem", ignore = true)
    @Mapping(target = "removeImageUrlItem", ignore = true)
    EximWorkspaceDTOV1 map(Workspace workspace);

    default MenuSnapshotDTOV1 create(List<MenuItem> menuStructure) {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        snapshot.setCreated(OffsetDateTime.now());
        snapshot.setId(UUID.randomUUID().toString());
        snapshot.setMenu(map(menuStructure));
        return snapshot;
    }

    default EximMenuStructureDTOV1 map(List<MenuItem> menuItems) {
        EximMenuStructureDTOV1 structureDTOV1 = new EximMenuStructureDTOV1();
        structureDTOV1.setMenuItems(mapList(menuItems));
        return structureDTOV1;
    }

    List<EximWorkspaceMenuItemDTOV1> mapList(List<MenuItem> menuItems);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    EximWorkspaceMenuItemDTOV1 map(MenuItem menuItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "parent.id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    MenuItem map(EximWorkspaceMenuItemDTOV1 eximWorkspaceMenuItemDTOV1);

    default void recursiveMappingTreeStructure(List<EximWorkspaceMenuItemDTOV1> items, Workspace workspace, MenuItem parent,
            List<MenuItem> mappedItems) {
        int position = 0;
        for (EximWorkspaceMenuItemDTOV1 item : items) {
            if (item != null) {
                MenuItem menu = map(item);
                updateMenu(menu, position, workspace, parent);
                mappedItems.add(menu);
                position++;

                if (item.getChildren() == null || item.getChildren().isEmpty()) {
                    continue;
                }

                recursiveMappingTreeStructure(item.getChildren(), workspace, menu, mappedItems);
            }
        }
    }

    default MenuItem updateMenu(MenuItem menuItem, int position, Workspace workspace,
            MenuItem parent) {
        menuItem.setWorkspace(workspace);
        menuItem.setWorkspaceName(workspace.getName());
        menuItem.setPosition(position);
        menuItem.setParent(parent);
        return menuItem;
    }

    default MenuSnapshotDTOV1 mapTree(Collection<MenuItem> entities) {
        MenuSnapshotDTOV1 dto = new MenuSnapshotDTOV1();
        dto.setCreated(OffsetDateTime.now());
        dto.setId(UUID.randomUUID().toString());
        if (entities.isEmpty()) {
            dto.getMenu().setMenuItems(new ArrayList<>());
            return dto;
        }

        var parentChildrenMap = entities.stream()
                .collect(Collectors
                        .groupingBy(menuItem -> menuItem.getParent() == null ? "TOP" : menuItem.getParent().getKey()));
        dto.setMenu(new EximMenuStructureDTOV1());
        dto.getMenu().setMenuItems(parentChildrenMap.get("TOP").stream().map(this::map).toList());
        return dto;
    }
}
