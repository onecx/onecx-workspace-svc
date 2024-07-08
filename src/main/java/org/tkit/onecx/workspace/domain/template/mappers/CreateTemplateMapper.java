package org.tkit.onecx.workspace.domain.template.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.template.create.model.*;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface CreateTemplateMapper {

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

    default List<Product> createProducts(List<ProductTemplateDTO> dto, Workspace workspace) {
        if (dto == null) {
            return List.of();
        }
        return dto.stream().map(x -> createProduct(x, workspace)).toList();
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "baseUrl", source = "dto.baseUrl")
    @Mapping(target = "workspaceId", ignore = true)
    Product createProduct(ProductTemplateDTO dto, Workspace workspace);

    List<Microfrontend> createMicrofrontends(List<MicrofrontendTemplateDTO> dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend createMicrofrontend(MicrofrontendTemplateDTO dto);

    default List<Slot> createSlots(List<SlotTemplateDTO> dto, Workspace workspace) {
        if (dto == null) {
            return List.of();
        }
        return dto.stream().map(x -> createSlot(x, workspace)).toList();
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "workspaceId", ignore = true)
    Slot createSlot(SlotTemplateDTO dto, Workspace workspace);

    default List<Role> createRoles(List<RoleTemplateDTO> dto, Workspace workspace) {
        if (dto == null) {
            return List.of();
        }
        return dto.stream().map(x -> createRole(workspace, x))
                .toList();
    }

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    Role createRole(Workspace workspace, RoleTemplateDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "name")
    Role createRole(Workspace workspace, String name);

    default Map<String, Set<String>> recursiveMappingTreeStructure(List<MenuItemTemplateDTO> items, Workspace workspace,
            MenuItem parent, List<MenuItem> mappedItems) {

        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        List<MenuItem> result = new ArrayList<>();
        Map<String, Set<String>> roles = new HashMap<>();

        for (MenuItemTemplateDTO item : items) {
            if (item != null) {
                MenuItem menu = map(item);
                menu.setWorkspace(workspace);
                menu.setParent(parent);
                result.add(menu);

                if (item.getRoles() != null) {
                    roles.put(menu.getId(), item.getRoles());
                } else {
                    roles.put(menu.getId(), Set.of());
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
    @Mapping(target = "parentId", ignore = true)
    MenuItem map(MenuItemTemplateDTO dto);
}
