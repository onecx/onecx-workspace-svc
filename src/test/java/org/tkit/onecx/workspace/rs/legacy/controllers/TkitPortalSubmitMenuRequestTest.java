package org.tkit.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationResponseDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.ScopeDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.TkitMenuItemStructureDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TkitPortalRestController.class)
class TkitPortalSubmitMenuRequestTest extends AbstractTest {

    @InjectMock
    TkitLegacyAppConfig appConfig;

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void submitMenuRegistrationRequestTest() {
        Mockito.when(appConfig.enableMenuAutoRegistration()).thenReturn(true);
        var request = new MenuRegistrationRequestDTO();
        var response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalName", "does-not-exist")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isFalse();

        response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalName", "test03")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);
        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isFalse();

        request.setMenuItems(new ArrayList<>());
        response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalName", "test03")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);
        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isFalse();

        request = new MenuRegistrationRequestDTO();
        request.setRequestVersion(0);
        var menuItems = new ArrayList<TkitMenuItemStructureDTO>();
        request.setMenuItems(menuItems);
        var menuItem = new TkitMenuItemStructureDTO();
        menuItems.add(menuItem);

        menuItem.setKey("PARAMETERS_MANAGEMENT_UI_ROOT");
        menuItem.setName("Parameters management root menu item");
        menuItem.setUrl("/1000kit-parameters-ui");
        menuItem.setDisabled(false);
        menuItem.setPosition(0);
        menuItem.setScope(ScopeDTO.APP);
        menuItem.setParentKey("PORTAL_MAIN_MENU");
        menuItem.setPermissionObject("PARAMETERS_MANAGEMENT_UI_ROOT");
        menuItem.setI18n(Map.of("de", "Parameters management", "en", "Parameters management"));
        menuItem.setChildren(new ArrayList<>());

        var subMenuItem = new TkitMenuItemStructureDTO();
        menuItem.getChildren().add(subMenuItem);
        subMenuItem.setKey("PARAMETERS_MANAGEMENT_SEARCH");
        subMenuItem.setName("Parameters management search page menu item");
        subMenuItem.setUrl("/1000kit-parameters-ui/parameters/search");
        subMenuItem.setDisabled(false);
        subMenuItem.setPosition(0);
        subMenuItem.setScope(ScopeDTO.PAGE);
        subMenuItem.setPortalExit(false);
        subMenuItem.setParentKey("PARAMETERS_MANAGEMENT_UI_ROOT");
        subMenuItem.setPermissionObject("PARAMETER_SEARCH");
        subMenuItem.setI18n(Map.of("de", "Parameters Management Search", "en", "Parameters Management Search"));

        response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalName", "test01")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isTrue();

        request.getMenuItems().get(0).setParentKey(null);
        response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalName", "test01")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isTrue();
    }

    @ApplicationScoped
    @Mock
    public static class MockedAppConfig implements TkitLegacyAppConfig {

        @Override
        public boolean enableMenuAutoRegistration() {
            return false;
        }
    }

}
