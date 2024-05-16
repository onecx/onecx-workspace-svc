package org.tkit.onecx.workspace.domain.template.models;

import java.util.List;

import org.tkit.onecx.workspace.domain.models.*;

public record WorkspaceCreateTemplate(List<Role> roles, List<Slot> slots,
        List<Product> products, List<Assignment> assignments, List<MenuItem> menus) {
}
