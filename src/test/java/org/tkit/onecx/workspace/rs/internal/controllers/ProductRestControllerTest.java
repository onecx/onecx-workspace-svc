package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ProductInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ProductRestControllerTest extends AbstractTest {

    @Test
    void createProductInWorkspaceTest() {
        var request = new CreateProductRequestDTO();
        request.setWorkspaceId("does-not-exists");
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
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");

        request.setWorkspaceId("11-111");
        var dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).hasSize(2);

        request.setMicrofrontends(null);
        request.setProductName("testProduct1");
        request.setBaseUrl("/test1");
        dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isNull();
    }

    @Test
    void deleteProductByIdTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "5678")
                .delete("{productId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "5678")
                .delete("{productId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new ProductSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotEmpty().hasSize(1);
    }

    @Test
    void getProductsForWorkspaceIdTest() {

        var criteria = new ProductSearchCriteriaDTO()
                .workspaceId("does-not-exists");

        // not existing product
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getStream()).isNotNull().isEmpty();

        criteria.workspaceId("11-111");

        // existing product
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotEmpty().hasSize(2);
    }

    @Test
    void updateProductByIdTest() {
        var request = new UpdateProductRequestDTO();
        request.setBaseUrl("/onecx-core");
        request.setModificationCount(0);

        // not sending request
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "does-not-exist")
                .put("{productId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        assertThat(error).isNotNull();

        // not existing product
        given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "does-not-exist")
                .put("{productId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var criteria = new ProductSearchCriteriaDTO()
                .workspaceId("11-111");

        var product = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("1234")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(product).isNotNull();
        request.setBaseUrl("/mho-test");
        request.setModificationCount(product.getModificationCount());
        request.setMicrofrontends(new ArrayList<>());
        for (var mf : product.getMicrofrontends()) {
            var updateDto = new UpdateMicrofrontendDTO();
            updateDto.setBasePath(mf.getBasePath());
            updateDto.setMfeId(mf.getMfeId());
            request.getMicrofrontends().add(updateDto);
        }
        request.getMicrofrontends().get(0).setBasePath("/mfe1-test");

        var dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "1234")
                .put("{productId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isNotEmpty();
        assertThat(dto.getBaseUrl()).isEqualTo(request.getBaseUrl());

        var filteredProduct = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("1234")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(filteredProduct).isNotNull();
        assertThat(filteredProduct.getMicrofrontends().get(0).getBasePath())
                .isEqualTo(request.getMicrofrontends().get(0).getBasePath());

        dto.setMicrofrontends(new ArrayList<>());
        dto = given()
                .when()
                .body(dto)
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "1234")
                .put("{productId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isEmpty();
        assertThat(dto.getBaseUrl()).isEqualTo(request.getBaseUrl());

        //second time should fail because of wrong modificationCount
        request.setModificationCount(-1);
        given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("productId", "1234")
                .put("{productId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void modificationCountTest() {

        var update = new UpdateProductRequestDTO();
        update.setBaseUrl("1234");
        update.setModificationCount(-1);

        given()
                .when()
                .body(update)
                .contentType(APPLICATION_JSON)
                .put("1234")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }
}
