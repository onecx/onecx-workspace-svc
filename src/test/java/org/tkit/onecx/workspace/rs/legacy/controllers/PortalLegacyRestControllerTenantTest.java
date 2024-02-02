package org.tkit.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuItemStructureDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(PortalLegacyRestController.class)
class PortalLegacyRestControllerTenantTest extends AbstractTest {

    @Test
    void getMenuStructureForNoPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get("{portalName}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isEmpty();
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{portalName}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameAndAppTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .pathParam("applicationId", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{portalName}/{applicationId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameAndAppDifferentTenantTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .pathParam("applicationId", "test01")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("{portalName}/{applicationId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isEmpty();
    }
}
