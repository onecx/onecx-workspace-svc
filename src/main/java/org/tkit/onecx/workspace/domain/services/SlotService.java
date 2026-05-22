package org.tkit.onecx.workspace.domain.services;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.SlotDAO;
import org.tkit.onecx.workspace.domain.models.Component;
import org.tkit.onecx.workspace.domain.models.Slot;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.rs.internal.mappers.SlotMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateSlotRequestDTO;

@ApplicationScoped
public class SlotService {

    @Inject
    SlotDAO dao;

    @Inject
    SlotMapper mapper;

    @Transactional
    public Slot updateWithExistingComponents(Slot slot, List<Component> components) {
        slot.setComponents(new ArrayList<>());
        slot = dao.update(slot);
        slot.setComponents(components);
        return dao.update(slot);
    }

    @Transactional
    public Slot updateWithoutComponents(Slot slot, List<Component> components) {
        slot.setComponents(components);
        return dao.update(slot);
    }

    public Slot createOrUpdateSlot(Workspace workspace, UpdateSlotRequestDTO updateSlotRequestDTO) {
        var existingSlots = dao.findSlotsByWorkspaceId(workspace.getId());
        var matchingSlot = existingSlots.stream().filter(slot -> slot.getName().equals(updateSlotRequestDTO.getName()))
                .findFirst();
        Slot updatedOrCreatedSlot;

        //slot exists
        if (matchingSlot.isPresent()) {
            // slot components are empty, delete the slot
            if (updateSlotRequestDTO.getComponents() == null || updateSlotRequestDTO.getComponents().isEmpty()) {
                dao.deleteById(matchingSlot.get().getId());
                return null;
            }
            if (matchingSlot.get().getComponents().isEmpty()) {
                updatedOrCreatedSlot = updateWithoutComponents(matchingSlot.get(),
                        mapper.mapComponents(updateSlotRequestDTO.getComponents()));
            } else {
                updatedOrCreatedSlot = updateWithExistingComponents(matchingSlot.get(),
                        mapper.mapComponents(updateSlotRequestDTO.getComponents()));
            }
            //slot does not exist, create new slot with components
        } else {
            updatedOrCreatedSlot = mapper.create(updateSlotRequestDTO, workspace);
            updatedOrCreatedSlot = dao.create(updatedOrCreatedSlot);
        }
        return updatedOrCreatedSlot;
    }
}
