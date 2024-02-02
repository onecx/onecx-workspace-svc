package org.tkit.onecx.workspace.domain.di.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Microfrontend;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.di.workspace.v1.model.MenuItemStructureDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.MicrofrontendDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.ProductDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.WorkspaceImportDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class WorkspaceDataImportMapperV1 {

    @Inject
    ObjectMapper mapper;

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
    @Mapping(target = "subjectLink", source = "subjectLinks")
    @Mapping(target = "imageUrl", source = "imageUrls")
    public abstract Workspace createWorkspace(WorkspaceImportDTOV1 workspaceDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract Product createWorkspace(ProductDTOV1 productDTO);

    @Mapping(target = "id", ignore = true)
    public abstract Microfrontend createWorkspace(MicrofrontendDTOV1 mfeDTO);

    public String map(List<String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return String.join(",", value);
    }

    public Set<String> map(String roles) {
        if (roles != null && !roles.isBlank()) {
            String[] values = roles.split(",");
            return new HashSet<>(Arrays.asList(values));
        } else
            return new HashSet<>();
    }

    public String map(Set<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            return roles.stream().map(Object::toString).collect(Collectors.joining(","));
        } else
            return "";
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "badge", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "modificationCount", ignore = true, defaultValue = "0")
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract MenuItem mapMenu(MenuItemStructureDTOV1 menuItemStructureDto);

}
