package org.tkit.onecx.workspace.domain.di.models;

import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigDocFilename("onecx-workspace-svc.adoc")
@ConfigMapping(prefix = "onecx.workspace.template.import")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TemplateConfig {

    /**
     * Role mapping for the template import
     */
    @WithName("role-mapping")
    Map<String, String> roleMapping();

    /**
     * Template import tenants
     */
    @WithName("tenants")
    @WithDefault("default")
    List<String> tenants();

}
