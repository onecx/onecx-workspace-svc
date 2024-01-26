package io.github.onecx.workspace.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.external.v1.WorkspaceExternalV1Api;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.rs.external.v1.mappers.WorkspaceMapper;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class WorkspaceExternalV1RestController implements WorkspaceExternalV1Api {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    WorkspaceMapper mapper;

    @Override
    public Response searchWorkspaces(WorkspaceSearchCriteriaDTOV1 workspaceSearchCriteriaDTOV1) {
        var criteria = mapper.map(workspaceSearchCriteriaDTOV1);
        var result = workspaceDAO.findBySearchCriteria(criteria);
        return Response.ok(mapper.mapAbstractList(result)).build();
    }
}
