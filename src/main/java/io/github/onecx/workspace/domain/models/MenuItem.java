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

import org.tkit.quarkus.jpa.models.TraceableEntity;

import io.github.onecx.workspace.domain.models.enums.Scope;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "WS_MENU_ITEM", indexes = {
        @Index(columnList = "ITEM_WORKSPACE", name = "PTL_MENU_ITEM_ITEM_WORKSPACE_IDX"),
        @Index(columnList = "ITEM_PARENT", name = "PTL_MENU_ITEM_ITEM_PARENT_IDX"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "PTL_MENU_ITEM_ITEM_KEY_WORKSPACE", columnNames = { "ITEM_KEY", "ITEM_WORKSPACE" })
})
@NamedEntityGraph(name = MenuItem.MENU_ITEM_WORKSPACE_AND_TRANSLATIONS, attributeNodes = { @NamedAttributeNode("i18n"),
        @NamedAttributeNode("workspace") })
@NamedEntityGraph(name = "MenuItem.loadById", includeAllAttributes = true, attributeNodes = { @NamedAttributeNode("i18n"),
        @NamedAttributeNode("children") })
@SuppressWarnings("squid:S2160")
public class MenuItem extends TraceableEntity {

    public static final String MENU_ITEM_WORKSPACE_AND_TRANSLATIONS = "MenuItem.workspaceAndTranslations";

    @ManyToOne(cascade = { REFRESH }, optional = false)
    @JoinColumn(name = "ITEM_WORKSPACE")
    Workspace workspace;

    @Column(name = "ITEM_KEY")
    private String key;

    @Column(name = "ITEM_NAME")
    private String name;

    @Column(name = "ITEM_DESCRIPTION")
    private String description;

    @Column(name = "ITEM_URL")
    private String url;

    @Column(name = "ITEM_WORKSPACE_NAME")
    private String workspaceName;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "ITEM_DISABLED")
    @NotNull
    private boolean disabled;

    @Column(name = "ITEM_POS")
    private int position;

    @Column(name = "ITEM_PERMISSION_OBJECT")
    private String permission;

    @Column(name = "ITEM_BADGE")
    private String badge;

    @Column(name = "ITEM_SCOPE")
    @Enumerated(STRING)
    private Scope scope;

    @Column(name = "WORKSPACE_EXIT")
    @NotNull
    private boolean workspaceExit;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ITEM_PARENT")
    private MenuItem parent;

    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "parent", orphanRemoval = true)
    @OrderBy("position")
    private Set<MenuItem> children = new HashSet<>();

    @ElementCollection(fetch = LAZY)
    @MapKeyColumn(name = "LANGUAGE")
    @CollectionTable(name = "PTL_MENU_ITEM_I18N")
    private Map<String, String> i18n = new HashMap<>();

    @Column(name = "ROLES", columnDefinition = "TEXT")
    private String roles;

}
