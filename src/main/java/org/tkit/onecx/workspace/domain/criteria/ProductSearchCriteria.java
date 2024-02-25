package org.tkit.onecx.workspace.domain.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCriteria {
    private String workspaceId;
    private String productName;
    private Integer pageNumber;
    private Integer pageSize;
}
