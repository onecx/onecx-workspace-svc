package io.github.onecx.workspace.domain.models;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class SubjectLink implements Serializable {

    private String label;
    private String url;
}
