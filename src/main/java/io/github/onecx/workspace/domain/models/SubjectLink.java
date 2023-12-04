package io.github.onecx.workspace.domain.models;

import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class SubjectLink {

    private String label;
    private String url;
}
