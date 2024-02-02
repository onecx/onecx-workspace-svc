package org.tkit.onecx.workspace.rs.exim.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.exim.v1.model.ExportWorkspacesRequestDTOV1;
import gen.org.tkit.onecx.workspace.rs.exim.v1.model.MenuSnapshotDTOV1;
import gen.org.tkit.onecx.workspace.rs.exim.v1.model.WorkspaceSnapshotDTOV1;

@ApplicationScoped
public class ExportImportLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ExportWorkspacesRequestDTOV1.class, x -> x.getClass().getSimpleName()),
                item(10, WorkspaceSnapshotDTOV1.class,
                        x -> x.getClass().getSimpleName() + ":" + ((WorkspaceSnapshotDTOV1) x).getId()),
                item(10, MenuSnapshotDTOV1.class,
                        x -> x.getClass().getSimpleName() + ":" + ((MenuSnapshotDTOV1) x).getId()));
    }
}
