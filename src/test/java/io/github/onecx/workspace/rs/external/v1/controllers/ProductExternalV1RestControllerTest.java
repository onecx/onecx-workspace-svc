package io.github.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.external.v1.model.ProductDTOV1;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(ProductsExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ProductExternalV1RestControllerTest extends AbstractTest {

    @Test
    void getProductsForWorkspaceIdTest() {
        // not existing product
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "does-not-exist")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTOV1>>() {
                });

        assertThat(response).isNotNull().isEmpty();

        // existing product
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTOV1>>() {
                });

        assertThat(dto).isNotNull().isNotEmpty().hasSize(2);
        assertThat(dto.get(0).getProductName()).isNotEmpty();
        assertThat(dto.get(1).getProductName()).isNotEmpty();
    }
}
