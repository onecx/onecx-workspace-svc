package org.tkit.onecx.workspace.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.RoleInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateRoleRequestDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateRoleRequestDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class RoleInternalRestController implements RoleInternalApi {
    @Override
    public Response createWorkspaceRole(String id, CreateRoleRequestDTO createRoleRequestDTO) {
        return null;
    }

    @Override
    public Response deleteWorkspaceRole(String id, String roleId) {
        return null;
    }

    @Override
    public Response getWorkspaceRole(String id, String roleId) {
        return null;
    }

    @Override
    public Response getWorkspaceRoles(String id) {
        return null;
    }

    @Override
    public Response updateWorkspaceRole(String id, String roleId, UpdateRoleRequestDTO updateRoleRequestDTO) {
        return null;
    }
}
