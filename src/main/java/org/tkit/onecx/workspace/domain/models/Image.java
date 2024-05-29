package org.tkit.onecx.workspace.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "IMAGE", uniqueConstraints = {
        @UniqueConstraint(name = "IMAGE_CONSTRAINTS", columnNames = { "REF_ID", "REF_TYPE" })
})
@SuppressWarnings("squid:S2160")
public class Image extends TraceableEntity {

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "REF_TYPE")
    private String refType;

    @Column(name = "REF_ID")
    private String refId;

    @Column(name = "DATA_LENGTH")
    private Integer length;

    @Column(name = "DATA")
    private byte[] imageData;

    /**
     * Flag to identify created by an operator
     */
    @Column(name = "OPERATOR")
    private Boolean operator;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;
}
