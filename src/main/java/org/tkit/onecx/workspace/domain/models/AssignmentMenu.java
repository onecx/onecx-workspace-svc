package org.tkit.onecx.workspace.domain.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AssignmentMenu(String menuItemId, String roleName) {
}
