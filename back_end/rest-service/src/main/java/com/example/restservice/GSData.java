package com.example.restservice;

//import java.io.*;
import org.json.*;

import java.util.ArrayList;
//import org.json.simple.JSONObject;

public class GSData {
	public int id;
	public String parentId;
	public String scholar_id;
	public String title = "";
	public int year = 0;
	public String summary = "";
	public String queryUrl;
	//public String[] authors;
	//public String[] author_urls;
	public ArrayList<String> authors;
	public ArrayList<String> author_urls;
	public GSData[] citers = new GSData[0];
	public String doc_url;
	public String source_url;
	public String cited_by_url;
	public int cited_by_count;
	public GSData[] fake;

	GSData(){
		this.title = "";
		this.year = -1;
	}

	GSData(String title, int year){
		this.title = title;
		this.year = year;
	}

	//Might be useful but we dont have to keep this
	public JSONObject toCitersJSON(GSData[] children){
		JSONArray childjson = new JSONArray();
		for(int i = 0; i < children.length; i++){
			JSONObject jtemp = new JSONObject();
			for(int j = 0; j < children[i].authors.size(); j++){
				try {
					jtemp.put(children[i].authors.get(j), children[i].author_urls.get(j));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				childjson.put (new JSONObject().put("authors_and_links", jtemp)
						.put("cited_by_count", children[i].cited_by_count)
						.put("cited_by_url", children[i].cited_by_url)
						.put("doc_url", children[i].doc_url)
						.put("referenced_by", new JSONArray())
						.put("scholar_id", children[i].scholar_id)
						.put("source_url", children[i].source_url)
						.put("summary_short", children[i].summary)
						.put("title_short", children[i].title)
						.put("year", children[i].year));
			}
			catch (JSONException e){
				e.printStackTrace();
			}
		}
		JSONObject jsonResp = new JSONObject();
		try {
			jsonResp.put("id", this.scholar_id)
					.put("papers", childjson);
		} catch (JSONException e){
			e.printStackTrace();
		} finally {
			return jsonResp;
		}
	}
}
