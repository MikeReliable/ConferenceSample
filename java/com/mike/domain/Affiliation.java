package com.mike.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "affiliation")
public class Affiliation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long affiliationId;

    @NotBlank(message = "Поле не может быть пустым")
    private String organizationFull;
    @NotBlank(message = "Поле не может быть пустым")
    private String organizationShort;
    @NotBlank(message = "Поле не может быть пустым")
    private String city;
    @NotBlank(message = "Поле не может быть пустым")
    private String country;

    public Affiliation() {
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_affiliation",
            joinColumns = {@JoinColumn(name = "affiliation_id", referencedColumnName = "affiliationId")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}
    )
    private Set<User> userSet;

    @OneToMany(mappedBy = "affiliation")
    Set<CoauthorAffiliation> coauthorAffiliationSet;

    public Long getAffiliationId() {
        return affiliationId;
    }

    public void setAffiliationId(Long affiliationId) {
        this.affiliationId = affiliationId;
    }

    public String getOrganizationFull() {
        return organizationFull;
    }

    public void setOrganizationFull(String organizationFull) {
        this.organizationFull = organizationFull;
    }

    public String getOrganizationShort() {
        return organizationShort;
    }

    public void setOrganizationShort(String organizationShort) {
        this.organizationShort = organizationShort;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Set<User> getUserSet() {
        return userSet;
    }

    public void setUserSet(Set<User> userSet) {
        this.userSet = userSet;
    }

    public Set<CoauthorAffiliation> getCoauthorAffiliationSet() {
        return coauthorAffiliationSet;
    }

    public void setCoauthorAffiliationSet(Set<CoauthorAffiliation> coauthorAffiliationSet) {
        this.coauthorAffiliationSet = coauthorAffiliationSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Affiliation that = (Affiliation) o;
        return Objects.equals(affiliationId, that.affiliationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(affiliationId);
    }
}
