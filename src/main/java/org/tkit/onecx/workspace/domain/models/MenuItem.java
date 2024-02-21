package org.tkit.onecx.workspace.domain.models;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

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
        @NamedAttributeNode("children"), @NamedAttributeNode("workspace") })
@NamedEntityGraph(name = "MenuItem.loadChildren", includeAllAttributes = true, attributeNodes = {
        @NamedAttributeNode("children") })
@SuppressWarnings("squid:S2160")
public class MenuItem extends TraceableEntity {

    public static final String MENU_ITEM_WORKSPACE_AND_TRANSLATIONS = "MenuItem.workspaceAndTranslations";

    public static final String MENU_ITEM_LOAD_ALL = "MenuItem.loadById";

    public static final String MENU_ITEM_LOAD_CHILDREN = "MenuItem.loadChildren";

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @ManyToOne(cascade = { REFRESH }, optional = false, fetch = LAZY)
    @JoinColumn(name = "WORKSPACE")
    Workspace workspace;

    @Column(name = "WORKSPACE", insertable = false, updatable = false)
    private String workspaceId;

    @Column(name = "KEY")
    private String key;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "URL")
    private String url;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "DISABLED")
    private boolean disabled;

    @Column(name = "POS")
    private int position;

    @Column(name = "BADGE")
    private String badge;

    @Column(name = "SCOPE")
    @Enumerated(STRING)
    private Scope scope;

    @Column(name = "EXTERNAL")
    private boolean external;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PARENT")
    private MenuItem parent;

    @Column(name = "PARENT", insertable = false, updatable = false)
    private String parentId;

    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "parent", orphanRemoval = true)
    @OrderBy("position")
    private Set<MenuItem> children = new HashSet<>();

    @ElementCollection
    @MapKeyColumn(name = "LANGUAGE")
    @Column(name = "i18n")
    @CollectionTable(name = "MENU_ITEM_I18N")
    private Map<String, String> i18n = new HashMap<>();

    @PostPersist
    void postPersist() {
        if (parent != null) {
            parentId = parent.getId();
        }
        workspaceId = workspace.getId();
    }

    public enum Scope {

        WORKSPACE,
        APP,
        PAGE
    }
}
