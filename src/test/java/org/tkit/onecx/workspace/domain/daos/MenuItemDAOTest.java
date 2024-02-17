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
class MenuItemDAOTest {
    @Inject
    MenuItemDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.deleteAllMenuItemsByWorkspaceId(null),
                MenuItemDAO.ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID);
        methodExceptionTests(() -> dao.deleteAllMenuItemsByWorkspace(null),
                MenuItemDAO.ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME);
        methodExceptionTests(() -> dao.updateMenuItems(null, null, null), MenuItemDAO.ErrorKeys.ERROR_UPDATE_MENU_ITEMS);
        methodExceptionTests(() -> dao.loadAllMenuItemsByWorkspace(null),
                MenuItemDAO.ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME);
        methodExceptionTests(() -> dao.deleteAllMenuItemsByWorkspaceNameAndAppId(null, null),
                MenuItemDAO.ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_NAME_AND_APP_ID);
        methodExceptionTests(() -> dao.loadMenuItemByWorkspaceAndKey(null, null),
                MenuItemDAO.ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACE_NAME);
        methodExceptionTests(() -> dao.findById(null),
                MenuItemDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.loadById(null),
                MenuItemDAO.ErrorKeys.LOAD_ENTITY_BY_ID_FAILED);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
