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
class WorkspaceDAOTest {
    @Inject
    WorkspaceDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {

        methodExceptionTests(() -> dao.loadByNameProducts(null),
                WorkspaceDAO.ErrorKeys.ERROR_LOAD_WORKSPACE_PRODUCTS);
        methodExceptionTests(() -> dao.loadByUrlProductsSlots(null),
                WorkspaceDAO.ErrorKeys.ERROR_LOAD_WORKSPACE_PRODUCTS_SLOTS);
        methodExceptionTests(() -> dao.loadById(null),
                WorkspaceDAO.ErrorKeys.ERROR_LOAD_WORKSPACE);
        methodExceptionTests(() -> dao.findBySearchCriteria(null),
                WorkspaceDAO.ErrorKeys.ERROR_FIND_BY_CRITERIA);
        methodExceptionTests(() -> dao.findById(null),
                WorkspaceDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.findByName(null),
                WorkspaceDAO.ErrorKeys.ERROR_FIND_WORKSPACE_BY_NAME);
        methodExceptionTests(() -> dao.findByUrl(null),
                WorkspaceDAO.ErrorKeys.ERROR_FIND_WORKSPACE_BY_URL);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }

}
