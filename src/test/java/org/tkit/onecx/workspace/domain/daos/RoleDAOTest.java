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
class RoleDAOTest {
    @Inject
    RoleDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.findRolesByWorkspaceAndNames(null, null),
                RoleDAO.ErrorKeys.ERROR_FILTER_ROLE_NAMES);
        methodExceptionTests(() -> dao.loadById(null),
                RoleDAO.ErrorKeys.LOAD_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.findByCriteria(null),
                RoleDAO.ErrorKeys.ERROR_FIND_ROLE_BY_CRITERIA);
        methodExceptionTests(() -> dao.findById(null),
                RoleDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);

    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
