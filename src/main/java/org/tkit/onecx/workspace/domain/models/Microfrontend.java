package org.tkit.onecx.workspace.domain.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MICROFRONTEND", uniqueConstraints = {
        @UniqueConstraint(name = "UI_PRODUCT_BASE_PATH", columnNames = { "PRODUCT_GUID", "BASE_PATH" })
})
@SuppressWarnings("squid:S2160")
public class Microfrontend implements Serializable {

    @Id
    @Column(name = "GUID")
    private String id = UUID.randomUUID().toString();

    @Column(name = "MFE_ID", nullable = false)
    private String mfeId;

    @Column(name = "BASE_PATH", nullable = false)
    private String basePath;

    @Column(name = "PRODUCT_GUID", insertable = false, updatable = false)
    private String productId;

    @Column(name = "EXPOSED_MODULE")
    private String exposedModule;

}
