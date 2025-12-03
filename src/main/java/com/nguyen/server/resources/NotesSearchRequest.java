package com.nguyen.server.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotesSearchRequest {
    @JsonProperty("keyword")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}