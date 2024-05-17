package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class SlotLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, CreateSlotRequestDTO.class,
                        x -> CreateSlotRequestDTO.class.getSimpleName() + "["
                                + ((CreateSlotRequestDTO) x).getSlots().size() + ","
                                + ((CreateSlotRequestDTO) x).getWorkspaceId() + "]"),
                item(10, UpdateSlotRequestDTO.class,
                        x -> UpdateSlotRequestDTO.class.getSimpleName() + "[" + ((UpdateSlotRequestDTO) x).getName()
                                + "]"));
    }

}
