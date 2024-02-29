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
                this.item(10, UpdateMenuItemParentRequestDTO.class,
                        x -> UpdateMenuItemParentRequestDTO.class.getSimpleName() + "["
                                + ((UpdateMenuItemParentRequestDTO) x).getParentItemId()
                                + "]"),
                this.item(10, UpdateMenuItemRequestDTO.class,
                        x -> UpdateMenuItemRequestDTO.class.getSimpleName() + "[" + ((UpdateMenuItemRequestDTO) x).getKey()
                                + "]"),
                this.item(10, CreateMenuItemDTO.class,
                        x -> CreateMenuItemDTO.class.getSimpleName() + "[" + ((CreateMenuItemDTO) x).getKey() + "]"),

                item(10, MenuStructureSearchCriteriaDTO.class,
                        x -> MenuStructureSearchCriteriaDTO.class.getSimpleName() + "["
                                + ((MenuStructureSearchCriteriaDTO) x).getWorkspaceId() + "]"),

                item(10, MenuItemSearchCriteriaDTO.class, x -> {
                    MenuItemSearchCriteriaDTO d = (MenuItemSearchCriteriaDTO) x;
                    return MenuItemSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }));
    }
}
