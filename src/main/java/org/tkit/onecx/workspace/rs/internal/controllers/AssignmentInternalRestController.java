package org.tkit.onecx.workspace.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.AssignmentInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateAssignmentRequestDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AssignmentInternalRestController implements AssignmentInternalApi {
    @Override
    public Response createWorkspaceAssignment(String id, CreateAssignmentRequestDTO createAssignmentRequestDTO) {
        return null;
    }

    @Override
    public Response deleteAssignment(String id, String assignmentId) {
        return null;
    }

    @Override
    public Response getAssignment(String id, String assignmentId) {
        return null;
    }

    @Override
    public Response getWorkspaceAssignments(String id) {
        return null;
    }
}
