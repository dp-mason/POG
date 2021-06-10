package com.example.restservice;

// This Class corresponds wit he "ShortPaperInfo" Class in the Python program

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
//import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;



import java.util.Hashtable;

public class PaperInfo {
    String title_short;
    Hashtable<String, String> authors_and_links;
    Long year;
    String summary_short;
    String source_url;
    String scholar_id;
    String cited_by_url;
    Long cited_by_count;
    //Vector<PaperInfo> referenced_by;
    String doc_url;

    public void initFromJSON(String file_name) throws IOException, ParseException {
        // TODO: THIS IS NAIIVE, only sends back our predetermined output.JSON, get it to work with the python output
        Object obj = new JSONParser().parse(new FileReader(file_name));
        // typecasting obj to JSONObject
        JSONObject parsed_object = (JSONObject) obj;

        //TODO: put this in its own function in the PaperInfo class?
        this.cited_by_count = (Long) parsed_object.get("cited_by_count");
        this.scholar_id = (String) parsed_object.get("scholar_id");
        this.year = (Long) parsed_object.get("year");
        this.title_short = (String) parsed_object.get("title_short");
        //this.authors_and_links = (Hashtable<String, String>) parsed_object.get("authors_and_links");
        this.summary_short = (String) parsed_object.get("summary_short");
        this.source_url = (String) parsed_object.get("source_url");
        this.scholar_id = (String) parsed_object.get("scholar_id");
        this.cited_by_url = (String) parsed_object.get("cited_by_url");
        this.cited_by_count = (Long) parsed_object.get("cited_by_count");
        //this.referenced_by;
        this.doc_url = (String) parsed_object.get("doc_url");
    }

}
