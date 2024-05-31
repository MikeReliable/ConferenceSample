package com.mike.domain;

public enum ContractType {
    INDIVIDUAL ("ФЛ только оргвзнос"),
    INDIVIDUAL_PAPER ("ФЛ оргвзнос со статьей в RPJ"),
    ENTITY ("ЮЛ только оргвзнос"),
    ENTITY_PAPER ("ЮЛ оргвзнос со статьей в RPJ");

    private String contractType;

    ContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }
}
