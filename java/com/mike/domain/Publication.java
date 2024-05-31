package com.mike.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "publication")
public class Publication implements Comparable<Publication> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long publicationId;

    @NotBlank(message = "Поле не может быть пустым")
    private String publicationName;
    private String filename;
    private String permission;
    private String poster;
    private boolean acceptThesis;

    @Column(name = "publ_section")
    private String sectionName;

    @Column(name = "publ_type")
    private String presentationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "publication")
    private Set<PublicationCoauthor> publicationCoauthors;

    public Publication() {
    }

    public Publication(Long publicationId) {
        this.publicationId = publicationId;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public boolean isAcceptThesis() {
        return acceptThesis;
    }

    public void setAcceptThesis(boolean acceptThesis) {
        this.acceptThesis = acceptThesis;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public Set<PublicationCoauthor> getPublicationCoauthors() {
        return publicationCoauthors;
    }

    public void setPublicationCoauthors(Set<PublicationCoauthor> publicationCoauthors) {
        this.publicationCoauthors = publicationCoauthors;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Publication that = (Publication) o;
        return Objects.equals(publicationId, that.publicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId);
    }

    @Override
    public int compareTo(Publication o) {
        return this.getUser().getLastName().compareTo(o.getUser().getLastName());
    }
}
