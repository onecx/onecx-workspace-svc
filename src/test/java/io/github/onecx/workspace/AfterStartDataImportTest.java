package io.github.onecx.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.onecx.workspace.domain.daos.WorkspaceDAO;
import io.github.onecx.workspace.domain.models.Workspace;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AfterStartDataImportTest extends AbstractTest {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Test
    void importDataFromFileTest() {
        Stream<Workspace> result = workspaceDAO.findAll();
        var restulList = result.toList();
        assertThat(restulList).hasSize(1);
        assertThat(restulList.get(0).getWorkspaceName()).isEqualTo("ADMIN");
        assertThat(restulList.get(0).getTenantId()).isEqualTo("tenant-100");

    }

}
