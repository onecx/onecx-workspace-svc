package org.tkit.onecx.workspace.test;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.restassured.RestAssured.config;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

import java.security.PrivateKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;
import org.tkit.onecx.workspace.domain.template.models.CreateTemplateConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.Mock;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.config.RestAssuredConfig;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@SuppressWarnings("java:S2187")
public class AbstractTest {

    protected static final String APM_HEADER_PARAM = "apm-principal-token";
    protected static final String CLAIMS_ORG_ID = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.tenant-id.mock.claim-org-id", String.class);

    static {
        config = RestAssuredConfig.config().objectMapperConfig(
                objectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            var objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
                            return objectMapper;
                        }));
    }

    protected static final String USER_ALICE = "alice";

    protected static final String USER_BOB = "bob";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    protected String createAccessTokenBearer(String user) {
        return "Bearer " + createAccessToken(user);
    }

    protected String createAccessToken(String user) {
        return keycloakClient.getAccessToken(user);
    }

    protected static String createToken(String organizationId) {
        try {
            String userName = "test-user";
            JsonObjectBuilder claims = Json.createObjectBuilder();
            claims.add(Claims.preferred_username.name(), userName);
            claims.add(Claims.sub.name(), userName);
            claims.add(CLAIMS_ORG_ID, organizationId);
            PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
            return Jwt.claims(claims.build()).sign(privateKey);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        CreateTemplateConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(CreateTemplateConfig.class);
        }
    }
}
