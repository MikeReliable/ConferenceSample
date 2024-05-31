package com.mike.domain;

public enum PublicationState {
    NEW("NEW"),CHECKED("CHECKED"),CORRECTED("CORRECTED"),ACCEPTED("ACCEPTED");

   private String state;

    PublicationState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
