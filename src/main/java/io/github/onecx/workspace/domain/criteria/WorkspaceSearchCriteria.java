package io.github.onecx.workspace.domain.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceSearchCriteria {

    private String workspaceName;

    private String themeName;

    private Integer pageNumber;

    private Integer pageSize;
}