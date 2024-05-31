package com.mike.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "publication_co_author")
public class PublicationCoauthor implements Serializable, Comparable<PublicationCoauthor> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int place;
    private boolean speaker;

    @ManyToOne
    @JoinColumn(name = "publication_id")
    Publication publication;

    @ManyToOne
    @JoinColumn(name = "coauthor_affiliation_id")
    CoauthorAffiliation coauthorAffiliation;

    public PublicationCoauthor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public void setSpeaker(boolean speaker) {
        this.speaker = speaker;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public CoauthorAffiliation getCoauthorAffiliation() {
        return coauthorAffiliation;
    }

    public void setCoauthorAffiliation(CoauthorAffiliation coauthorAffiliation) {
        this.coauthorAffiliation = coauthorAffiliation;
    }

    @Override
    public int compareTo(PublicationCoauthor o) {
        int comparePlace = o.getPlace();
        return this.place - comparePlace;
    }
}
