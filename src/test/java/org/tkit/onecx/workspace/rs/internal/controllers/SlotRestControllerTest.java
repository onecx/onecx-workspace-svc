package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(SlotInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:all", "ocx-ws:read", "ocx-ws:write", "ocx-ws:delete" })
class SlotRestControllerTest extends AbstractTest {

    @Test
    void createNewSlotTest() {

        // create Role
        var requestDTO = new CreateSlotRequestDTO();
        requestDTO.setSlots(List.of(new CreateSlotDTO().name("slot01")));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then().statusCode(BAD_REQUEST.getStatusCode());

        requestDTO.workspaceId("does-not-exists");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then().statusCode(NOT_FOUND.getStatusCode());

        requestDTO.workspaceId("11-111");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then().statusCode(CREATED.getStatusCode());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("/workspace/11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(WorkspaceSlotsDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getSlots()).hasSize(5);

        // create Role without body
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("createSlot.createSlotRequestDTO: must not be null");

        // create Slot with existing name, should skip
        requestDTO = new CreateSlotRequestDTO();
        requestDTO.setWorkspaceId("11-111");
        requestDTO.setSlots(List.of(new CreateSlotDTO().name("slot1")));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getNotFoundSlot() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("does-not-exists")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteSlotNoneExistsTest() {

        // delete Role
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .delete("DELETE_1")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if Role exists
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("DELETE_1")
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteSlotTest() {
        // delete Role in portal
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .delete("s11")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getSlotByIdTest() {

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("s11")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("slot1");
        assertThat(dto.getId()).isEqualTo("s11");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("___")
                .then().statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("s12")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("slot2");
        assertThat(dto.getId()).isEqualTo("s12");

    }

    @Test
    void getWorkspaceSlotTest() {
        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("/workspace/11-111")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspaceSlotsDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getSlots()).isNotNull().hasSize(4);
        assertThat(data.getSlots().get(0)).isNotNull();
        assertThat(data.getSlots().get(0).getComponents()).isNotNull().hasSize(3);

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("/workspace/11-222")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspaceSlotsDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getSlots()).isNotNull().isEmpty();
    }

    @Test
    void updateSlotTest() {

        // download Role
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .when()
                .get("s11")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        // update none existing Slot
        var requestDto = new UpdateSlotRequestDTO();
        requestDto.setName("test01");
        requestDto.setModificationCount(dto.getModificationCount());
        requestDto.getComponents().add(new SlotComponentDTO().name("new_c2").productName("p1").appId("a1"));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(requestDto)
                .when()
                .put("does-not-exists")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // update Slot
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(requestDto)
                .when()
                .put("s11")
                .then()
                .statusCode(OK.getStatusCode());

        // update Slot with old modificationCount
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(requestDto)
                .when()
                .put("s11")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull()
                .isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.OPTIMISTIC_LOCK.name());
        assertThat(exception.getDetail()).isNotNull()
                .isEqualTo(
                        "Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [org.tkit.onecx.workspace.domain.models.Slot#s11]");

        // download Role
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .when()
                .get("s11")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(requestDto.getName());
        assertThat(dto.getComponents()).isNotNull().hasSize(1);
        assertThat(dto.getComponents().get(0)).returns("new_c2", from(SlotComponentDTO::getName));

    }

    @Test
    void updateSlotComponentsIfNoneExistYes() {

        // download slot
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .when()
                .get("s14")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        // update none existing Slot
        var requestDto = new UpdateSlotRequestDTO();

        requestDto.setName(dto.getName()); //keep same name to avoid update because of changed name
        requestDto.setModificationCount(dto.getModificationCount());
        requestDto.getComponents().add(new SlotComponentDTO().name("new_c2").productName("p1").appId("a1"));

        // update Slot
        var updatedSlot = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(requestDto)
                .when()
                .put("s14")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(SlotDTO.class);

        assertThat(dto.getModificationCount()).isLessThan(updatedSlot.getModificationCount());
        assertThat(updatedSlot.getComponents()).hasSize(1);
    }

    @Test
    void updateSlotComponentOrder() {
        // get slot
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .when()
                .get("s11")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);
        //remove first component and add it again to the end
        var firstComponent = dto.getComponents().get(0);
        dto.getComponents().remove(0);
        dto.getComponents().add(firstComponent);

        var requestDto = new UpdateSlotRequestDTO();
        requestDto.setName(dto.getName());
        requestDto.setModificationCount(dto.getModificationCount());
        requestDto.setComponents(dto.getComponents());

        // update Slot
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(requestDto)
                .when()
                .put("s11")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(SlotDTO.class);
        Assertions.assertEquals("c2", result.getComponents().get(0).getName());
        Assertions.assertEquals("c3", result.getComponents().get(1).getName());
        Assertions.assertEquals("c1", result.getComponents().get(2).getName());

    }

    @Test
    void updateSlotWithExistingNameTest() {

        // download Role
        var d = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .when()
                .get("s11")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(SlotDTO.class);

        var dto = new UpdateRoleRequestDTO();
        dto.setModificationCount(d.getModificationCount());
        dto.setName("slot2");
        dto.setDescription("description");

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .when()
                .body(dto)
                .put("s11")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo("MERGE_ENTITY_FAILED");
        assertThat(exception.getDetail()).isNotNull()
                .isEqualTo(
                        "could not execute statement [ERROR: duplicate key value violates unique constraint 'slot_workspace_name'  Detail: Key (name, workspace_guid, tenant_id)=(slot2, 11-111, tenant-100) already exists.]");
        assertThat(exception.getInvalidParams()).isEmpty();
    }

    @Test
    void updateSlotWithoutBodyTest() {

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .when()
                .pathParam("id", "update_create_new")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull()
                .isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("updateSlot.updateSlotRequestDTO: must not be null");
        assertThat(exception.getInvalidParams()).isNotNull().hasSize(1);
    }
}
