package org.tkit.onecx.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.domain.daos.AssignmentDAO;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.onecx.workspace.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AfterStartDataImportTest extends AbstractTest {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Inject
    AssignmentDAO assignmentDAO;

    @Test
    void importDataFromFileTest() {
        var resultList = workspaceDAO.findAllAsList();
        assertThat(resultList).isNotNull().hasSize(1);
        var w = resultList.get(0);
        assertThat(w.getName()).isEqualTo("ADMIN");
        assertThat(w.getTenantId()).isEqualTo("tenant-100");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2);
        var map = w.getRoles().stream().collect(Collectors.toMap(Role::getName, x -> x));

        assertThat(map).containsOnlyKeys("onecx-admin", "onecx-user-test");

        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(4);

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(3);

        var assignments = assignmentDAO.findAllAsList();
        assertThat(assignments).isNotNull().hasSize(7);
    }

}
