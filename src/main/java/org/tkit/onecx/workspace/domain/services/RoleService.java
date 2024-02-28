package org.tkit.onecx.workspace.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.RoleDAO;

@ApplicationScoped
public class RoleService {

    @Inject
    RoleDAO roleDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Transactional
    public void deleteRole(String roleId) {
        assignmentDAO.deleteAllByRoleId(roleId);
        roleDAO.deleteQueryById(roleId);
    }
}
