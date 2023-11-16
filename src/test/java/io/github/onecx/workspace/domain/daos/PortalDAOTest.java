package io.github.onecx.workspace.domain.daos;

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
class PortalDAOTest {
    @Inject
    PortalDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.countPortalsForRegMfeId(null),
                PortalDAO.ErrorKeys.ERROR_COUNT_PORTALS_FOR_MFE_ID);
        methodExceptionTests(() -> dao.findByPortalName(null), PortalDAO.ErrorKeys.ERROR_FIND_PORTAL_NAME);
        methodExceptionTests(() -> dao.findByBaseUrl(null),
                PortalDAO.ErrorKeys.ERROR_FIND_BY_BASE_URL);
        methodExceptionTests(() -> dao.findBySearchCriteria(null),
                PortalDAO.ErrorKeys.ERROR_FIND_BY_CRITERIA);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
