package org.tkit.onecx.workspace.domain.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class WorkspaceTranslationKey implements Serializable {

    @Column(name = "LANGUAGE", nullable = false)
    private String language;

    @Column(name = "FIELD_KEY", nullable = false)
    private String fieldKey;
}
