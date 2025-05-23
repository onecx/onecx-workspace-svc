package org.tkit.onecx.workspace.rs.user.services;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.workspace")
@ConfigDocFilename("onecx-workspace-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TokenConfig {

    /**
     * Token configuration.
     *
     * @return token configuration.
     */
    @WithName("token")
    Config config();

    interface Config {
        /**
         * Verification status
         */
        @WithName("verified")
        @WithDefault("false")
        boolean verified();

        /**
         * Suffix of public key
         */
        @WithName("issuer.public-key-location.suffix")
        @WithDefault("/protocol/openid-connect/certs")
        String publicKeyLocationSuffix();

        /**
         * Status if public key is enabled
         */
        @WithName("issuer.public-key-location.enabled")
        @WithDefault("false")
        boolean publicKeyEnabled();

        /**
         * Separator
         */
        @WithName("claim.separator")
        Optional<String> claimSeparator();

        /**
         * Claim path
         */
        @WithName("claim.path")
        @WithDefault("realm_access/roles")
        String claimPath();
    }
}
