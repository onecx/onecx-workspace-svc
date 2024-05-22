package org.tkit.onecx.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Role;
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
        assertThat(resultList).isNotNull().hasSize(1);
        var w = resultList.get(0);
        assertThat(w.getName()).isEqualTo("ADMIN");
        assertThat(w.getTenantId()).isEqualTo("tenant-100");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2);
        var map = w.getRoles().stream().collect(Collectors.toMap(Role::getName, x -> x));

        assertThat(map).containsOnlyKeys("onecx-admin", "onecx-user-test");
    }

}
