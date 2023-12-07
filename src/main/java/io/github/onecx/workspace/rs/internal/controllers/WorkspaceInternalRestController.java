package io.github.onecx.workspace.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.internal.WorkspaceInternalApi;
import gen.io.github.onecx.workspace.rs.internal.model.CreateWorkspaceRequestDTO;
import gen.io.github.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.workspace.rs.internal.model.UpdateWorkspaceRequestDTO;
import gen.io.github.onecx.workspace.rs.internal.model.WorkspaceSearchCriteriaDTO;
import io.github.onecx.workspace.domain.daos.MenuItemDAO;
import io.github.onecx.workspace.domain.daos.ProductDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import io.github.onecx.workspace.rs.internal.mappers.WorkspaceMapper;

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

    @Inject
    ProductDAO productDAO;

    @Context
    UriInfo uriInfo;

    @Override
    @Transactional
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
        // delete menu before deleting workspace
        menuDao.deleteAllMenuItemsByWorkspaceId(id);
        productDAO.deleteProductByWorkspaceId(id);

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
    public Response searchWorkspace(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO) {
        var criteria = workspaceMapper.map(workspaceSearchCriteriaDTO);
        var result = dao.findBySearchCriteria(criteria);
        return Response.ok(workspaceMapper.mapPageResult(result)).build();
    }

    @Override
    @Transactional
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTO updateWorkspaceRequestDTO) {
        Workspace workspace = dao.findById(id);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // update portalItemName for all portal's menu items
        var newWorkspaceName = updateWorkspaceRequestDTO.getWorkspaceName();
        var oldWorkspaceName = workspace.getWorkspaceName();

        if (!oldWorkspaceName.equals(newWorkspaceName)) {
            menuDao.updateMenuItems(newWorkspaceName, oldWorkspaceName, updateWorkspaceRequestDTO.getBaseUrl());
        }

        workspaceMapper.update(updateWorkspaceRequestDTO, workspace);
        dao.update(workspace);

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
