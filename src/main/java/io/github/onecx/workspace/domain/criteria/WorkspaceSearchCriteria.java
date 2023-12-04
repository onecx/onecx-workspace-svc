package io.github.onecx.workspace.domain.criteria;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceSearchCriteria implements Serializable {

    private String workspaceName;

    private String themeName;

    private Integer pageNumber;

    private Integer pageSize;
}
