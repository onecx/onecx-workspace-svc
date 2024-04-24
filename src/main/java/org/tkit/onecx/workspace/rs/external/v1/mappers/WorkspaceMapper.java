package org.tkit.onecx.workspace.rs.external.v1.mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.domain.models.Role;
import org.tkit.onecx.workspace.domain.models.Slot;
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

    WorkspaceAbstractDTOV1 mapAbstract(Workspace workspace);

    @Mapping(target = "name", source = "workspaceName")
    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTOV1 criteria);

    @Mapping(target = "workspaceRoles", source = "roles")
    @Mapping(target = "removeWorkspaceRolesItem", ignore = true)
    @Mapping(target = "products", qualifiedByName = "products-to-string")
    @Mapping(target = "removeProductsItem", ignore = true)
    WorkspaceDTOV1 map(Workspace workspace);

    @Named("products-to-string")
    default Set<String> productsToString(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream().map(Product::getProductName).collect(Collectors.toSet());
    }

    @Mapping(target = "removeProductsItem", ignore = true)
    WorkspaceLoadDTOV1 load(Workspace workspace);

    @Mapping(target = "removeProductsItem", ignore = true)
    @Mapping(target = "removeSlotsItem", ignore = true)
    WorkspaceWrapperDTOV1 loadWrapper(Workspace workspace);

    @Mapping(target = "removeComponentsItem", ignore = true)
    WorkspaceWrapperSlotDTOV1 loadSlot(Slot slot);

    default Set<String> roleMap(List<Role> roles) {
        return new HashSet<>(roles.stream().map(Role::getName).toList());
    }
}
