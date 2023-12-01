package io.github.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(ProductInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class ProductRestControllerTest extends AbstractTest {

    @Test
    void createProductInWorkspaceTest() {
        var request = new CreateProductRequestDTO();
        request.setProductName("testProduct");
        request.setBaseUrl("/test");
        var mfe = new CreateMicrofrontendDTO();
        mfe.setMfeId("testMfe1");
        mfe.setBasePath("/testMfe1");
        request.addMicrofrontendsItem(mfe);
        mfe = new CreateMicrofrontendDTO();
        mfe.setMfeId("testMfe2");
        mfe.setBasePath("/testMfe2");
        request.addMicrofrontendsItem(mfe);

        // test not existing workspace
        var error = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "does-not-exists")
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");

        var dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).hasSize(2);
    }

    @Test
    void deleteProductByIdTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "5678")
                .delete("{productId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(dto).isNotNull().isNotEmpty();
        assertThat(dto).hasSize(1);
        assertThat(dto.get(0).getMicrofrontends()).isNotEmpty();
    }

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
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        // existing product
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(dto).isNotNull().isNotEmpty();
        assertThat(dto).hasSize(2);
        assertThat(dto.get(0).getMicrofrontends()).isNotEmpty();
        assertThat(dto.get(1).getMicrofrontends()).isNotEmpty();
    }

    @Test
    void updateProductByIdTest() {
        var request = new UpdateProductRequestDTO();
        request.setBaseUrl("/onecx-core");

        // not sending request
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "does-not-exist")
                .put("{productId}")
                .then().log().all()
                .statusCode(BAD_REQUEST.getStatusCode());

        assertThat(error).isNotNull();

        // not existing product
        given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "does-not-exist")
                .put("{productId}")
                .then().log().all()
                .statusCode(NOT_FOUND.getStatusCode());

        var dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .put("{productId}")
                .then().log().all()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isNull();
        assertThat(dto.getBaseUrl()).isEqualTo(request.getBaseUrl());
    }
}
