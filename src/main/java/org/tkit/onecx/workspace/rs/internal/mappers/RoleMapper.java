package org.tkit.onecx.workspace.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.workspace.domain.criteria.RoleSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface RoleMapper {

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTO mapPage(PageResult<Role> page);

    RoleSearchCriteria map(RoleSearchCriteriaDTO dto);

    default Role create(CreateRoleRequestDTO dto, Workspace workspace) {
        var role = create(dto);
        role.setWorkspace(workspace);
        return role;
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
    Role create(CreateRoleRequestDTO dto);

    RoleDTO map(Role data);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    void update(UpdateRoleRequestDTO dto, @MappingTarget Role role);
}
