package org.tkit.onecx.workspace.domain.criteria;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceSearchCriteria implements Serializable {

    private String name;

    private String themeName;

    private Integer pageNumber;

    private Integer pageSize;
}
