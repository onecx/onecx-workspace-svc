package org.tkit.onecx.workspace.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AssignmentDAOTest {
    @Inject
    AssignmentDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.deleteAllByWorkspaceIds(null),
                AssignmentDAO.ErrorKeys.ERROR_DELETE_ITEMS_BY_WORKSPACE_ID);
        methodExceptionTests(() -> dao.findAssignmentMenuForWorkspaces(null),
                AssignmentDAO.ErrorKeys.ERROR_FIND_ASSIGNMENT_BY_WORKSPACES);
        methodExceptionTests(() -> dao.findAssignmentMenuForWorkspace(null),
                AssignmentDAO.ErrorKeys.ERROR_FIND_ASSIGNMENT_BY_WORKSPACE);
        methodExceptionTests(() -> dao.deleteAllByWorkspaceId(null),
                AssignmentDAO.ErrorKeys.ERROR_DELETE_ITEMS_BY_WORKSPACE_ID);
        methodExceptionTests(() -> dao.deleteAllByMenuId(null),
                AssignmentDAO.ErrorKeys.ERROR_DELETE_ITEMS_BY_MENU_ID);
        methodExceptionTests(() -> dao.deleteAllByRoleId(null),
                AssignmentDAO.ErrorKeys.ERROR_DELETE_ITEMS_BY_ROLE_ID);
        methodExceptionTests(() -> dao.findByCriteria(null),
                AssignmentDAO.ErrorKeys.ERROR_FIND_ASSIGNMENT_BY_CRITERIA);
        methodExceptionTests(() -> dao.findById(null),
                AssignmentDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);

    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
