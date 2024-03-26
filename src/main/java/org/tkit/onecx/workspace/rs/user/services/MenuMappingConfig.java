package org.tkit.onecx.workspace.rs.user.services;

import java.util.Map;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.workspace")
public interface MenuMappingConfig {
    /**
     *
     * @return user configs
     */
    @WithName("user.menu")
    UserConfig userConfig();

    interface UserConfig {
        /**
         *
         * @return menu mapping keys
         */
        @WithName("mapping")
        Map<String, String> mapping();
    }
}
