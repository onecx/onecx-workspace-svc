package io.github.onecx.workspace.rs.legacy.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;

@ApplicationScoped
public class TkitPortalLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, MenuRegistrationRequestDTO.class,
                        x -> "MenuRegistrationRequestDTO[ request version: "
                                + ((MenuRegistrationRequestDTO) x).getRequestVersion()
                                + "menu items size: "
                                + (((MenuRegistrationRequestDTO) x).getMenuItems() != null
                                        ? ((MenuRegistrationRequestDTO) x).getMenuItems().size()
                                        : "null")
                                + " ]"));
    }

}
