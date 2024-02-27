package org.tkit.onecx.workspace.rs.user.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.models.MenuItem;

import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuItemDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuStructureDTO;

@Mapper
public interface UserMenuMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    UserWorkspaceMenuItemDTO mapTreeItem(MenuItem entity);

    default UserWorkspaceMenuStructureDTO empty(String workspaceName) {
        return new UserWorkspaceMenuStructureDTO().workspaceName(workspaceName).menu(List.of());
    }

    default UserWorkspaceMenuStructureDTO mapTree(String workspaceName, Collection<MenuItem> entities,
            Map<String, Set<String>> mapping, Set<String> roles) {
        UserWorkspaceMenuStructureDTO dto = empty(workspaceName);
        if (entities.isEmpty()) {
            return dto;
        }

        var items = entities.stream().filter(m -> m.getParentId() == null).collect(Collectors.toSet());
        items = filterMenu(items, mapping, roles);
        if (items.isEmpty()) {
            return dto;
        }

        return dto.menu(items.stream().map(this::mapTreeItem).toList());
    }

    default Set<MenuItem> filterMenu(Set<MenuItem> items, Map<String, Set<String>> mapping, Set<String> roles) {
        Set<MenuItem> tmp = new HashSet<>(items);
        tmp.forEach(m -> {
            var mr = mapping.get(m.getId());
            if (mr == null || mr.stream().noneMatch(roles::contains)) {
                items.remove(m);
            } else {
                filterChildren(m, mapping, roles);
            }
        });

        return items;
    }

    default void filterChildren(MenuItem menuItem, Map<String, Set<String>> mapping, Set<String> roles) {
        Set<MenuItem> items = new HashSet<>(menuItem.getChildren());
        items.forEach(child -> {
            var mr = mapping.get(child.getId());
            if (mr != null && mr.stream().noneMatch(roles::contains)) {
                menuItem.getChildren().remove(child);
            } else {
                if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                    filterChildren(child, mapping, roles);
                }
            }
        });
    }

}
