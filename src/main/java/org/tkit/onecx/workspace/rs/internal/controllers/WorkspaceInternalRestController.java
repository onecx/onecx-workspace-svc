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
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.domain.services.WorkspaceService;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.WorkspaceMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.WorkspaceInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class WorkspaceInternalRestController implements WorkspaceInternalApi {

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Inject
    WorkspaceMapper workspaceMapper;

    @Inject
    WorkspaceDAO dao;

    @Inject
    WorkspaceService service;

    @Context
    UriInfo uriInfo;

    @Override
    public Response createWorkspace(CreateWorkspaceRequestDTO createWorkspaceRequestDTO) {
        var workspace = workspaceMapper.create(createWorkspaceRequestDTO);
        workspace = dao.create(workspace);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(workspace.getId()).build())
                .entity(workspaceMapper.map(workspace))
                .build();
    }

    @Override
    @Transactional
    public Response deleteWorkspace(String id) {
        var workspace = dao.findById(id);
        if (workspace == null) {
            return Response.noContent().build();
        }
        service.deleteWorkspace(workspace);
        return Response.noContent().build();
    }

    @Override
    public Response getWorkspace(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(workspaceMapper.map(item)).build();
    }

    @Override
    public Response findWorkspaceByName(String name) {
        var item = dao.findByName(name);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(workspaceMapper.map(item)).build();
    }

    @Override
    public Response searchWorkspace(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO) {
        var criteria = workspaceMapper.map(workspaceSearchCriteriaDTO);
        var result = dao.findBySearchCriteria(criteria);
        return Response.ok(workspaceMapper.mapPageResult(result)).build();
    }

    @Override
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTO updateWorkspaceRequestDTO) {
        Workspace workspace = dao.findById(id);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        workspaceMapper.update(updateWorkspaceRequestDTO, workspace);
        workspace = dao.update(workspace);

        return Response.ok(workspaceMapper.map(workspace)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }
}
