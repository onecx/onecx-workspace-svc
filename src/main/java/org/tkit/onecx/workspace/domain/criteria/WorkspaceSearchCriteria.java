package org.tkit.onecx.workspace.domain.criteria;

import java.io.Serializable;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceSearchCriteria implements Serializable {

    private String name;

    private Set<String> names;

    private String themeName;

    private String baseUrl;

    private Integer pageNumber = 0;

    private Integer pageSize = 100;
}
