package io.github.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.rs.internal.model.CreateMenuItemDTO;
import gen.io.github.onecx.workspace.rs.internal.model.MenuItemDTO;
import gen.io.github.onecx.workspace.rs.internal.model.WorkspaceMenuItemStructrueDTO;

@ApplicationScoped
public class MenuItemLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, MenuItemDTO.class,
                        x -> "MenuItem[ key: " + ((MenuItemDTO) x).getKey()
                                + ", workspaceName: " + ((MenuItemDTO) x).getWorkspaceName() + " ]"),
                this.item(10, CreateMenuItemDTO.class,
                        x -> "CreateMenuItemDTO[ key: " + ((CreateMenuItemDTO) x).getKey()
                                + ", url: " + ((CreateMenuItemDTO) x).getUrl() + " ]"),
                this.item(10, WorkspaceMenuItemStructrueDTO.class,
                        x -> "WorkspaceMenuItemStructrueDTO[ menu items size: "
                                + (((WorkspaceMenuItemStructrueDTO) x).getMenuItems() != null
                                        ? ((WorkspaceMenuItemStructrueDTO) x).getMenuItems().size()
                                        : "null")
                                + " ]")

        );

    }
}
