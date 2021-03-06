package com.example.restservice;

public class ScholarHtml {
    private String raw_html;

    public ScholarHtml(String received_html){
        this.raw_html = received_html;
    }

    public String getRaw_html() {
        return raw_html;
    }
}
