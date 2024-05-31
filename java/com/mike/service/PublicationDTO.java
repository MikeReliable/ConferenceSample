package com.mike.service;

import java.util.List;

public class PublicationDTO {

    private Long publicationId;
    private boolean acceptThesis;
    private String publicationName;
    private String publicationSection;
    private String publicationType;
    private String publicationFilename;
    private String publicationPermission;
    private List<PublicationCoauthorsDTO> publicationCoauthorsDTOList;

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }


    public boolean isAcceptThesis() {
        return acceptThesis;
    }

    public void setAcceptThesis(boolean acceptThesis) {
        this.acceptThesis = acceptThesis;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }


    public String getPublicationSection() {
        return publicationSection;
    }

    public void setPublicationSection(String publicationSection) {
        this.publicationSection = publicationSection;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getPublicationFilename() {
        return publicationFilename;
    }

    public void setPublicationFilename(String publicationFilename) {
        this.publicationFilename = publicationFilename;
    }

    public String getPublicationPermission() {
        return publicationPermission;
    }

    public void setPublicationPermission(String publicationPermission) {
        this.publicationPermission = publicationPermission;
    }

    public List<PublicationCoauthorsDTO> getPublicationCoauthorsDTOList() {
        return publicationCoauthorsDTOList;
    }

    public void setPublicationCoauthorsDTOList(List<PublicationCoauthorsDTO> publicationCoauthorsDTOList) {
        this.publicationCoauthorsDTOList = publicationCoauthorsDTOList;
    }
}
