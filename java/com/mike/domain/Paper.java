package com.mike.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "paper")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paperId;

    @NotBlank(message = "Поле не может быть пустым")
    private String paperName;
    private String paperFilename;
    private String paperFilenamePdf;
    private String paperPermission;
    private String paperReview;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "state")
    @Enumerated(EnumType.STRING)
    private PublicationState state;

    public Paper() {
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getPaperFilename() {
        return paperFilename;
    }

    public void setPaperFilename(String paperFilename) {
        this.paperFilename = paperFilename;
    }

    public String getPaperFilenamePdf() {
        return paperFilenamePdf;
    }

    public void setPaperFilenamePdf(String paperFilenamePdf) {
        this.paperFilenamePdf = paperFilenamePdf;
    }

    public String getPaperPermission() {
        return paperPermission;
    }

    public void setPaperPermission(String paperPermission) {
        this.paperPermission = paperPermission;
    }

    public String getPaperReview() {
        return paperReview;
    }

    public void setPaperReview(String paperReview) {
        this.paperReview = paperReview;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PublicationState getState() {
        return state;
    }

    public void setState(PublicationState state) {
        this.state = state;
    }
}
