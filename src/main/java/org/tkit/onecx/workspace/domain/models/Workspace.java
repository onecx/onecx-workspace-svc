package org.tkit.onecx.workspace.domain.models;

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
@Table(name = "WORKSPACE", uniqueConstraints = {
        @UniqueConstraint(name = "WORKSPACE_NAME_TENANT_ID", columnNames = { "NAME", "TENANT_ID" }),
        @UniqueConstraint(name = "WORKSPACE_BASE_URL_TENANT_ID", columnNames = { "BASE_URL", "TENANT_ID" })
})
@NamedEntityGraph(name = Workspace.WORKSPACE_FULL, includeAllAttributes = true)
@NamedEntityGraph(name = Workspace.WORKSPACE_PRODUCTS, attributeNodes = { @NamedAttributeNode(value = "products") })
@NamedEntityGraph(name = Workspace.WORKSPACE_PRODUCTS_SLOTS, attributeNodes = { @NamedAttributeNode(value = "products"),
        @NamedAttributeNode(value = "slots") })
@SuppressWarnings("squid:S2160")
public class Workspace extends TraceableEntity {

    public static final String WORKSPACE_FULL = "Workspace.full";
    public static final String WORKSPACE_PRODUCTS = "Workspace.products";
    public static final String WORKSPACE_PRODUCTS_SLOTS = "Workspace.products_slots";

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "MANDATORY")
    private Boolean mandatory;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DISPLAY_NAME", nullable = false)
    private String displayName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "THEME")
    private String theme;

    @Column(name = "HOME_PAGE")
    private String homePage;

    @Column(name = "BASE_URL", nullable = false)
    private String baseUrl;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Embedded
    private WorkspaceAddress address;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "RSS_FEED_URL")
    private String rssFeedUrl;

    @Column(name = "FOOTER_LABEL")
    private String footerLabel;

    @Column(name = "ROLES")
    @OneToMany(mappedBy = "workspace", fetch = LAZY, cascade = { CascadeType.REMOVE, CascadeType.PERSIST })
    private List<Role> roles;

    @Column(name = "LOGO_URL")
    private String logoUrl;

    @OneToMany(mappedBy = "workspace", fetch = LAZY, cascade = { CascadeType.REMOVE })
    private List<Product> products;

    @OneToMany(mappedBy = "workspace", fetch = LAZY, cascade = { CascadeType.REMOVE })
    private List<Slot> slots;

    /**
     * Flag to identify created by an operator
     */
    @Column(name = "OPERATOR")
    private Boolean operator;
}
