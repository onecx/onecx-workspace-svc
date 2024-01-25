package io.github.onecx.workspace.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

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
}
