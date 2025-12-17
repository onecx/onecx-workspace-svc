package org.tkit.onecx.workspace.rs.user.services;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.workspace")
@ConfigDocFilename("onecx-workspace-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MenuMappingConfig {

    /**
     * User menu configuration.
     *
     */
    @WithName("user.menu")
    Menu menu();

    interface Menu {
        /**
         * Menu mapping keys
         */
        @WithName("mapping")
        Map<String, String> mapping();

    }
}
