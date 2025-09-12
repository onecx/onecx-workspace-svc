package org.tkit.onecx.workspace.rs.admin.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.admin.v1.model.CreateWorkspaceRequestDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.UpdateWorkspaceRequestDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.WorkspaceSearchCriteriaDTOAdminV1;

@ApplicationScoped
public class WorkspaceAdminLogParamV1 implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateWorkspaceRequestDTOAdminV1.class,
                        x -> CreateWorkspaceRequestDTOAdminV1.class.getSimpleName() + "["
                                + ((CreateWorkspaceRequestDTOAdminV1) x).getName()
                                + "]"),
                this.item(10, UpdateWorkspaceRequestDTOAdminV1.class,
                        x -> UpdateWorkspaceRequestDTOAdminV1.class.getSimpleName() + "["
                                + ((UpdateWorkspaceRequestDTOAdminV1) x).getResource().getName()
                                + "]"),
                item(10, WorkspaceSearchCriteriaDTOAdminV1.class, x -> {
                    WorkspaceSearchCriteriaDTOAdminV1 d = (WorkspaceSearchCriteriaDTOAdminV1) x;
                    return WorkspaceSearchCriteriaDTOAdminV1.class.getSimpleName() + "[" + d.getPageNumber() + ","
                            + d.getPageSize()
                            + "]";
                }));
    }
}
