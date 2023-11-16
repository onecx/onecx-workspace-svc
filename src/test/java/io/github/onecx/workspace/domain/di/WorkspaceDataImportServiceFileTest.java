package io.github.onecx.workspace.domain.di;

import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.onecx.workspace.domain.daos.PortalDAO;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@DisplayName("Portal data import test from example file")
@TestProfile(WorkspaceDataImportServiceFileTest.CustomProfile.class)
class WorkspaceDataImportServiceFileTest extends AbstractTest {

    @Inject
    PortalDAO portalDAO;

    @Test
    @DisplayName("Import portal data from file")
    void importDataFromFileTest() {

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
                    "tkit.dataimport.configurations.portal.enabled", "true",
                    "tkit.dataimport.configurations.portal.file", "./src/test/resources/import/portal-import.json",
                    "tkit.dataimport.configurations.portal.metadata.operation", "CLEAN_INSERT",
                    "tkit.dataimport.configurations.portal.stop-at-error", "true");
        }
    }

}
