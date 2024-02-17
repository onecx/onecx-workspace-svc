package org.tkit.onecx.workspace.domain.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentSearchCriteria {

    private String[] menuItemId;
    private Integer pageNumber;
    private Integer pageSize;
}
