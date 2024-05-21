package org.tkit.onecx.workspace.rs.exim.v1.mappers;

import java.time.OffsetDateTime;
import java.util.*;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.exim.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ExportImportMapperV1 {

    ImportMenuResponseDTOV1 create(String id, ImportResponseStatusDTOV1 status);

    default WorkspaceSnapshotDTOV1 create(Map<String, Workspace> workspaces) {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        snapshot.setCreated(OffsetDateTime.now());
        snapshot.setId(UUID.randomUUID().toString());
        snapshot.setWorkspaces(map(workspaces));
        return snapshot;
    }

    Map<String, EximWorkspaceDTOV1> map(Map<String, Workspace> data);

    @Mapping(target = "mandatory", ignore = true)
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
    @Mapping(target = "slots", ignore = true)
    Workspace create(EximWorkspaceDTOV1 workspaceDTO);

    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    Role create(EximWorkspaceRoleDTOV1 dto);

    @AfterMapping
    default void afterWorkspace(EximWorkspaceDTOV1 dto, @MappingTarget Workspace workspace) {
        if (workspace == null) {
            return;
        }
        if (workspace.getRoles() == null) {
            return;
        }
        workspace.getRoles().forEach(r -> r.setWorkspace(workspace));
    }

    @Mapping(target = "removeWorkspacesItem", ignore = true)
    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "workspaces", source = "workspaces")
    ImportWorkspaceResponseDTOV1 create(WorkspaceSnapshotDTOV1 request,
            Map<String, ImportResponseStatusDTOV1> workspaces);

    @Mapping(target = "removeProductsItem", ignore = true)
    @Mapping(target = "removeRolesItem", ignore = true)
    @Mapping(target = "removeSlotsItem", ignore = true)
    EximWorkspaceDTOV1 map(Workspace workspace);

    @Mapping(target = "removeComponentsItem", ignore = true)
    EximSlotDTOV1 map(Slot slot);

    EximComponentDTOV1 map(Component component);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    EximProductDTOV1 map(Product product);

    @Mapping(target = "appId", source = "mfeId")
    EximMicrofrontendDTOV1 map(Microfrontend microfrontend);

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
    @Mapping(target = "removeRolesItem", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "children", ignore = true)
    EximWorkspaceMenuItemDTOV1 map(MenuItem menuItem);

    default List<EximWorkspaceMenuItemDTOV1> children(Set<MenuItem> set, Map<String, Set<String>> roles) {
        if (set == null) {
            return List.of();
        }
        List<EximWorkspaceMenuItemDTOV1> list = new ArrayList<>(set.size());
        for (MenuItem item : set) {
            list.add(map(item, roles));
        }
        return list;
    }

    default EximWorkspaceMenuItemDTOV1 map(MenuItem menuItem, Map<String, Set<String>> roles) {
        var menu = map(menuItem);
        if (menu == null) {
            return null;
        }

        var r = roles.get(menuItem.getId());
        if (r != null) {
            menu.setRoles(r);
        }

        menu.setChildren(children(menuItem.getChildren(), roles));
        return menu;
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
    MenuItem map(EximWorkspaceMenuItemDTOV1 eximWorkspaceMenuItemDTOV1);

    default Map<String, Set<String>> recursiveMappingTreeStructure(List<EximWorkspaceMenuItemDTOV1> items, Workspace workspace,
            MenuItem parent,
            List<MenuItem> mappedItems) {

        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        List<MenuItem> result = new ArrayList<>();
        Map<String, Set<String>> roles = new HashMap<>();

        for (EximWorkspaceMenuItemDTOV1 item : items) {
            if (item != null) {
                MenuItem menu = map(item);
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

    default MenuSnapshotDTOV1 mapTree(Collection<MenuItem> entities, Map<String, Set<String>> roles) {
        MenuSnapshotDTOV1 dto = new MenuSnapshotDTOV1();
        dto.setCreated(OffsetDateTime.now());
        dto.setId(UUID.randomUUID().toString());
        if (entities.isEmpty()) {
            dto.getMenu().setMenuItems(new ArrayList<>());
            return dto;
        }

        var parents = entities.stream().filter(m -> m.getParentId() == null).map(m -> map(m, roles)).toList();

        dto.setMenu(new EximMenuStructureDTOV1());
        dto.getMenu().setMenuItems(parents);
        return dto;
    }

    default List<Product> create(List<EximProductDTOV1> products, Workspace workspace) {
        List<Product> newProducts = new ArrayList<>();
        products.forEach(productDTOV1 -> newProducts.add(map(productDTOV1, workspace)));
        return newProducts;
    }

    default List<Slot> createSlots(List<EximSlotDTOV1> slots, Workspace workspace) {
        List<Slot> newSlots = new ArrayList<>();
        slots.forEach(dto -> newSlots.add(map(dto, workspace)));
        return newSlots;
    }

    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "baseUrl", source = "productDTOV1.baseUrl")
    Product map(EximProductDTOV1 productDTOV1, Workspace workspace);

    @Mapping(target = "mfeId", source = "appId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend map(EximMicrofrontendDTOV1 eximMicrofrontendDTOV1);

    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    Slot map(EximSlotDTOV1 dto, Workspace workspace);

}
