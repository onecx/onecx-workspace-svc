package io.github.onecx.workspace.domain.di;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceDataImportServiceTest extends AbstractTest {

    @Inject
    WorkspaceDAO dao;

    @Inject
    WorkspaceDataImportService service;

    @Test
    void importDataTest() {
        Workspace workspace = dao.loadByWorkspaceName("test01");
        Assertions.assertNotNull(workspace);
        Assertions.assertNotNull(workspace.getProducts());
        Assertions.assertNotNull(workspace.getProducts().get(0).getMicrofrontends());

        Stream<Workspace> result = dao.findAll();
        Assertions.assertEquals(3, result.count());
    }

}
