package com.mike.domain;

public enum PresentationType {
    PLENARY("Пленарный"), INVITED("Приглашенный"), SECTION("Секционный"), POSTER("Постерный");

    private String presentationType;

    PresentationType(String string) {
        this.presentationType = string;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }
}
