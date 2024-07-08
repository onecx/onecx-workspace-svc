package org.tkit.onecx.workspace.rs.exim.v1.mappers;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.exim.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ExportImportMapperV1 {

    default List<Image> createImages(String workspaceName, Map<String, ImageDTOV1> images) {
        if (images == null) {
            return List.of();
        }
        List<Image> result = new ArrayList<>();
        images.forEach((refType, dto) -> result.add(createImage(workspaceName, refType, dto)));
        return result;
    }

    default Image updateImage(Image image, ImageDTOV1 dto) {
        image.setImageData(dto.getImageData());
        image.setMimeType(dto.getMimeType());
        image.setLength(length(dto.getImageData()));
        return image;
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
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "length", source = "dto.imageData", qualifiedByName = "length")
    Image createImage(String refId, String refType, ImageDTOV1 dto);

    @Named("length")
    default Integer length(byte[] data) {
        if (data == null) {
            return 0;
        }
        return data.length;
    }

    ImportMenuResponseDTOV1 create(String id, ImportResponseStatusDTOV1 status);

    default WorkspaceSnapshotDTOV1 create(Map<String, Workspace> workspaces, List<Image> images, Collection<MenuItem> menus,
            Map<String, Set<String>> roles) {
        if (workspaces == null) {
            return null;
        }
        var imagesMap = createImages(images);

        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        snapshot.setCreated(OffsetDateTime.now());
        snapshot.setId(UUID.randomUUID().toString());
        snapshot.setWorkspaces(map(workspaces, imagesMap, menus, roles));
        return snapshot;
    }

    default Map<String, Map<String, ImageDTOV1>> createImages(List<Image> images) {
        if (images == null) {
            return Map.of();
        }
        Map<String, Map<String, ImageDTOV1>> result = new HashMap<>();
        images.forEach(image -> result.computeIfAbsent(image.getRefId(), k -> new HashMap<>())
                .put(image.getRefType(), createImage(image)));
        return result;
    }

    ImageDTOV1 createImage(Image image);

    default Map<String, EximWorkspaceDTOV1> map(Map<String, Workspace> data, Map<String, Map<String, ImageDTOV1>> images,
            Collection<MenuItem> menus, Map<String, Set<String>> roles) {
        if (data == null) {
            return Map.of();
        }

        Map<String, List<EximWorkspaceMenuItemDTOV1>> menuMap = new HashMap<>();
        var parents = menus.stream().filter(m -> m.getParentId() == null).toList();
        parents.forEach(m -> menuMap.computeIfAbsent(m.getWorkspaceId(), k -> new ArrayList<>()).add(map(m, roles)));

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        data.forEach((name, value) -> {
            EximWorkspaceDTOV1 dto = map(value);
            dto.setImages(images.get(name));
            dto.setMenuItems(menuMap.get(value.getId()));
            map.put(name, dto);
        });
        return map;
    }

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
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "displayName", ignore = true)
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
    @Mapping(target = "removeImagesItem", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "removeMenuItemsItem", ignore = true)
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

    static String imageId(Image image) {
        return imageId(image.getRefId(), image.getRefType());
    }

    static String imageId(String refId, String refType) {
        return refId + "#" + refType;
    }
}
