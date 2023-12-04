package io.github.onecx.workspace.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class WorkspaceAddress {

    @Column(name = "street")
    private String street;

    @Column(name = "streetNo")
    private String streetNo;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "postalCode")
    private String postalCode;
}
