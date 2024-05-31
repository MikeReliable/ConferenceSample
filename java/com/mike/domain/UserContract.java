package com.mike.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class UserContract implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractFilename;
    private String contractType;
    private String invoiceFilename;
    private String checkFilename;

    @ManyToOne
    @JoinTable(name = "user_contract_usr",
            joinColumns = {@JoinColumn(name = "user_contractn_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "usr_id", referencedColumnName = "id")})
    private User user;

    public UserContract() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractFilename() {
        return contractFilename;
    }

    public void setContractFilename(String contractFilename) {
        this.contractFilename = contractFilename;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getInvoiceFilename() {
        return invoiceFilename;
    }

    public void setInvoiceFilename(String invoiceFilename) {
        this.invoiceFilename = invoiceFilename;
    }

    public String getCheckFilename() {
        return checkFilename;
    }

    public void setCheckFilename(String checkFilename) {
        this.checkFilename = checkFilename;
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
        if (!(o instanceof UserContract)) return false;
        UserContract userContract = (UserContract) o;
        return Objects.equals(id, userContract.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
