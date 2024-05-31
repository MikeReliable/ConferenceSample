package com.mike.service;

import java.util.Objects;

public class UserDTO {

    private String firstName;
    private String middleName;
    private String lastName;
    private String organizationShort;
    private String city;


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

    @Override
    public String toString() {
        return "{firstName\": \"" + firstName + "\", \"lastName\": \"" + lastName + "\", \"middleName\": \"" + middleName + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(firstName, userDTO.firstName) && Objects.equals(middleName, userDTO.middleName) && Objects.equals(lastName, userDTO.lastName) && Objects.equals(organizationShort, userDTO.organizationShort) && Objects.equals(city, userDTO.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName, organizationShort, city);
    }
}
