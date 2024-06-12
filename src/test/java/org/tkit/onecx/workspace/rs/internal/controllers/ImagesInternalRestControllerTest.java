package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.workspace.rs.internal.mappers.ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.image.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ImagesInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ImagesInternalRestControllerTest extends AbstractTest {

    private static final String MEDIA_TYPE_IMAGE_PNG = "image/png";
    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File FILE = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTest.class.getResource("/images/Testimage.png")).getFile());

    @Test
    void uploadImage() {
        given()
                .pathParam("refId", "productName")
                .pathParam("refType", RefTypeDTO.LOGO.toString())
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

    }

    @Test
    void uploadImageEmptyBody() {

        var exception = given()
                .pathParam("refId", "productName")
                .pathParam("refType", RefTypeDTO.LOGO.toString())
                .when()
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("uploadImage.contentLength: must be greater than or equal to 1");
    }

    @Test
    void uploadImage_shouldReturnBadRequest_whenImageIs() {

        var refId = "productNameUpload";
        var refType = RefTypeDTO.LOGO;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode());

        var exception = given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'image_constraints'  Detail: Key (ref_id, tenant_id, ref_type)=(productNameUpload, tenant-100, logo) already exists.]");
    }

    @Test
    void getImagePngTest() {

        var refId = "themPngTest";
        var refType = RefTypeDTO.FAVICON;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_PNG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();
    }

    @Test
    void getImageJpgTest() {

        var refId = "nameJpg";
        var refType = RefTypeDTO.LOGO;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();
    }

    @Test
    void getImageTest_shouldReturnNotFound_whenImagesDoesNotExist() {

        var refId = "productNameGetTest";
        var refType = RefTypeDTO.FAVICON;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("refId", refId + "_not_exists")
                .pathParam("refType", refType)
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateImage() {

        var refId = "productUpdateTest";
        var refType = RefTypeDTO.LOGO;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        var res = given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .put()
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        Assertions.assertNotNull(res);

        given()
                .pathParam("refId", "does-not-exists")
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .put()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateImage_returnNotFound_whenEntryNotExists() {

        var refId = "productNameUpdateFailed";
        var refType = RefTypeDTO.LOGO;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        var exception = given()
                .pathParam("refId", "wrongRefId")
                .pathParam("refType", "wrongRefType")
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .put()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(exception);
    }

    @Test
    void testMaxUploadSize() {

        var refId = "productMaxUpload";

        byte[] body = new byte[110001];
        new Random().nextBytes(body);

        var exception = given()
                .pathParam("refId", refId)
                .pathParam("refType", RefTypeDTO.LOGO)
                .when()
                .body(body)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImage.contentLength: must be less than or equal to 110000");

    }

    @Test
    void deleteImage() {

        var refId = "workspaceDeleteTest";
        var refType = RefTypeDTO.LOGO;

        given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        var res = given()
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_PNG)
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        Assertions.assertNotNull(res);

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("refId", refId)
                .pathParam("refType", refType)
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
