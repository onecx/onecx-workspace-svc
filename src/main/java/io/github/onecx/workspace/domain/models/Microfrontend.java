package io.github.onecx.workspace.domain.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MICROFRONTEND", uniqueConstraints = {
        @UniqueConstraint(name = "MFE_ID_PATH_PRODUCT_GUID", columnNames = { "MFE_ID", "BASE_PATH", "PRODUCT_GUID" })
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

}
