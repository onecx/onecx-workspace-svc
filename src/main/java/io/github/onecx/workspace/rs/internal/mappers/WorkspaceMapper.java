package io.github.onecx.workspace.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

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
    @Mapping(target = "stream", qualifiedByName = "mapStream")
    WorkspacePageResultDTO mapPageResult(PageResult<Workspace> page);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "version", source = "modificationCount")
    WorkspaceDTO map(Workspace data);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "subjectLinks", ignore = true)
    @Mapping(target = "version", source = "modificationCount")
    @Named("mapWithoutLazy")
    WorkspaceDTO mapWithoutLazy(Workspace data);

    @Named("mapStream")
    @IterableMapping(qualifiedByName = "mapWithoutLazy")
    List<WorkspaceDTO> mapStream(Stream<Workspace> stream);
}
