package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.criteria.MenuItemSearchCriteria;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MenuItemMapper {

    @Mapping(target = "removeStreamItem", ignore = true)
    MenuItemPageResultDTO mapPage(PageResult<MenuItem> page);

    @Mapping(target = "parentItemId", source = "parentId")
    MenuItemResultDTO mapPageItem(MenuItem item);

    MenuItemLoadCriteria map(MenuStructureSearchCriteriaDTO dto);

    MenuItemSearchCriteria map(MenuItemSearchCriteriaDTO dto);

    default MenuItem update(MenuItem menu, UpdateMenuItemParentRequestDTO dto) {
        menu.setModificationCount(dto.getModificationCount());
        menu.setPosition(dto.getPosition());
        return menu;
    }

    default MenuItem create(CreateMenuItemDTO dto, Workspace workspace, MenuItem parentItem) {
        var result = create(dto);
        result.setWorkspace(workspace);
        result.setParent(parentItem);
        return result;
    }

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
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
    @Mapping(target = "parentId", ignore = true)
    MenuItem create(CreateMenuItemDTO dto);

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "name", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "url", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "key", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateMenuItemRequestDTO menuItemDetailsDto, @MappingTarget MenuItem entity);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "parentItemId", source = "parentId")
    MenuItemDTO map(MenuItem item);

    default MenuItemStructureDTO mapTree(Collection<MenuItem> entities) {
        MenuItemStructureDTO dto = new MenuItemStructureDTO();
        if (entities.isEmpty()) {
            dto.setMenuItems(new ArrayList<>());
            return dto;
        }

        var parentChildrenMap = entities.stream()
                .collect(Collectors
                        .groupingBy(menuItem -> menuItem.getParent() == null ? "TOP" : menuItem.getParent().getId()));

        dto.setMenuItems(parentChildrenMap.get("TOP").stream().map(this::mapTreeItem).toList());
        return dto;
    }

    List<WorkspaceMenuItemDTO> mapCollection(Collection<MenuItem> entities);

    @Mapping(target = "parentItemId", source = "parent.id")
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    WorkspaceMenuItemDTO mapTreeItem(MenuItem entity);

    default void recursiveMappingTreeStructure(List<WorkspaceMenuItemDTO> items, Workspace workspace, MenuItem parent,
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
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "parent.id", source = "parentItemId")
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    MenuItem mapMenu(WorkspaceMenuItemDTO menuItemStructureDto);

    default void updateMenu(MenuItem menuItem, int position, Workspace workspace,
            MenuItem parent) {
        menuItem.setWorkspace(workspace);
        menuItem.setPosition(position);
        menuItem.setParent(parent);
    }

    default MenuItemStructureDTO mapTreeByRoles(List<MenuItem> entities, Map<String, Set<String>> assignments) {
        MenuItemStructureDTO dto = new MenuItemStructureDTO();

        if (entities.isEmpty()) {
            dto.setMenuItems(new ArrayList<>());
            return dto;
        }

        List<MenuItem> filteredEntities = entities.stream()
                .filter(menuItem -> assignments.containsKey(menuItem.getId()))
                .toList();

        if (filteredEntities.isEmpty()) {
            dto.setMenuItems(new ArrayList<>());
            return dto;
        }

        var parentChildrenMap = filteredEntities.stream()
                .collect(
                        Collectors.groupingBy(menuItem -> menuItem.getParent() == null ? "TOP" : menuItem.getParent().getId()));

        List<WorkspaceMenuItemDTO> topMenuItems = parentChildrenMap.getOrDefault("TOP", new ArrayList<>()).stream()
                .map(menuItem -> mapTreeItemWithChildren(menuItem, parentChildrenMap))
                        .toList();

        dto.setMenuItems(topMenuItems);
        return dto;
    }

    private WorkspaceMenuItemDTO mapTreeItemWithChildren(MenuItem menuItem, Map<String, List<MenuItem>> parentChildrenMap) {
        WorkspaceMenuItemDTO dto = mapTreeItem(menuItem);

        List<MenuItem> children = parentChildrenMap.get(menuItem.getId());
        if (children != null) {
            List<WorkspaceMenuItemDTO> childDTOs = children.stream()
                    .map(child -> mapTreeItemWithChildren(child, parentChildrenMap))
                    .toList();
            dto.setChildren(childDTOs);
        } else {
            dto.setChildren(new ArrayList<>());
        }

        return dto;
    }
}
