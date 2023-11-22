package io.github.onecx.workspace.rs.internal.controllers;

import gen.io.github.onecx.workspace.rs.internal.WorkspaceInternalApi;
import gen.io.github.onecx.workspace.rs.internal.model.CreateWorkspaceRequestDTO;
import gen.io.github.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.workspace.rs.internal.model.UpdateWorkspaceRequestDTO;
import gen.io.github.onecx.workspace.rs.internal.model.WorkspaceSearchCriteriaDTO;
import io.github.onecx.workspace.domain.daos.MenuItemDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import io.github.onecx.workspace.rs.internal.mappers.WorkspaceMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

@Path("/internal/workspaces")
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
    MenuItemDAO menuDao;

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
    public Response deleteWorkspace(String id) {
        // delete menu before deleting workspace
        menuDao.deleteAllMenuItemsByWorkspaceId(id);

        dao.deleteQueryById(id);
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
    public Response getWorkspaces(Integer pageNumber, Integer pageSize) {

        return null;
    }

    @Override
    public Response searchWorkspace(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO) {
        var criteria = workspaceMapper.map(workspaceSearchCriteriaDTO);
        var result = dao.findBySearchCriteria(criteria);
        return Response.ok(workspaceMapper.mapPageResult(result)).build();
    }

    @Override
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTO updateWorkspaceRequestDTO) {
        Workspace item = dao.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        workspaceMapper.update(updateWorkspaceRequestDTO, item);
        dao.update(item);
        return Response.noContent().build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
