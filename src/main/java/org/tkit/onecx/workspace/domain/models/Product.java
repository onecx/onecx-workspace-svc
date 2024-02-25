package org.tkit.onecx.workspace.domain.models;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PRODUCT", uniqueConstraints = {
        @UniqueConstraint(name = "PRODUCT_NAME_WORKSPACE_GUID", columnNames = { "PRODUCT_NAME", "WORKSPACE_GUID", "TENANT_ID" })
})
@NamedEntityGraph(name = "Product.loadById", includeAllAttributes = true, attributeNodes = { @NamedAttributeNode("workspace") })
@SuppressWarnings("squid:S2160")
public class Product extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "BASE_URL", unique = true)
    private String baseUrl;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "WORKSPACE_GUID")
    private Workspace workspace;

    @Column(name = "WORKSPACE_GUID", insertable = false, updatable = false)
    private String workspaceId;

    @OneToMany(fetch = EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PRODUCT_GUID")
    private List<Microfrontend> microfrontends;

    @PostPersist
    void postPersist() {
        workspaceId = workspace.getId();
    }

}
