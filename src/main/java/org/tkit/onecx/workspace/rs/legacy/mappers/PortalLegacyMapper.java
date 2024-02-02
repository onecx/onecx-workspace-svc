package org.tkit.onecx.workspace.rs.legacy.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.enums.Scope;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuItemStructureDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.ScopeDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface PortalLegacyMapper {

    default List<MenuItemStructureDTO> mapToTree(List<MenuItem> menuItems) {
        List<MenuItemStructureDTO> result = new ArrayList<>();
        if (menuItems == null) {
            return result;
        }

        // create map of <id, menuItem>
        var map = menuItems.stream().map(this::mapWithEmptyChildren)
                .collect(Collectors.toMap(MenuItemStructureDTO::getId, x -> x));

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

    @Mapping(target = "portalExit", source = "workspaceExit")
    @Mapping(target = "parentItemId", source = "parent.id")
    @Mapping(target = "portalId", source = "workspaceName")
    @Mapping(target = "parentKey", source = "parent.id")
    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeChildrenItem", ignore = true)
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "children", ignore = true)
    MenuItemStructureDTO mapWithEmptyChildren(MenuItem entity);

    @ValueMapping(target = "PORTAL", source = "WORKSPACE")
    ScopeDTO map(Scope scope);

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
