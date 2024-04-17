package org.tkit.onecx.workspace.rs.user.controllers;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.criteria.MenuItemLoadCriteria;
import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.AssignmentMenu;
import org.tkit.onecx.workspace.rs.user.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.rs.user.mappers.UserMenuMapper;
import org.tkit.onecx.workspace.rs.user.services.MenuMappingConfig;
import org.tkit.onecx.workspace.rs.user.services.TokenService;
import org.tkit.quarkus.log.cdi.LogExclude;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.user.UserMenuInternalApi;
import gen.org.tkit.onecx.workspace.rs.user.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuRequestDTO;

@LogService
@ApplicationScoped
public class UserMenuInternalController implements UserMenuInternalApi {

    @Inject
    TokenService tokenService;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Inject
    UserMenuMapper mapper;

    @Inject
    MenuMappingConfig mappingConfig;

    @Override
    public Response getUserMenu(String workspaceName, @LogExclude UserWorkspaceMenuRequestDTO userWorkspaceMenuRequestDTO) {
        var roles = tokenService.getTokenRoles(userWorkspaceMenuRequestDTO.getToken());
        HashSet<String> menuKeys = new HashSet<>();
        if (userWorkspaceMenuRequestDTO.getMenuKeys() != null) {
            userWorkspaceMenuRequestDTO.getMenuKeys().forEach(s -> {
                if (mappingConfig.userConfig().mapping().containsKey(s)) {
                    menuKeys.add(mappingConfig.userConfig().mapping().get(s));
                }
            });
        }

        var workspace = workspaceDAO.findByName(workspaceName);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<AssignmentMenu> assignmentRecords = assignmentDAO.findAssignmentMenuForWorkspace(workspace.getId());
        if (assignmentRecords.isEmpty()) {
            return Response.ok(mapper.empty(workspaceName)).build();
        }

        // menuItemId -> set of roles
        Map<String, Set<String>> mapping = assignmentRecords.stream()
                .collect(Collectors.groupingBy(AssignmentMenu::menuItemId, mapping(AssignmentMenu::roleName, toSet())));

        var criteria = new MenuItemLoadCriteria();
        criteria.setWorkspaceId(workspace.getId());
        var items = menuItemDAO.loadAllMenuItemsByCriteria(criteria);

        return Response.ok(mapper.mapTree(workspace, items, mapping, new HashSet<>(roles), menuKeys))
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
