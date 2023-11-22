package io.github.onecx.workspace.rs.internal.mappers;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import io.github.onecx.workspace.domain.models.SubjectLink;
import io.github.onecx.workspace.domain.models.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

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
    WorkspaceDTO map(Workspace data);
}
