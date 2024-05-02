package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.criteria.ProductSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Microfrontend;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductPageResultDTO mapPage(PageResult<Product> page);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductResultDTO mapResult(Product product);

    ProductSearchCriteria map(ProductSearchCriteriaDTO dto);

    default Product create(CreateProductRequestDTO dto, Workspace workspace) {
        var result = create(dto);
        result.setWorkspace(workspace);
        return result;
    }

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Product create(CreateProductRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend create(CreateMicrofrontendDTO dto);

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "microfrontends", qualifiedByName = "updateListDTO")
    void update(UpdateProductRequestDTO dto, @MappingTarget Product product);

    @Named("updateListDTO")
    default List<Microfrontend> updateMicrofrontendList(List<UpdateMicrofrontendDTO> listToUpdate) {
        var list = new ArrayList<Microfrontend>();

        if (listToUpdate != null) {
            for (var mf : listToUpdate) {
                list.add(update(mf));
            }
        }

        return list;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Microfrontend update(UpdateMicrofrontendDTO dto);

    List<ProductDTO> map(List<Product> entity);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(Product entity);

    MicrofrontendDTO map(Microfrontend entity);
}
