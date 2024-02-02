package org.tkit.onecx.workspace.domain.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class WorkspaceAddress implements Serializable {

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
