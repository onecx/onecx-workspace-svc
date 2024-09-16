package org.tkit.onecx.workspace.domain.template.models;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Workspace template config.
 */
@StaticInitSafe
@ConfigDocFilename("onecx-workspace-svc.adoc")
@ConfigMapping(prefix = "onecx.workspace")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface CreateTemplateConfig {

    /**
     * Create template configuration.
     *
     * @return create template configuration.
     */
    @WithName("template.create")
    Create create();

    interface Create {

        /**
         * Enabled or enable create template configuration.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Create template resource.
         */
        @WithName("resource")
        @WithDefault("template/workspace-create.json")
        String resource();

        /**
         * Class-path resource
         */
        @WithName("class-path-resource")
        @WithDefault("true")
        boolean classPathResource();

        /**
         * Role mapping for create template.
         */
        @WithName("role-mapping")
        Map<String, String> roleMapping();
    }
}
