package com.example.restservice;

//import java.io.*;
import org.json.*;

import java.util.ArrayList;
//import org.json.simple.JSONObject;

public class GSData {
	public int id;
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

	GSData(){
		this.title = "";
		this.year = -1;
	}

	GSData(String title, int year){
		this.title = title;
		this.year = year;
	}
	
	public String makeQueryUrl() {
		String url = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
		url += this.title.replace(" ", "+");
		url += "&btnG=";
		return url;
	}
	
	public String makeSourceUrl() {
		String url = "https://www.journal.org/";
		url += this.title.replace(" ", "+");
		url += ".com";
		return url;
	}
	
	public String makeDocUrl() {
		String url = "https://www.journal.org/";
		url += this.title.replace(" ", "+");
		url += ".pdf";
		return url;
	}
	public String makeCitedByUrl() {
		String url = "https://scholar.google.com/scholar?cites=";
		url += this.title.replace(" ", "+");
		url += "&as_sdt=5,43&sciodt=0,43&hl=en";
		return url;
	}
	//~String
	public JSONObject toJSON(GSData[] children){
		JSONObject[] childjson = new JSONObject[children.length];
		for(int i = 0; i < children.length; i++){
			//String[] other_authors = new String[children[i].authors.length];
			JSONObject jtemp = new JSONObject();
			for(int j = 0; j < children[i].authors.size(); j++){
				try {
					jtemp.put(children[i].authors.get(j), children[i].author_urls.get(j));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			String[] s = new String[0];
			try {
				childjson[i] = new JSONObject().put("authors_and_links", jtemp)
						.put("cited_by_count", children[i].cited_by_count)
						.put("cited_by_url", children[i].cited_by_url)
						.put("doc_url", children[i].doc_url)
						.put("referenced_by", s)
						.put("scholar_id", Integer.toString(children[i].id))
						.put("source_url", children[i].source_url)
						.put("summary_short", children[i].summary)
						.put("title_short", year);
			}
			catch (JSONException e){
				e.printStackTrace();
			}
		}
		JSONObject jsonResp = new JSONObject();
		try {
			jsonResp.put("id", Integer.toString(this.id))
					.put("papers", childjson);
		} catch (JSONException e){
			e.printStackTrace();
		} finally {
			//~return jsonResp.toString();
			return jsonResp;
		}
	}
}
