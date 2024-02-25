package org.tkit.onecx.workspace.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.RoleDAO;
import org.tkit.onecx.workspace.rs.internal.mappers.AssignmentMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.internal.AssignmentInternalApi;
import gen.org.tkit.onecx.workspace.rs.internal.model.AssignmentSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.CreateAssignmentRequestDTO;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AssignmentInternalRestController implements AssignmentInternalApi {

    @Inject
    AssignmentDAO dao;

    @Inject
    AssignmentMapper mapper;

    @Inject
    RoleDAO roleDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Context
    UriInfo uriInfo;

    @Override
    public Response createAssignment(CreateAssignmentRequestDTO createAssignmentRequestDTO) {
        var role = roleDAO.findById(createAssignmentRequestDTO.getRoleId());
        if (role == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var menu = menuItemDAO.findById(createAssignmentRequestDTO.getMenuItemId());
        if (menu == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var data = mapper.create(role, menu);
        data = dao.create(data);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(data.getId()).build())
                .entity(mapper.map(data))
                .build();
    }

    @Override
    public Response deleteAssignment(String assignmentId) {
        dao.deleteQueryById(assignmentId);
        return Response.noContent().build();
    }

    @Override
    public Response getAssignment(String assignmentId) {
        var assignment = dao.findById(assignmentId);
        if (assignment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(assignment)).build();
    }

    @Override
    public Response searchAssignments(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO) {
        var criteria = mapper.map(assignmentSearchCriteriaDTO);
        var result = dao.findByCriteria(criteria);
        return Response.ok(mapper.map(result)).build();
    }
}
