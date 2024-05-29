package org.tkit.onecx.workspace.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ImageDAOMethodTest {

    @Inject
    ImageDAO dao;

    @Test
    void methodTests() {

        var items1 = dao.findByRefIds(null);
        assertThat(items1).isNotNull();

        var items2 = dao.findByRefIds(List.of());
        assertThat(items2).isNotNull();
    }
}
