package org.tkit.onecx.workspace.rs.admin.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.admin.v1.model.CreateWorkspaceRequestDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.UpdateWorkspaceDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspaceAbstractDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspaceDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspacePageResultDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspaceSearchCriteriaDTOAdminV1;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceAdminMapperV1 {

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "i18n", ignore = true)
    Workspace create(CreateWorkspaceRequestDTOAdminV1 dto);

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
    @Mapping(target = "i18n", ignore = true)
    void update(UpdateWorkspaceDTOAdminV1 dto, @MappingTarget Workspace workspace);

    @Mapping(target = "baseUrl", ignore = true)
    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTOAdminV1 dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTOAdminV1 mapPageResult(PageResult<Workspace> page);

    WorkspaceAbstractDTOAdminV1 mapAbstract(Workspace workspace);

    WorkspaceDTOAdminV1 map(Workspace data);

}
