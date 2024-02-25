package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(ProductInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ProductRestControllerTenantTest extends AbstractTest {

    @Test
    void createProductInWorkspaceTest() {
        var request = new CreateProductRequestDTO();
        request.setWorkspaceId("11-111");
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

        // test workspace under different tenant
        var error = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
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
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isNull();

        // test the same with org 3 the workspace will not be found
        error = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org3"))
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");
    }

    @Test
    void deleteProductByIdTest() {
        // delete with different tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
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
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotEmpty().hasSize(2);
        assertThat(dto.getStream().get(0).getMicrofrontends()).isNotEmpty();

        // delete with correct tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .pathParam("productId", "5678")
                .delete("{productId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotEmpty().hasSize(1);
        assertThat(dto.getStream().get(0).getMicrofrontends()).isNotEmpty();
    }

    @Test
    void getProductsForWorkspaceIdTest() {
        // existing product different tenant
        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(response).isNotNull().isEmpty();

        // existing product
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(dto).isNotNull().isNotEmpty().hasSize(2);
        assertThat(dto.get(0).getMicrofrontends()).isNotEmpty();
        assertThat(dto.get(1).getMicrofrontends()).isNotEmpty();
    }

    @Test
    void updateProductByIdTest() {
        var request = new UpdateProductRequestDTO();
        request.setBaseUrl("/onecx-core");
        request.setModificationCount(10);

        // not sending request
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .put("{productId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        assertThat(error).isNotNull();

        // not existing product
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .body(request)
                .put("{productId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var productDTOList = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        var product = productDTOList.get(0);
        request.setBaseUrl("/mho-test");
        request.setMicrofrontends(new ArrayList<>());
        for (var mf : product.getMicrofrontends()) {
            var updateDto = new UpdateMicrofrontendDTO();
            updateDto.setBasePath(mf.getBasePath());
            updateDto.setMfeId(mf.getMfeId());
            request.getMicrofrontends().add(updateDto);
        }
        request.getMicrofrontends().get(0).setBasePath("/mfe1-test");

        // do not find as another tenant
        given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .put("{productId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var dto = given()
                .when()
                .body(request)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .put("{productId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isNotEmpty();
        assertThat(dto.getBaseUrl()).isEqualTo(request.getBaseUrl());

        var emptyResponse = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(emptyResponse).isNotNull().isEmpty();

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(new TypeRef<List<ProductDTO>>() {
                });

        assertThat(response).isNotNull().isNotEmpty().hasSize(2);
        var filteredProduct = response.stream().filter(x -> x.getProductName().equals(product.getProductName())).findFirst();
        assertThat(filteredProduct.get().getMicrofrontends()).isNotEmpty();
        assertThat(filteredProduct.get().getMicrofrontends().get(0).getBasePath())
                .isEqualTo(request.getMicrofrontends().get(0).getBasePath());

        dto.setMicrofrontends(new ArrayList<>());
        dto = given()
                .when()
                .body(dto)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("productId", "1234")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .put("{productId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMicrofrontends()).isEmpty();
        assertThat(dto.getBaseUrl()).isEqualTo(request.getBaseUrl());
    }
}
