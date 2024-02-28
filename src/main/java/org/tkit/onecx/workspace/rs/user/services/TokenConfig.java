package org.tkit.onecx.workspace.rs.user.services;

import java.util.Optional;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.workspace.token")
public interface TokenConfig {

    @WithName("verified")
    @WithDefault("false")
    boolean verified();

    @WithName("issuer.public-key-location.suffix")
    @WithDefault("/protocol/openid-connect/certs")
    String publicKeyLocationSuffix();

    @WithName("issuer.public-key-location.enabled")
    @WithDefault("false")
    boolean publicKeyEnabled();

    @WithName("claim.separator")
    Optional<String> claimSeparator();

    @WithName("claim.path")
    @WithDefault("realm_access/roles")
    String claimPath();
}
