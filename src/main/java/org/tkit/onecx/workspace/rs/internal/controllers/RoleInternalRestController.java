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
import org.tkit.onecx.workspace.domain.daos.RoleDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.RoleMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.RoleInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateRoleRequestDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateRoleRequestDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class RoleInternalRestController implements RoleInternalApi {

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Inject
    RoleDAO dao;

    @Inject
    RoleMapper mapper;

    @Context
    UriInfo uriInfo;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Override
    public Response deleteWorkspaceRole(String roleId) {
        dao.deleteQueryById(roleId);
        return Response.noContent().build();
    }

    @Override
    public Response getWorkspaceRole(String roleId) {
        var role = dao.findById(roleId);
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(role)).build();
    }

    @Override
    public Response updateWorkspaceRole(String roleId, UpdateRoleRequestDTO updateRoleRequestDTO) {
        var role = dao.findById(roleId);
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        mapper.update(updateRoleRequestDTO, role);
        dao.update(role);
        return Response.ok(mapper.map(role)).build();
    }

    @Override
    public Response createRole(CreateRoleRequestDTO createRoleRequestDTO) {
        var workspace = workspaceDAO.getReference(createRoleRequestDTO.getWorkspaceId());
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var role = mapper.create(createRoleRequestDTO, workspace);
        role = dao.create(role);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(role.getId()).build())
                .entity(mapper.map(role))
                .build();
    }

    @Override
    public Response searchRoles(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        var criteria = mapper.map(roleSearchCriteriaDTO);
        var result = dao.findByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
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
