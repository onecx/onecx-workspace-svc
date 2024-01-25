package io.github.onecx.workspace.domain.models;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import io.github.onecx.workspace.domain.models.enums.Scope;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "MENU_ITEM", indexes = {
        @Index(columnList = "WORKSPACE", name = "MENU_ITEM_WORKSPACE_IDX"),
        @Index(columnList = "PARENT", name = "MENU_ITEM_PARENT_IDX"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "MENU_ITEM_KEY_WORKSPACE", columnNames = { "KEY", "WORKSPACE", "TENANT_ID" })
})
@NamedEntityGraph(name = MenuItem.MENU_ITEM_WORKSPACE_AND_TRANSLATIONS, attributeNodes = { @NamedAttributeNode("i18n"),
        @NamedAttributeNode("workspace") })
@NamedEntityGraph(name = "MenuItem.loadById", includeAllAttributes = true, attributeNodes = { @NamedAttributeNode("i18n"),
        @NamedAttributeNode("children") })
@SuppressWarnings("squid:S2160")
public class MenuItem extends TraceableEntity {

    public static final String MENU_ITEM_WORKSPACE_AND_TRANSLATIONS = "MenuItem.workspaceAndTranslations";

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @ManyToOne(cascade = { REFRESH }, optional = false)
    @JoinColumn(name = "WORKSPACE")
    Workspace workspace;

    @Column(name = "KEY")
    private String key;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "URL")
    private String url;

    @Column(name = "WORKSPACE_NAME")
    private String workspaceName;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "DISABLED")
    @NotNull
    private boolean disabled;

    @Column(name = "POS")
    private int position;

    @Column(name = "PERMISSION_OBJECT")
    private String permission;

    @Column(name = "BADGE")
    private String badge;

    @Column(name = "SCOPE")
    @Enumerated(STRING)
    private Scope scope;

    @Column(name = "WORKSPACE_EXIT")
    @NotNull
    private boolean workspaceExit;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PARENT")
    private MenuItem parent;

    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "parent", orphanRemoval = true)
    @OrderBy("position")
    private Set<MenuItem> children = new HashSet<>();

    @ElementCollection
    @MapKeyColumn(name = "LANGUAGE")
    @Column(name = "i18n")
    @CollectionTable(name = "MENU_ITEM_I18N")
    private Map<String, String> i18n = new HashMap<>();
}
