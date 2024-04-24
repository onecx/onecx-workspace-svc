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
@Table(name = "SLOT", uniqueConstraints = {
        @UniqueConstraint(name = "SLOT_WORKSPACE_NAME", columnNames = { "NAME", "WORKSPACE_GUID", "TENANT_ID" })
})
public class Slot extends TraceableEntity {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "WORKSPACE_GUID")
    private Workspace workspace;

    @Column(name = "WORKSPACE_GUID", insertable = false, updatable = false)
    private String workspaceId;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "COMPONENT", nullable = false)
    @ElementCollection
    @CollectionTable(name = "SLOT_COMPONENTS", joinColumns = @JoinColumn(name = "SLOT_GUID"))
    @OrderColumn(name = "INDEX")
    private List<String> components;

    @PostPersist
    void postPersist() {
        workspaceId = workspace.getId();
    }
}
