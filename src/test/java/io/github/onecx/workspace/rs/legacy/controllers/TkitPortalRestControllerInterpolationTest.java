package io.github.onecx.workspace.rs.legacy.controllers;

import gen.io.github.onecx.workspace.rs.legacy.model.TkitMenuItemStructureDTO;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(TkitPortalRestController.class)
public class TkitPortalRestControllerInterpolationTest extends AbstractTest {

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .queryParam("interpolate", Boolean.TRUE)
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<TkitMenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull();
        assertThat(data).isNotEmpty();
        assertThat(data).hasSize(5);
        assertThat(getUrlFromMenuItemByName(data, "PORTAL_CHILD_5")).isEqualTo("interpolated5");
        assertThat(getUrlFromMenuItemByName(data, "PORTAL_CHILD_6")).isEqualTo("interpolated6");
        assertThat(getUrlFromMenuItemByName(data, "PORTAL_CHILD_7")).isEqualTo("/interpolated7");
        assertThat(getUrlFromMenuItemByName(data, "PORTAL_CHILD_8")).isEqualTo("/interpolated8");
    }

    private String getUrlFromMenuItemByName(List<TkitMenuItemStructureDTO> menuList, String key) {
        String foundurl;
        for (TkitMenuItemStructureDTO menuItem : menuList) {
            if (menuItem.getKey().equals(key)) {
                foundurl = menuItem.getUrl();
                return foundurl;
            }

            if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
                foundurl = getUrlFromMenuItemByName(menuItem.getChildren(), key);
                if (foundurl != null) {
                    return foundurl;
                }
            }
        }

        return null;
    }

}
