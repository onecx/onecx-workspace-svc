package org.tkit.onecx.workspace.rs.external.v1.mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {

    List<ProductDTOV1> map(List<Product> entity);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTOV1 map(Product entity);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTOV1 mapAbstractList(PageResult<Workspace> page);

    default WorkspaceAbstractDTOV1 mapAbstract(Workspace workspace) {
        WorkspaceAbstractDTOV1 abstractDTOV1 = new WorkspaceAbstractDTOV1();
        abstractDTOV1.setName(workspace.getName());
        abstractDTOV1.setTheme(workspace.getTheme());
        abstractDTOV1.setDescription(workspace.getDescription());
        abstractDTOV1.setProducts(workspace.getProducts().stream().map(Product::getProductName).toList());
        return abstractDTOV1;
    }

    @Mapping(target = "name", source = "workspaceName")
    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTOV1 criteria);

    @Mapping(target = "workspaceRoles", source = "roles")
    @Mapping(target = "removeWorkspaceRolesItem", ignore = true)
    WorkspaceDTOV1 map(Workspace workspace);

    @Mapping(target = "removeProductsItem", ignore = true)
    WorkspaceLoadDTOV1 load(Workspace workspace);

    default Set<String> roleMap(List<Role> roles) {
        return new HashSet<>(roles.stream().map(Role::getName).toList());
    }
}
