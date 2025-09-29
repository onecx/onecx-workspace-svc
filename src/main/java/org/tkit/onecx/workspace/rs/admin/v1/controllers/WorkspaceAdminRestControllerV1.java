package org.tkit.onecx.workspace.rs.admin.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.domain.services.WorkspaceService;
import org.tkit.onecx.workspace.domain.template.services.CreateTemplateService;
import org.tkit.onecx.workspace.rs.admin.v1.mappers.AdminExceptionMapperV1;
import org.tkit.onecx.workspace.rs.admin.v1.mappers.WorkspaceAdminMapperV1;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.admin.v1.WorkspaceAdminApi;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.CreateWorkspaceRequestDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.ProblemDetailResponseDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.UpdateWorkspaceRequestDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspaceSearchCriteriaDTOAdminV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
class WorkspaceAdminRestControllerV1 implements WorkspaceAdminApi {

    @Inject
    AdminExceptionMapperV1 exceptionMapper;

    @Inject
    WorkspaceAdminMapperV1 workspaceMapper;

    @Inject
    WorkspaceDAO dao;

    @Inject
    WorkspaceService service;

    @Inject
    CreateTemplateService createTemplateService;

    @Override
    public Response createWorkspace(CreateWorkspaceRequestDTOAdminV1 createWorkspaceRequestDTOAdminV1) {
        var workspace = workspaceMapper.create(createWorkspaceRequestDTOAdminV1);
        var template = createTemplateService.createTemplate(workspace);

        workspace = service.createWorkspace(workspace, template);
        return Response.status(Response.Status.CREATED)
                .entity(workspaceMapper.map(workspace))
                .build();
    }

    @Override
    public Response deleteWorkspace(String id) {
        service.deleteWorkspace(id);
        return Response.noContent().build();
    }

    @Override
    public Response getWorkspaceById(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(workspaceMapper.map(item)).build();
    }

    @Override
    public Response searchWorkspacesByCriteria(WorkspaceSearchCriteriaDTOAdminV1 workspaceSearchCriteriaDTOAdminV1) {
        var criteria = workspaceMapper.map(workspaceSearchCriteriaDTOAdminV1);
        var result = dao.findBySearchCriteria(criteria);
        return Response.ok(workspaceMapper.mapPageResult(result)).build();
    }

    @Override
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTOAdminV1 updateWorkspaceRequestDTOAdminV1) {
        Workspace workspace = dao.findById(id);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        workspaceMapper.update(updateWorkspaceRequestDTOAdminV1.getResource(), workspace);
        workspace = dao.update(workspace);

        return Response.ok(workspaceMapper.map(workspace)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOAdminV1> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOAdminV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOAdminV1> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

}
