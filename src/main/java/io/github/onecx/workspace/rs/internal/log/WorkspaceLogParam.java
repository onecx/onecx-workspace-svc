package io.github.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class WorkspaceLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateWorkspaceRequestDTO.class,
                        x -> "CreateWorkspaceRequestDTO[ name: " + ((CreateWorkspaceRequestDTO) x).getName()
                                + ", baseUrl: " + ((CreateWorkspaceRequestDTO) x).getBaseUrl()
                                + ", company name: " + ((CreateWorkspaceRequestDTO) x).getCompanyName()
                                + " ]"),
                this.item(10, UpdateWorkspaceRequestDTO.class,
                        x -> "UpdateWorkspaceRequestDTO[ name: " + ((UpdateWorkspaceRequestDTO) x).getName()
                                + ", baseUrl: " + ((UpdateWorkspaceRequestDTO) x).getBaseUrl()
                                + ", company name: " + ((UpdateWorkspaceRequestDTO) x).getCompanyName()
                                + " ]")

        );
    }
}
