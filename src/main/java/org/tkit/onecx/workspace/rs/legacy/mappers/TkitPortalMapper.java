package org.tkit.onecx.workspace.rs.legacy.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.ScopeDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.TkitMenuItemStructureDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface TkitPortalMapper {

    default void recursiveMappingTreeStructure(List<TkitMenuItemStructureDTO> items, Workspace workspace, MenuItem parent,
            String applicationId, List<MenuItem> mappedItems) {
        int position = 0;
        for (TkitMenuItemStructureDTO item : items) {
            if (item != null) {
                MenuItem menu = mapMenu(item);
                updateMenu(menu, position, workspace, parent, applicationId);
                mappedItems.add(menu);
                position++;

                if (item.getChildren() == null || item.getChildren().isEmpty()) {
                    continue;
                }

                recursiveMappingTreeStructure(item.getChildren(), workspace, menu, applicationId, mappedItems);
            }
        }
    }

    @Mapping(target = "external", source = "portalExit")
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "parent.id", source = "parentItemId")
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    MenuItem mapMenu(TkitMenuItemStructureDTO menuItemStructureDto);

    default void updateMenu(MenuItem menuItem, int position, Workspace workspace,
            MenuItem parent, String applicationId) {
        menuItem.setWorkspace(workspace);
        menuItem.setPosition(position);
        menuItem.setParent(parent);
        menuItem.setApplicationId(applicationId);
    }

    @ValueMapping(target = "WORKSPACE", source = "PORTAL")
    MenuItem.Scope map(ScopeDTO scope);

    default List<TkitMenuItemStructureDTO> mapToTree(List<MenuItem> menuItems, String portalId) {
        var result = new ArrayList<TkitMenuItemStructureDTO>();
        if (menuItems == null) {
            return result;
        }

        // create map of <id, menuItem>
        var map = menuItems.stream().map(this::mapWithEmptyChildren)
                .map(item -> item.portalId(portalId))
                .collect(Collectors.toMap(TkitMenuItemStructureDTO::getGuid, x -> x));

        // loop over all menu items, add parent or child in result tree
        menuItems.forEach(m -> {
            if (m.getParent() == null) {
                result.add(map.get(m.getId()));
            } else {
                var parent = map.get(m.getParent().getId());
                parent.addChildrenItem(map.get(m.getId()));
            }
        });

        return result;
    }

    @Mapping(target = "permissionObject", ignore = true)
    @Mapping(target = "portalExit", source = "external")
    @Mapping(target = "parentItemId", source = "parent.id")
    @Mapping(target = "portalId", ignore = true)
    @Mapping(target = "parentKey", source = "parent.id")
    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeChildrenItem", ignore = true)
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "guid", source = "id")
    TkitMenuItemStructureDTO mapWithEmptyChildren(MenuItem entity);

    @ValueMapping(target = "PORTAL", source = "WORKSPACE")
    ScopeDTO map(MenuItem.Scope scope);

    @Mapping(target = "removeParametersItem", ignore = true)
    @Mapping(target = "namedParameters", ignore = true)
    @Mapping(target = "removeNamedParametersItem", ignore = true)
    @Mapping(target = "parameters", ignore = true)
    RestExceptionDTO exception(String errorCode, String message);

    @Mapping(target = "removeParametersItem", ignore = true)
    @Mapping(target = "namedParameters", ignore = true)
    @Mapping(target = "removeNamedParametersItem", ignore = true)
    RestExceptionDTO exception(String errorCode, String message, List<Object> parameters);
}
