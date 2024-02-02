package org.tkit.onecx.workspace.domain.di;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.WorkspaceDataImportDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.WorkspaceImportDTOV1;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceDataImportServiceExceptionTest extends AbstractTest {

    @InjectMock
    WorkspaceDAO dao;

    @Inject
    WorkspaceDataImportService service;

    @Inject
    ObjectMapper mapper;

    @BeforeEach
    void init() {
        doThrow(RuntimeException.class).when(dao).create((Workspace) any());
    }

    @Test
    void importDataDaoExceptionTest() {
        var config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CLEAN_INSERT");
            }

            @Override
            public byte[] getData() {
                try {
                    var data = new WorkspaceDataImportDTOV1();
                    data.setRequests(new ArrayList<>());
                    ImportRequestDTOV1 importRequest = new ImportRequestDTOV1();
                    data.getRequests().add(importRequest);
                    WorkspaceImportDTOV1 workspace = new WorkspaceImportDTOV1();
                    importRequest.setWorkspace(workspace);
                    workspace.setName("test1");
                    workspace.setBaseUrl("baseurl");
                    workspace.setTenantId("tenant-100");
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        assertThrows(WorkspaceDataImportService.ImportException.class, () -> service.importData(config));

        var config2 = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CLEAN_INSERT");
            }

            @Override
            public byte[] getData() {
                try {
                    var data = new WorkspaceDataImportDTOV1();
                    data.setRequests(new ArrayList<>());
                    ImportRequestDTOV1 importRequest = new ImportRequestDTOV1();
                    data.getRequests().add(importRequest);
                    WorkspaceImportDTOV1 workspace = new WorkspaceImportDTOV1();
                    workspace.setTenantId("tenant-100");
                    importRequest.setWorkspace(workspace);
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        assertThrows(WorkspaceDataImportService.ImportException.class, () -> service.importData(config2));

        var config3 = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CLEAN_INSERT");
            }

            @Override
            public byte[] getData() {
                try {
                    var data = new WorkspaceDataImportDTOV1();
                    data.setRequests(new ArrayList<>());
                    ImportRequestDTOV1 importRequest = new ImportRequestDTOV1();
                    data.getRequests().add(importRequest);
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        assertThrows(WorkspaceDataImportService.ImportException.class, () -> service.importData(config3));

        doThrow(RuntimeException.class).when(dao).deleteAll();
        assertThrows(WorkspaceDataImportService.ImportException.class, () -> service.importData(config));
    }
}
