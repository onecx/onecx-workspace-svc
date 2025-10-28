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
        methodExceptionTests(() -> dao.updateMenuItem(null, null, 0, null, 0, true),
                MenuItemDAO.ErrorKeys.ERROR_UPDATE_MENU_ITEM);
        methodExceptionTests(() -> dao.loadAllMenuItemsByCriteria(null),
                MenuItemDAO.ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_CRITERIA);
        methodExceptionTests(() -> dao.loadAllMenuItemsByWorkspaces(null),
                MenuItemDAO.ErrorKeys.ERROR_LOAD_ALL_MENU_ITEMS_BY_WORKSPACES);
        methodExceptionTests(() -> dao.loadAllChildren(null),
                MenuItemDAO.ErrorKeys.ERROR_LOAD_ALL_CHILDREN);
        methodExceptionTests(() -> dao.deleteAllMenuItemsByWorkspaceId(null),
                MenuItemDAO.ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_ID);
        methodExceptionTests(() -> dao.findById(null),
                MenuItemDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.deleteAllMenuItemsByWorkspaceIds(null),
                MenuItemDAO.ErrorKeys.ERROR_DELETE_ALL_MENU_ITEMS_BY_WORKSPACE_IDS);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
