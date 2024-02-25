package org.tkit.onecx.workspace.domain.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleSearchCriteria {
    private String workspaceId;
    private String name;
    private String description;
    private Integer pageNumber;
    private Integer pageSize;
}
