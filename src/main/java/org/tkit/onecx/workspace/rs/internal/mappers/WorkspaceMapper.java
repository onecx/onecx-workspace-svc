package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

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
    @Mapping(target = "roles", ignore = true)
    Workspace create(CreateWorkspaceRequestDTO dto);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "modificationCount", source = "modificationCount")
    void update(UpdateWorkspaceRequestDTO dto, @MappingTarget Workspace workspace);

    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO mapPageResult(PageResult<Workspace> page);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "subjectLinks", source = "subjectLink")
    @Mapping(target = "imageUrls", source = "imageUrl")
    WorkspaceDTO map(Workspace data);

    default Set<String> map(String roles) {
        if (roles != null && !roles.isBlank()) {
            String[] values = roles.split(",");
            return new HashSet<>(Arrays.asList(values));
        } else
            return new HashSet<>();
    }

    default String map(Set<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            return roles.stream().map(Object::toString).collect(Collectors.joining(","));
        } else
            return "";
    }

}
