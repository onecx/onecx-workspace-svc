package org.tkit.onecx.workspace.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.criteria.AssignmentSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Assignment;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.AssignmentDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.AssignmentPageResultDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.AssignmentSearchCriteriaDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AssignmentMapper {

    @Mapping(target = "removeStreamItem", ignore = true)
    AssignmentPageResultDTO map(PageResult<Assignment> page);

    AssignmentSearchCriteria map(AssignmentSearchCriteriaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "menuItemId", ignore = true)
    Assignment create(Role role, MenuItem menuItem);

    @Mapping(target = "workspaceId", source = "menuItem.workspaceId")
    AssignmentDTO map(Assignment data);
}
