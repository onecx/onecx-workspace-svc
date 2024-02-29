package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@ApplicationScoped
public class AssignmentLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, CreateAssignmentRequestDTO.class,
                        x -> CreateAssignmentRequestDTO.class.getSimpleName() + "["
                                + ((CreateAssignmentRequestDTO) x).getRoleId() + ","
                                + ((CreateAssignmentRequestDTO) x).getMenuItemId() + "]"),
                item(10, UpdateProductRequestDTO.class,
                        x -> UpdateProductRequestDTO.class.getSimpleName() + "[" + ((UpdateProductRequestDTO) x).getBaseUrl()
                                + "]"),
                item(10, AssignmentSearchCriteriaDTO.class, x -> {
                    AssignmentSearchCriteriaDTO d = (AssignmentSearchCriteriaDTO) x;
                    return AssignmentSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }));
    }
}
