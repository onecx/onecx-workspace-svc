package io.github.onecx.workspace.domain.models;

import static jakarta.persistence.FetchType.LAZY;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "WORKSPACE", uniqueConstraints = {
        @UniqueConstraint(name = "WORKSPACE_NAME_TENANT_ID", columnNames = { "WORKSPACE_NAME", "TENANT_ID" }),
        @UniqueConstraint(name = "WORKSPACE_BASE_URL", columnNames = { "BASE_URL" })
})
@NamedEntityGraph(name = Workspace.WORKSPACE_FULL, attributeNodes = { @NamedAttributeNode("subjectLinks"),
        @NamedAttributeNode("imageUrls"), @NamedAttributeNode(value = "products") })
@SuppressWarnings("squid:S2160")
public class Workspace extends TraceableEntity {

    public static final String WORKSPACE_FULL = "Workspace.full";

    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "WORKSPACE_NAME", nullable = false)
    private String workspaceName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "THEME")
    private String theme;

    @Column(name = "HOME_PAGE")
    private String homePage;

    @Column(name = "BASE_URL", unique = true)
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

    @ElementCollection(fetch = LAZY)
    @CollectionTable(name = "WS_ITEM_SUBJECT_LINKS")
    @AttributeOverride(name = "label", column = @Column(name = "link_label"))
    @AttributeOverride(name = "url", column = @Column(name = "link_url"))
    private Set<SubjectLink> subjectLinks = new HashSet<>();

    @ElementCollection(fetch = LAZY)
    @CollectionTable(name = "WS_ITEM_IMAGE_URLS")
    @Column(name = "IMAGE_URL")
    private Set<String> imageUrls = new HashSet<>();

    @Column(name = "WORKSPACE_ROLES", columnDefinition = "TEXT")
    private String workspaceRoles;

    @Column(name = "LOGO_URL")
    private String logoUrl;

    @OneToMany(fetch = LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "WORKSPACE_GUID")
    private List<Product> products;

}
