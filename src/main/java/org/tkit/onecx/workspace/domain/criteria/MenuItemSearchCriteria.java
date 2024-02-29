package org.tkit.onecx.workspace.domain.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItemSearchCriteria {
    private String workspaceId;
    private Integer pageNumber;
    private Integer pageSize;
}
