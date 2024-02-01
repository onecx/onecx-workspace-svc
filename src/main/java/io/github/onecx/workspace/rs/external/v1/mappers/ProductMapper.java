package io.github.onecx.workspace.rs.external.v1.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.external.v1.model.ProductDTOV1;
import io.github.onecx.workspace.domain.models.Product;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {
    List<ProductDTOV1> map(List<Product> entity);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTOV1 map(Product entity);
}
