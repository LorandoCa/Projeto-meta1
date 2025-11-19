package com.example.servingwebcontent.forms;

import java.io.Serializable;

public class SearchForm implements Serializable {
    private Long id;
    private String word;

    public SearchForm() {}

    public SearchForm(Long id, String word) {
        this.id = id;
        this.word = word;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }

}