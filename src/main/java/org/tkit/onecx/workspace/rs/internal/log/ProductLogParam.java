package org.tkit.onecx.workspace.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.rs.internal.model.CreateProductRequestDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.UpdateProductRequestDTO;

@ApplicationScoped
public class ProductLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateProductRequestDTO.class,
                        x -> CreateProductRequestDTO.class.getSimpleName() + "["
                                + ((CreateProductRequestDTO) x).getProductName()
                                + "," + ((CreateProductRequestDTO) x).getBaseUrl() + ","
                                + (((CreateProductRequestDTO) x).getMicrofrontends() != null
                                        ? ((CreateProductRequestDTO) x).getMicrofrontends().size()
                                        : "null")
                                + "]"),
                this.item(10, UpdateProductRequestDTO.class,
                        x -> UpdateProductRequestDTO.class.getSimpleName() + "[" + ((UpdateProductRequestDTO) x).getBaseUrl()
                                + ","
                                + (((UpdateProductRequestDTO) x).getMicrofrontends() != null
                                        ? ((UpdateProductRequestDTO) x).getMicrofrontends().size()
                                        : "null")
                                + "]"));
    }
}
