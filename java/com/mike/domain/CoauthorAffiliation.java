package com.mike.domain;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "coauthor_affiliation")
public class CoauthorAffiliation {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coauthor_id")
    private Coauthor coauthor;

    @ManyToOne
    @JoinColumn(name = "affiliation_id")
    private Affiliation affiliation;

    @OneToMany(mappedBy = "coauthorAffiliation")
    Set<PublicationCoauthor> coauthorPlaceSet;

    public CoauthorAffiliation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Coauthor getCoauthor() {
        return coauthor;
    }

    public void setCoauthor(Coauthor coauthor) {
        this.coauthor = coauthor;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    public Set<PublicationCoauthor> getCoauthorPlaceSet() {
        return coauthorPlaceSet;
    }

    public void setCoauthorPlaceSet(Set<PublicationCoauthor> coauthorPlaceSet) {
        this.coauthorPlaceSet = coauthorPlaceSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoauthorAffiliation that = (CoauthorAffiliation) o;
        return Objects.equals(coauthor, that.coauthor) && Objects.equals(affiliation, that.affiliation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coauthor, affiliation);
    }
}
