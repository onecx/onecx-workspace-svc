package io.github.onecx.workspace.rs.internal.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.domain.models.MenuItem;
import io.github.onecx.workspace.domain.models.Workspace;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class MenuItemMapper {

    public abstract List<MenuItemDTO> map(Stream<MenuItem> items);

    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract MenuItem create(CreateMenuItemDTO dto);

    @Mapping(target = "workspace", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract void update(MenuItemDTO menuItemDetailsDto, @MappingTarget MenuItem entity);

    public abstract List<MenuItemDTO> mapList(List<MenuItem> items);

    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "parentItemId", source = "parent.id")
    public abstract MenuItemDTO map(MenuItem item);

    public List<String> mapStringToList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(","));
    }

    public String mapListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    public WorkspaceMenuItemStructrueDTO mapTree(Collection<MenuItem> entities) {
        WorkspaceMenuItemStructrueDTO dto = new WorkspaceMenuItemStructrueDTO();
        if (entities.isEmpty()) {
            dto.setMenuItems(new ArrayList<>());
            return dto;
        }

        var parentChildrenMap = entities.stream()
                .collect(Collectors
                        .groupingBy(menuItem -> menuItem.getParent() == null ? "TOP" : menuItem.getParent().getKey()));

        dto.setMenuItems(parentChildrenMap.get("TOP").stream().map(this::mapTreeItem).toList());
        return dto;
    }

    public abstract List<WorkspaceMenuItemDTO> mapCollection(Collection<MenuItem> entities);

    @Mapping(target = "parentItemId", source = "parent.id")
    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    public abstract WorkspaceMenuItemDTO mapTreeItem(MenuItem entity);

    public void recursiveMappingTreeStructure(List<WorkspaceMenuItemDTO> items, Workspace workspace, MenuItem parent,
            List<MenuItem> mappedItems) {

        int position = 0;
        for (WorkspaceMenuItemDTO item : items) {
            if (item != null) {
                MenuItem menu = mapMenu(item);
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "parent.id", source = "parentItemId")
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract MenuItem mapMenu(WorkspaceMenuItemDTO menuItemStructureDto);

    public void updateMenu(MenuItem menuItem, int position, Workspace workspace,
            MenuItem parent) {
        menuItem.setWorkspace(workspace);
        menuItem.setWorkspaceName(workspace.getName());
        menuItem.setPosition(position);
        menuItem.setParent(parent);
    }
}
