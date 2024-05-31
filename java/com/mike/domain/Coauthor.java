package com.mike.domain;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "co_author")
public class Coauthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coAuthorId;
    private String firstName;
    private String middleName;
    private String lastName;

    @OneToMany(mappedBy = "coauthor")
    Set<CoauthorAffiliation> coauthorAffiliationSet;

    public Coauthor() {
    }

    public Coauthor(String firstName, String middleName, String lastName) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public Long getCoAuthorId() {
        return coAuthorId;
    }

    public void setCoAuthorId(Long coAuthorId) {
        this.coAuthorId = coAuthorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
        Coauthor coauthor = (Coauthor) o;
        return Objects.equals(coAuthorId, coauthor.coAuthorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coAuthorId);
    }
}
