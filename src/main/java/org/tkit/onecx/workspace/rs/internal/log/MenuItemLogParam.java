package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class MenuItemLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, UpdateMenuItemsRequestDTO.class,
                        x -> {
                            var items = ((UpdateMenuItemsRequestDTO) x).getItems();
                            return UpdateMenuItemsRequestDTO.class.getSimpleName() + "["
                                    + (items != null ? items.size() : "null") + "]";
                        }),
                this.item(10, UpdateMenuItemRequestDTO.class,
                        x -> UpdateMenuItemRequestDTO.class.getSimpleName() + "[" + ((UpdateMenuItemRequestDTO) x).getKey()
                                + "]"),
                this.item(10, CreateMenuItemDTO.class,
                        x -> CreateMenuItemDTO.class.getSimpleName() + "[" + ((CreateMenuItemDTO) x).getKey()
                                + "," + ((CreateMenuItemDTO) x).getUrl() + "]"),
                this.item(10, WorkspaceMenuItemStructureDTO.class,
                        x -> WorkspaceMenuItemStructureDTO.class.getSimpleName() + "["
                                + (((WorkspaceMenuItemStructureDTO) x).getMenuItems() != null
                                        ? ((WorkspaceMenuItemStructureDTO) x).getMenuItems().size()
                                        : "null")
                                + "]")

        );

    }
}
