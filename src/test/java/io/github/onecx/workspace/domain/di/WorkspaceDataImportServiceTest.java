package io.github.onecx.workspace.domain.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.io.github.onecx.workspace.di.workspace.v1.model.WorkspaceDataImportDTOV1;
import gen.io.github.onecx.workspace.di.workspace.v1.model.WorkspaceImportDTOV1;
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

    @Inject
    ObjectMapper mapper;

    @Test
    void importDataTest() {
        Workspace workspace = dao.loadByWorkspaceName("test01");
        Assertions.assertNotNull(workspace);
        Assertions.assertNotNull(workspace.getProducts());
        Assertions.assertNotNull(workspace.getProducts().get(0).getMicrofrontends());

        // test not existing workspace
        workspace = dao.loadByWorkspaceName("does-not-exist");
        Assertions.assertNull(workspace);

        Stream<Workspace> result = dao.findAll();
        Assertions.assertEquals(3, result.count());
    }

    @Test
    void importDataWithoutMenuItemsTest() {
        service.importData(new DataImportConfig() {
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
                    workspace.setWorkspaceName("test1");
                    workspace.setBaseUrl("baseurl");
                    importRequest.setMenuItems(null);
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        var workspaces = dao.findAll().toList();
        assertThat(workspaces).hasSize(1);

        service.importData(new DataImportConfig() {
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
                    workspace.setWorkspaceName("test1");
                    workspace.setBaseUrl("baseurl");
                    importRequest.setMenuItems(new ArrayList<>());
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        workspaces = dao.findAll().toList();
        assertThat(workspaces).hasSize(1);
    }

    @Test
    void importDataNONETest() {
        service.importData(new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "NONE");
            }
        });

        var workspaces = dao.findAll().toList();
        assertThat(workspaces).hasSize(3);
    }

    @Test
    void importDataNotSupportedTest() {
        service.importData(new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CUSTOM_NOT_SUPPORTED");
            }
        });

        var workspaces = dao.findAll().toList();
        assertThat(workspaces).hasSize(3);
    }

    @Test
    void importEmptyDataTest() {
        assertDoesNotThrow(() -> {

            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "CLEAN_INSERT");
                }

                @Override
                public byte[] getData() {
                    try {
                        return mapper.writeValueAsBytes(null);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "CLEAN_INSERT");
                }

                @Override
                public byte[] getData() {
                    try {
                        return mapper.writeValueAsBytes(new WorkspaceDataImportDTOV1());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "CLEAN_INSERT");
                }

                @Override
                public byte[] getData() {
                    try {
                        var data = new WorkspaceDataImportDTOV1();
                        data.setRequests(new ArrayList<>());
                        return mapper.writeValueAsBytes(data);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

        });

        var config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CLEAN_INSERT");
            }

            @Override
            public byte[] getData() {
                return new byte[] { 0 };
            }
        };
        assertThrows(WorkspaceDataImportService.ImportException.class, () -> service.importData(config));
    }

}
