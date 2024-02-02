package io.github.onecx.workspace.rs.internal.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import io.github.onecx.workspace.domain.models.Workspace;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Workspace create(CreateWorkspaceRequestDTO dto);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(UpdateWorkspaceRequestDTO dto, @MappingTarget Workspace workspace);

    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO mapPageResult(PageResult<Workspace> page);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "subjectLinks", source = "subjectLink")
    @Mapping(target = "imageUrls", source = "imageUrl")
    @Mapping(target = "removeWorkspaceRolesItem", ignore = true)
    WorkspaceDTO map(Workspace data);

    default Set<String> map(String roles) {
        if (roles != null && !roles.isBlank()) {
            String[] values = roles.split(",");
            Set<String> hashSet = new HashSet<>(Arrays.asList(values));
            return hashSet;
        } else
            return new HashSet<>();
    }

    default String map(Set<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            String str = roles.stream().map(Object::toString).collect(Collectors.joining(","));
            return str;
        } else
            return "";
    }

}
