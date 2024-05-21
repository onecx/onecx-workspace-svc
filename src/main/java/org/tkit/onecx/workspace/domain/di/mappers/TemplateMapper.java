package org.tkit.onecx.workspace.domain.di.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.workspace.domain.models.*;

import gen.org.tkit.onecx.workspace.template.di.model.*;

@Mapper
public interface TemplateMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "menuItemId", ignore = true)
    Assignment createAssignment(MenuItem menuItem, Role role);

    default List<Assignment> createAssignments(List<Role> roles, List<MenuItem> menus,
            Map<String, Set<String>> menuMap) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }

        var rolesMap = roles.stream().collect(Collectors.toMap(Role::getName, x -> x));
        List<Assignment> assignments = new ArrayList<>();

        menus.forEach(m -> menuMap.get(m.getId()).forEach(r -> {
            var role = rolesMap.get(r);
            if (role != null) {
                assignments.add(createAssignment(m, role));
            }
        }));
        return assignments;
    }

    @Mapping(target = "mandatory", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "theme", source = "themeName")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Workspace createWorkspace(TemplateWorkspaceDI dto);

    @AfterMapping
    default void afterWorkspace(TemplateWorkspaceDI dto, @MappingTarget Workspace workspace) {
        if (workspace == null) {
            return;
        }
        if (workspace.getRoles() != null) {
            workspace.getRoles().forEach(r -> r.setWorkspace(workspace));
        }
        if (workspace.getProducts() != null) {
            workspace.getProducts().forEach(r -> r.setWorkspace(workspace));
        }
        if (workspace.getSlots() != null) {
            workspace.getSlots().forEach(r -> r.setWorkspace(workspace));
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    Slot createSlot(TemplateSlotDI dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    Role createRole(TemplateRoleDI dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Product createWorkspace(TemplateProductDI dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend createWorkspace(TemplateMicrofrontendDI dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "badge", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    MenuItem createMenu(TemplateMenuItemDI dto);

    default Map<String, Set<String>> recursiveMappingTreeStructure(List<TemplateMenuItemDI> items, Workspace workspace,
            MenuItem parent,
            List<MenuItem> mappedItems) {

        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        List<MenuItem> result = new ArrayList<>();
        Map<String, Set<String>> roles = new HashMap<>();

        for (TemplateMenuItemDI item : items) {
            if (item != null) {
                MenuItem menu = createMenu(item);
                menu.setWorkspace(workspace);
                menu.setParent(parent);
                result.add(menu);

                if (item.getRoles() != null) {
                    roles.put(menu.getId(), item.getRoles());
                }
                roles.putAll(recursiveMappingTreeStructure(item.getChildren(), workspace, menu, mappedItems));
            }
        }

        // update position in the current list
        updatePosition(result);

        mappedItems.addAll(result);

        return roles;
    }

    default void updatePosition(List<MenuItem> items) {
        Map<Integer, Set<MenuItem>> pos = new TreeMap<>();
        items.forEach(c -> pos.computeIfAbsent(c.getPosition(), k -> new HashSet<>()).add(c));

        int index = 0;
        for (Map.Entry<Integer, Set<MenuItem>> entry : pos.entrySet()) {
            for (MenuItem m : entry.getValue()) {
                m.setPosition(index);
                index = index + 1;
            }
        }
    }
}
