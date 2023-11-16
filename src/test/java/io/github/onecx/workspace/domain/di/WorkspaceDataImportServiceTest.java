package io.github.onecx.workspace.domain.di;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceDataImportServiceTest extends AbstractTest {

    @Inject
    WorkspaceDataImportService service;

    @Test
    void importDataTest() {

    }
}
