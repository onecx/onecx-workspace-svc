package org.tkit.onecx.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AfterStartDataImportTest extends AbstractTest {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Test
    void importDataFromFileTest() {
        Stream<Workspace> result = workspaceDAO.findAll();
        var resultList = result.toList();
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).getName()).isEqualTo("ADMIN");
        assertThat(resultList.get(0).getTenantId()).isEqualTo("tenant-100");

    }

}
