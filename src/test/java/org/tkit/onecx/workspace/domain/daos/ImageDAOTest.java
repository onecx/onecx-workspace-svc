package org.tkit.onecx.workspace.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ImageDAOTest {

    @Inject
    ImageDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.deleteQueryByRefIds(null),
                ImageDAO.ErrorKeys.FAILED_TO_DELETE_BY_REF_IDS_QUERY);
        methodExceptionTests(() -> dao.findByRefIds(null),
                ImageDAO.ErrorKeys.ERROR_FIND_REF_IDS);
        methodExceptionTests(() -> dao.deleteQueryByRefId(null),
                ImageDAO.ErrorKeys.FAILED_TO_DELETE_BY_REF_ID_QUERY);
        methodExceptionTests(() -> dao.findByRefIdAndRefType(null, null),
                ImageDAO.ErrorKeys.FIND_ENTITY_BY_REF_ID_REF_TYPE_FAILED);
        methodExceptionTests(() -> dao.deleteQueryByRefIdAndRefType("1", RefTypeDTO.LOGO),
                ImageDAO.ErrorKeys.FAILED_TO_DELETE_BY_REF_ID_REF_TYPE_QUERY);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
