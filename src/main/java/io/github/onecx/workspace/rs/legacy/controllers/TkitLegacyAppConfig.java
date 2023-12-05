package io.github.onecx.workspace.rs.legacy.controllers;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "tkit.legacy")
public interface TkitLegacyAppConfig {

    @WithDefault("false")
    boolean enableMenuAutoRegistration();
}
