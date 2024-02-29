package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class RoleLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, CreateRoleRequestDTO.class,
                        x -> CreateRoleRequestDTO.class.getSimpleName() + "["
                                + ((CreateRoleRequestDTO) x).getName() + ","
                                + ((CreateRoleRequestDTO) x).getWorkspaceId() + "]"),
                item(10, UpdateRoleRequestDTO.class,
                        x -> UpdateRoleRequestDTO.class.getSimpleName() + "[" + ((UpdateRoleRequestDTO) x).getName()
                                + "]"),
                item(10, RoleSearchCriteriaDTO.class, x -> {
                    RoleSearchCriteriaDTO d = (RoleSearchCriteriaDTO) x;
                    return RoleSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }));
    }

}
