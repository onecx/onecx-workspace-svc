package org.tkit.onecx.workspace.domain.di.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.template.di.model.TemplateImportDI;
import gen.org.tkit.onecx.workspace.template.di.model.TemplateWorkspaceDI;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/test-import.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class TemplateImportTest {

    @Inject
    TemplateDataImportService service;

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    ObjectMapper mapper;

    @Test
    void importDataNoDataTest() {

        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of();
            }
        };

        assertThatThrownBy(() -> service.importData(config)).isInstanceOf(TemplateDataImportService.TemplateException.class);

        List<Workspace> data = workspaceDAO.findAllAsList();
        assertThat(data).isNotNull().hasSize(2);

    }

    @Test
    void importDataNoTenantsTest() {

        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of();
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper.writeValueAsBytes(new TemplateImportDI());
                } catch (Exception ex) {
                    return null;
                }
            }
        };

        service.importData(config);

        List<Workspace> data = workspaceDAO.findAllAsList();
        assertThat(data).isNotNull().hasSize(2);

    }

    @Test
    void importDataExistTest() {

        TemplateImportDI request = new TemplateImportDI()
                .addWorkspacesItem(new TemplateWorkspaceDI().name("test01"))
                .addWorkspacesItem(new TemplateWorkspaceDI().name("test02"));

        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of();
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper.writeValueAsBytes(request);
                } catch (Exception ex) {
                    return null;
                }
            }
        };

        service.importData(config);

        List<Workspace> data = workspaceDAO.findAllAsList();
        assertThat(data).isNotNull().hasSize(2);

    }
}
