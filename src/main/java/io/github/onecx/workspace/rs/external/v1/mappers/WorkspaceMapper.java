package io.github.onecx.workspace.rs.external.v1.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceDTOV1;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspacePageResultDTOV1;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.github.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import io.github.onecx.workspace.domain.models.Workspace;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTOV1 mapAbstractList(PageResult<Workspace> page);

    @Mapping(target = "name", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTOV1 criteria);

    @Mapping(target = "subjectLinks", ignore = true)
    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "removeWorkspaceRolesItem", ignore = true)
    WorkspaceDTOV1 map(Workspace workspace);

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
