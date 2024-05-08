package org.tkit.onecx.workspace.domain.di.mappers;

import java.util.*;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.di.workspace.v1.model.*;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface WorkspaceDataImportMapperV1 {

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
    @Mapping(target = "slots", ignore = true)
    Workspace createWorkspace(WorkspaceImportDTOV1 workspaceDTO);

    @AfterMapping
    default void afterWorkspace(WorkspaceImportDTOV1 dto, @MappingTarget Workspace workspace) {
        if (workspace == null) {
            return;
        }
        if (workspace.getRoles() == null) {
            return;
        }
        workspace.getRoles().forEach(r -> r.setWorkspace(workspace));
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
    Role createRole(WorkspaceRoleDTOV1 dto);

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
    Product createWorkspace(ProductDTOV1 productDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend createWorkspace(MicrofrontendDTOV1 mfeDTO);

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
    MenuItem mapMenu(MenuItemStructureDTOV1 menuItemStructureDto);

    default Map<String, Set<String>> recursiveMappingTreeStructure(List<MenuItemStructureDTOV1> items, Workspace workspace,
            MenuItem parent,
            List<MenuItem> mappedItems) {

        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        List<MenuItem> result = new ArrayList<>();
        Map<String, Set<String>> roles = new HashMap<>();

        for (MenuItemStructureDTOV1 item : items) {
            if (item != null) {
                MenuItem menu = mapMenu(item);
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
