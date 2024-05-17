package org.tkit.onecx.workspace.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.SlotDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Slot;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.SlotMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.SlotInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateSlotRequestDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateSlotRequestDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class SlotInternalRestController implements SlotInternalApi {

    @Context
    UriInfo uriInfo;

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Inject
    SlotMapper mapper;

    @Inject
    SlotDAO dao;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Override
    public Response createSlot(CreateSlotRequestDTO createSlotRequestDTO) {
        var workspace = workspaceDAO.findById(createSlotRequestDTO.getWorkspaceId());
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var slots = mapper.createList(createSlotRequestDTO, workspace);

        var existingSlots = dao.findSlotsByWorkspaceId(workspace.getId()).stream().map(Slot::getName).toList();
        slots.removeIf(slot -> existingSlots.contains(slot.getName()));
        var createdSlots = dao.create(slots).toList();
        if (!createdSlots.isEmpty()) {
            return Response.status(Response.Status.CREATED)
                    .entity(mapper.map(createdSlots))
                    .build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

    }

    @Override
    public Response deleteSlotById(String slotId) {
        dao.deleteById(slotId);
        return Response.noContent().build();
    }

    @Override
    public Response getSlotById(String slotId) {
        var slot = dao.findById(slotId);
        if (slot == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(slot)).build();
    }

    @Override
    public Response getSlotsForWorkspace(String id) {
        var items = dao.findSlotsByWorkspaceId(id);
        return Response.ok(mapper.create(items)).build();
    }

    @Override
    public Response updateSlot(String slotId, UpdateSlotRequestDTO updateSlotRequestDTO) {
        var slot = dao.loadById(slotId);
        if (slot == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        mapper.update(updateSlotRequestDTO, slot);
        slot = dao.update(slot);
        return Response.ok(mapper.map(slot)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }
}
