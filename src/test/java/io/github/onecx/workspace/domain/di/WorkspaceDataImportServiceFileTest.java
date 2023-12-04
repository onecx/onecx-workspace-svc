package io.github.onecx.workspace.domain.di;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@DisplayName("Portal data import test from example file")
@TestProfile(WorkspaceDataImportServiceFileTest.CustomProfile.class)
class WorkspaceDataImportServiceFileTest extends AbstractTest {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Test
    @DisplayName("Import portal data from file")
    void importDataFromFileTest() {
        Stream<Workspace> result = workspaceDAO.findAll();
        Assertions.assertEquals(1, result.count());

    }

    public static class CustomProfile implements QuarkusTestProfile {

        @Override
        public String getConfigProfile() {
            return "test";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "tkit.dataimport.enabled", "true",
                    "tkit.dataimport.configurations.workspace.enabled", "true",
                    "tkit.dataimport.configurations.workspace.file", "./src/test/resources/import/workspace-import.json",
                    "tkit.dataimport.configurations.workspace.metadata.operation", "CLEAN_INSERT",
                    "tkit.dataimport.configurations.workspace.stop-at-error", "true");
        }
    }

}
