package com.mike.service;

import java.util.List;

public class PublicationCoauthorsDTO {

    private int num;
    private boolean maxNum;
    private int place;
    private String organizationShort;
    private String city;
    private String firstName;
    private String middleName;
    private String lastName;
    private boolean doubleCoautor;
    private boolean speaker;
    private List<Integer> numList;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isMaxNum() {
        return maxNum;
    }

    public void setMaxNum(boolean maxNum) {
        this.maxNum = maxNum;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
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

    public boolean isDoubleCoautor() {
        return doubleCoautor;
    }

    public void setDoubleCoautor(boolean doubleCoautor) {
        this.doubleCoautor = doubleCoautor;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public void setSpeaker(boolean speaker) {
        this.speaker = speaker;
    }

    public List<Integer> getNumList() {
        return numList;
    }

    public void setNumList(List<Integer> numList) {
        this.numList = numList;
    }
}
