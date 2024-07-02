package org.tkit.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.legacy.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(PortalV2RestController.class)
class PortalV2RestControllerTenantTest extends AbstractTest {

    @Test
    void getMenuStructureForPortalNameOrg3Test() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "test01")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        Assertions.assertThat(data).isNotNull().isEmpty();
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        Assertions.assertThat(data).isNotNull().isNotEmpty().hasSize(5);

        data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .queryParam("interpolate", Boolean.FALSE)
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        Assertions.assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

}
