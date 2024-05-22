package org.tkit.onecx.workspace.domain.di.services;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Assignment;
import org.tkit.onecx.workspace.domain.models.MenuItem;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

@ApplicationScoped
public class WorkspaceTemplateService {

    private static final String PRINCIPAL = "template-import";

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    MenuItemDAO menuItemDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void createWorkspaces(String tenantId, List<Workspace> workspaces, List<MenuItem> menuItems,
            List<Assignment> assignments) {
        try {
            var ctx = Context.builder()
                    .principal(PRINCIPAL)
                    .tenantId(tenantId)
                    .build();
            ApplicationContext.start(ctx);

            workspaceDAO.create(workspaces);
            menuItemDAO.create(menuItems);
            assignmentDAO.create(assignments);

        } finally {
            ApplicationContext.close();
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<String> filterWorkspaceNames(String tenantId, Set<String> workspaceNames) {
        try {
            var ctx = Context.builder()
                    .principal(PRINCIPAL)
                    .tenantId(tenantId)
                    .build();
            ApplicationContext.start(ctx);

            return workspaceDAO.filterWorkspaceNames(workspaceNames);
        } finally {
            ApplicationContext.close();
        }
    }
}
