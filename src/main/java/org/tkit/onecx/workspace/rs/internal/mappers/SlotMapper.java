package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.workspace.domain.models.Slot;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface SlotMapper {

    default Slot create(CreateSlotRequestDTO dto, Workspace workspace) {
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
    @Mapping(target = "components", ignore = true)
    Slot create(CreateSlotRequestDTO dto);

    @Mapping(target = "removeComponentsItem", ignore = true)
    SlotDTO map(Slot data);

    default WorkspaceSlotsDTO create(List<Slot> slots) {
        return new WorkspaceSlotsDTO().slots(map(slots));
    }

    List<SlotDTO> map(List<Slot> data);

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
    void update(UpdateSlotRequestDTO dto, @MappingTarget Slot slot);
}
