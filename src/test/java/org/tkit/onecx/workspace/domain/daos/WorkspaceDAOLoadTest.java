package org.tkit.onecx.workspace.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class WorkspaceDAOLoadTest {

    @Inject
    WorkspaceDAO workspaceDAO;

    @Test
    void loadTest() {
        assertThat(workspaceDAO.loadById("does-not-exists")).isNull();
    }
}
