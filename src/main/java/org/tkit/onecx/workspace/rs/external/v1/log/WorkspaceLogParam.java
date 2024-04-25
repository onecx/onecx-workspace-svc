package org.tkit.onecx.workspace.rs.external.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.GetWorkspaceByUrlRequestDTOV1;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspaceLoadRequestDTOV1;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;

@ApplicationScoped
public class WorkspaceLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(

                item(10, GetWorkspaceByUrlRequestDTOV1.class,
                        x -> GetWorkspaceByUrlRequestDTOV1.class.getSimpleName() + ":"
                                + ((GetWorkspaceByUrlRequestDTOV1) x).getUrl()),
                item(10, WorkspaceLoadRequestDTOV1.class,
                        x -> WorkspaceLoadRequestDTOV1.class.getSimpleName() + ":" + ((WorkspaceLoadRequestDTOV1) x).getPath()),
                item(10, WorkspaceSearchCriteriaDTOV1.class, x -> {
                    WorkspaceSearchCriteriaDTOV1 d = (WorkspaceSearchCriteriaDTOV1) x;
                    return WorkspaceSearchCriteriaDTOV1.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }));
    }
}
