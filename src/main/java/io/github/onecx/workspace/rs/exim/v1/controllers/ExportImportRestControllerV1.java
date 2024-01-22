package io.github.onecx.workspace.rs.exim.v1.controllers;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.rs.exim.v1.WorkspaceExportImportApi;
import gen.io.github.onecx.workspace.rs.exim.v1.model.*;
import io.github.onecx.workspace.domain.daos.MenuItemDAO;
import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.MenuItem;
import io.github.onecx.workspace.rs.exim.v1.mappers.ExportImportExceptionMapperV1;
import io.github.onecx.workspace.rs.exim.v1.mappers.ExportImportMapperV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ExportImportRestControllerV1 implements WorkspaceExportImportApi {

    @Inject
    WorkspaceDAO dao;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    ExportImportExceptionMapperV1 exceptionMapper;

    @Inject
    ExportImportMapperV1 mapper;

    @Override
    public Response exportMenuByWorkspaceName(String name) {
        var menu = menuItemDAO.loadAllMenuItemsByWorkspaceName(name);
        if (menu.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.create(menu)).build();
    }

    @Override
    public Response exportWorkspaceByName(String name) {
        var workspace = dao.findByWorkspaceName(name);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.create(workspace)).build();
    }

    @Override
    public Response importMenu(String name, MenuSnapshotDTOV1 menuSnapshotDTOV1) {
        var menu = menuItemDAO.loadAllMenuItemsByWorkspaceName(name);
        var workspace = dao.findByWorkspaceName(name);
        ImportResponseDTOV1 responseDTOV1 = new ImportResponseDTOV1();

        if (workspace == null) {
            throw new ConstraintException("Workspace does not exist", MenuItemErrorKeys.WORKSPACE_DOES_NOT_EXIST, null);
        }
        if (!menu.isEmpty()) {
            menuItemDAO.deleteAllMenuItemsByWorkspaceId(workspace.getId());
            responseDTOV1.setStatus(ImportResponseStatusDTOV1.UPDATE);
        } else {
            responseDTOV1.setStatus(ImportResponseStatusDTOV1.CREATED);
        }

        List<MenuItem> items = new LinkedList<>();
        ArrayList<String> itemKeys = new ArrayList<>();
        mapper.recursiveMappingTreeStructure(menuSnapshotDTOV1.getMenu().getMenuItems(), workspace, null, items, itemKeys);
        menuItemDAO.create(items);
        return Response.ok(responseDTOV1).build();
    }

    @Override
    public Response importWorkspaces(WorkspaceSnapshotDTOV1 workspaceSnapshotDTOV1) {
        var workspace = dao.findByWorkspaceName(workspaceSnapshotDTOV1.getWorkspace().getWorkspaceName());
        ImportResponseDTOV1 response = new ImportResponseDTOV1();
        if (workspace == null) {
            dao.create(mapper.create(workspaceSnapshotDTOV1));
            response.setStatus(ImportResponseStatusDTOV1.CREATED);
        } else {
            throw new ConstraintException("Workspace already exists", MenuItemErrorKeys.WORKSPACE_ALREADY_EXIST, null);
        }

        return Response.ok(response).build();
    }

    @ServerExceptionMapper
    public RestResponse<EximProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<EximProblemDetailResponseDTOV1> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    enum MenuItemErrorKeys {
        WORKSPACE_DOES_NOT_EXIST,
        WORKSPACE_ALREADY_EXIST
    }
}
