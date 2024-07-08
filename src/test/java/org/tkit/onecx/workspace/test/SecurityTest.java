package org.tkit.onecx.workspace.test;

import java.util.List;

import org.tkit.quarkus.security.test.AbstractSecurityTest;
import org.tkit.quarkus.security.test.SecurityTestConfig;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SecurityTest extends AbstractSecurityTest {
    @Override
    public SecurityTestConfig getConfig() {
        SecurityTestConfig testConfig = new SecurityTestConfig();
        testConfig.addConfig("read", "/internal/workspaces/id", 404, List.of("ocx-ws:all"), "get");
        testConfig.addConfig("write", "/internal/workspaces", 400, List.of("ocx-ws:all"), "post");
        testConfig.addConfig("delete", "/internal/workspaces/id", 204, List.of("ocx-ws:all"), "delete");
        return testConfig;
    }
}
