package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class WorkspaceLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateWorkspaceRequestDTO.class,
                        x -> CreateWorkspaceRequestDTO.class.getSimpleName() + "[" + ((CreateWorkspaceRequestDTO) x).getName()
                                + "]"),
                this.item(10, UpdateWorkspaceRequestDTO.class,
                        x -> UpdateWorkspaceRequestDTO.class.getSimpleName() + "[" + ((UpdateWorkspaceRequestDTO) x).getName()
                                + "]"),
                item(10, WorkspaceSearchCriteriaDTO.class, x -> {
                    WorkspaceSearchCriteriaDTO d = (WorkspaceSearchCriteriaDTO) x;
                    return WorkspaceSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }));
    }
}
