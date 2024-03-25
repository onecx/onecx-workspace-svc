package org.tkit.onecx.workspace.domain.models;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ROLE", uniqueConstraints = {
        @UniqueConstraint(name = "UI_WORKSPACE_ROLE_NAME", columnNames = { "WORKSPACE_GUID", "NAME" })
})
@NamedEntityGraph(name = Role.ROLE_LOAD, attributeNodes = { @NamedAttributeNode("workspace") })
@SuppressWarnings("java:S2160")
public class Role extends TraceableEntity {

    public static final String ROLE_LOAD = "Role.load";

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "WORKSPACE_GUID")
    private Workspace workspace;

    @Column(name = "WORKSPACE_GUID", insertable = false, updatable = false)
    private String workspaceId;

    /**
     * The role name.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * The role description.
     */
    @Column(name = "DESCRIPTION")
    private String description;

    @PostPersist
    void postPersist() {
        workspaceId = workspace.getId();
    }
}
