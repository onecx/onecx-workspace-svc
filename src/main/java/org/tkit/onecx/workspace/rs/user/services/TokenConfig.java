package org.tkit.onecx.workspace.rs.user.services;

import java.util.Optional;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.workspace")
public interface TokenConfig {
    /**
     *
     * @return token configs
     */
    @WithName("token")
    Token token();

    interface Token {
        /**
         *
         * @return verification status
         */
        @WithName("verified")
        @WithDefault("false")
        boolean verified();

        /**
         *
         * @return suffix of public key
         */
        @WithName("issuer.public-key-location.suffix")
        @WithDefault("/protocol/openid-connect/certs")
        String publicKeyLocationSuffix();

        /**
         *
         * @return status if public key is enabled
         */
        @WithName("issuer.public-key-location.enabled")
        @WithDefault("false")
        boolean publicKeyEnabled();

        /**
         *
         * @return separator
         */
        @WithName("claim.separator")
        Optional<String> claimSeparator();

        /**
         *
         * @return claim path
         */
        @WithName("claim.path")
        @WithDefault("realm_access/roles")
        String claimPath();
    }
}
