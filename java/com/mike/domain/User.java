package com.mike.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usr")
public class User implements UserDetails, Comparable<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank(message = "Поле не может быть пустым")
    @Column(name = "username")
    private String email;
    @NotBlank(message = "Поле не может быть пустым")
    private String firstName;
    private String middleName;
    @NotBlank(message = "Поле не может быть пустым")
    private String lastName;
    @NotBlank(message = "Поле не может быть пустым")
    private String password;
    private String degree;
    private String position;
    private boolean young;
    private String telephone;
    private boolean active;
    private String activationCode;
    private String foto;

    private boolean blocked;
    private int actualYear;

    @Override
    public String getUsername() {
        return getEmail();
    }

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @ManyToOne
    @JoinTable(
            name = "user_affiliation",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "affiliation_id", referencedColumnName = "affiliationId")}
    )
    private Affiliation affiliation;

    @OneToMany(mappedBy = "user")
    private Set<Publication> publicationSet;

    @OneToMany(mappedBy = "user")
    private Set<Paper> paperSet;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_contract_usr",
            joinColumns = {@JoinColumn(name = "usr_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_contractn_id", referencedColumnName = "id")})
    private Set<UserContract> userContractSet;

    public User() {
    }

    public Long getId() {
        return id;
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

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isYoung() {
        return young;
    }

    public void setYoung(boolean young) {
        this.young = young;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public int getActualYear() {
        return actualYear;
    }

    public void setActualYear(int actualYear) {
        this.actualYear = actualYear;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    public Set<Publication> getPublicationSet() {
        return publicationSet;
    }

    public Set<Paper> getPaperSet() {
        return paperSet;
    }

    public Set<UserContract> getUserContractSet() {
        return userContractSet;
    }

    public void setUserContractSet(Set<UserContract> userContractSet) {
        this.userContractSet = userContractSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(User o) {
        return this.getLastName().compareTo(o.getLastName());
    }
}
