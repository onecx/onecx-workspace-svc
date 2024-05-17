package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.ArrayList;
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

    default List<Slot> createList(CreateSlotRequestDTO createSlotRequestDTO, Workspace workspace) {
        List<Slot> slots = new ArrayList<>();
        createSlotRequestDTO.getSlots().forEach(createSlotDTO -> slots.add(map(createSlotDTO, workspace)));
        return slots;
    }

    default Slot map(CreateSlotDTO createSlotDTO, Workspace workspace) {
        var slot = map(createSlotDTO);
        slot.setWorkspace(workspace);
        return slot;
    }

    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "components", ignore = true)
    Slot map(CreateSlotDTO createSlotDTO);
}
