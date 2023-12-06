package io.github.onecx.workspace.rs.internal.mappers;

import java.util.List;

import org.mapstruct.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.domain.models.Microfrontend;
import io.github.onecx.workspace.domain.models.Product;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {

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
    Microfrontend create(CreateMicrofrontendDTO dto);

    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void update(UpdateProductRequestDTO dto, @MappingTarget Product product);

    @Mapping(target = "id", ignore = true)
    Microfrontend update(UpdateMicrofrontendDTO dto);

    List<ProductDTO> map(List<Product> entity);

    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(Product entity);

    MicrofrontendDTO map(Microfrontend entity);
}
