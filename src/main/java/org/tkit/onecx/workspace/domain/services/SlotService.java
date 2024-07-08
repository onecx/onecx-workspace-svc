package org.tkit.onecx.workspace.domain.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.SlotDAO;
import org.tkit.onecx.workspace.domain.models.Component;
import org.tkit.onecx.workspace.domain.models.Slot;

@ApplicationScoped
public class SlotService {

    @Inject
    SlotDAO dao;

    @Transactional
    public Slot update(Slot slot, List<Component> components) {
        slot.setComponents(null);
        slot = dao.update(slot);
        slot.setComponents(components);
        return dao.update(slot);
    }
}
