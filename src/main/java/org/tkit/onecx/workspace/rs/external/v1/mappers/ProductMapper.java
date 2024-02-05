package org.tkit.onecx.workspace.rs.external.v1.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.workspace.domain.models.Product;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.ProductDTOV1;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {
    List<ProductDTOV1> map(List<Product> entity);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTOV1 map(Product entity);
}