package org.tkit.onecx.workspace.rs.legacy.controllers;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigDocFilename("onecx-workspace-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "tkit.legacy")
public interface PortalConfig {

    /**
     * Enabled or disable menu auto registration.
     */
    @WithName("enable-menu-auto-registration")
    @WithDefault("false")
    boolean enableMenuAutoRegistration();
}
