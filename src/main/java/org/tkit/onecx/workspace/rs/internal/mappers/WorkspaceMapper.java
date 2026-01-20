package org.tkit.onecx.workspace.rs.internal.mappers;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.rs.common.mappers.WorkspaceTranslationKeyMapper;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class, WorkspaceTranslationKeyMapper.class })
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
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "i18n", source = "i18n", qualifiedByName = "toEntityI18n")
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
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "i18n", source = "i18n", qualifiedByName = "toEntityI18n")
    void update(UpdateWorkspaceRequestDTO dto, @MappingTarget Workspace workspace);

    @Mapping(target = "baseUrl", ignore = true)
    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO mapPageResult(PageResult<Workspace> page);

    @Mapping(target = "i18n", source = "i18n", qualifiedByName = "toDtoI18n")
    @Mapping(target = "removeI18nItem", ignore = true)
    WorkspaceAbstractDTO mapAbstract(Workspace workspace);

    @Mapping(target = "i18n", source = "i18n", qualifiedByName = "toDtoI18n")
    @Mapping(target = "removeI18nItem", ignore = true)
    WorkspaceDTO map(Workspace data);

}
