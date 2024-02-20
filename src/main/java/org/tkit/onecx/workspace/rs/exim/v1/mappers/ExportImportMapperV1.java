package org.tkit.onecx.workspace.rs.exim.v1.mappers;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.exim.v1.model.*;

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
    @Mapping(target = "subjectLink", source = "subjectLinks")
    @Mapping(target = "imageUrl", source = "imageUrls")
    @Mapping(target = "roles", ignore = true)
    Workspace create(EximWorkspaceDTOV1 workspaceDTO);

    @Mapping(target = "removeWorkspacesItem", ignore = true)
    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "workspaces", source = "workspaces")
    ImportWorkspaceResponseDTOV1 create(WorkspaceSnapshotDTOV1 request,
            Map<String, ImportResponseStatusDTOV1> workspaces);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "subjectLinks", source = "subjectLink")
    @Mapping(target = "imageUrls", source = "imageUrl")
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
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "parent.id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
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

    default Set<String> map(String roles) {
        if (roles != null && !roles.isBlank()) {
            String[] values = roles.split(",");
            return new HashSet<>(Arrays.asList(values));
        } else
            return new HashSet<>();
    }

    default String map(Set<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            return roles.stream().map(Object::toString).collect(Collectors.joining(","));
        } else
            return "";
    }
}
