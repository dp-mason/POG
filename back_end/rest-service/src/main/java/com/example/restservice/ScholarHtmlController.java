package com.example.restservice;

import org.apache.commons.io.IOUtils;
//import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import org.json.simple.JSONArray;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.concurrent.atomic.AtomicLong;

@CrossOrigin
@RestController
@RequestMapping("/papers")
public class ScholarHtmlController {

	//private final String raw_scholar_html;

	// Gives this interaction a unique ID? so for each interaction there is a unique incremented number
	private final AtomicLong counter = new AtomicLong();

	//public ScholarHtmlController(String raw_scholar_html) {
	//    this.raw_scholar_html = raw_scholar_html;
	//}

	// A POST request on "www.someurl.com/papers" will trigger this controller.
	//      the post request body should contain the entire raw html for the google scholar search result page
	//      or or "cited by" page for a paper
	// Eventually we want this to return a JSON with parsed paper info, but for now "void" is ok
	@CrossOrigin
	@PostMapping(value = "/submitPaper"/*, produces = "application/json"*/)
	public @ResponseBody JSONObject Recv_Paper_Html(@RequestBody String user_html) throws IOException, ParseException, InterruptedException {

		String parent_id = user_html.substring(0,user_html.indexOf("---"));
		
		//TODO: should this be done on the javascript side instead? it would be less latency transferring over network
		// trim down the raw html so it can be sent over socket connection and faster parsing
		user_html = user_html.substring(user_html.indexOf("<div id=\"gs_bdy_ccl\" role=\"main\">"), user_html.indexOf("<div id=\"gs_res_ccl_bot\">"));
		
		String parsed_paper_info = "void";
		PaperInfo user_paper = new PaperInfo();

		// open socket used to communicate with the python parser server
		//	https://stackoverflow.com/questions/48266026/socket-java-client-python-server


		
		try{
			// open the socket
			Socket socket = new Socket("localhost",2022);  

			DataOutputStream dout = new DataOutputStream(socket.getOutputStream());  
			DataInputStream din = new DataInputStream(socket.getInputStream());

			// send the raw html as a string over the socket
			dout.writeUTF(user_html);
			dout.flush();

			System.out.println("send first mess");
			
			// read in the parsed json output from the socket
			parsed_paper_info = din.readUTF();//in.readLine();

			System.out.println("Message"+parsed_paper_info);
			
			Thread.sleep(4000);

			dout.close();  
			din.close();
			socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		// send the html that was received to the python server

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(parsed_paper_info);
		JSONObject result_json = (JSONObject) obj;

		
		// Initialize the data structure that will be used to enter stuff into SQL database
		// Loop over all papers on this page
		//      [papers at index i].initFromJSON("output.json");

		//TODO: AT THIS POINT OBJECT "obj" CONTAINS ALL OF THE DATA FROM THE PARSED JSON FILE ABOUT THE CITERS, 
		//TODO: FUNCTION/CLASS THAT ABSTRACTS AWAY THE DATABASE STUFF?
		//TODO: parent_id stores the id of parent of the papers in the json file


		JSONArray papers = (JSONArray) result_json.get("papers");

		GSData gsd;
		DBAccesser dba = new DBAccesser();

		for(Object paper : papers){
			System.out.println("Processing paper...");
			JSONObject paper_json = (JSONObject) paper;
			//gsd = new GSData(((String) paper.get("title_short")), ((int)((Long) paper.get("year"))));
			int parsed_yr = -2; 
			try{
				parsed_yr = Integer.parseInt(paper_json.get("year").toString());	
			}
			catch(NumberFormatException e){
				parsed_yr = -2;
			}
			catch(NullPointerException e){
				parsed_yr = -2;
			}

			String parsed_title = "foo";
			try{
				parsed_title = paper_json.get("title_short").toString();	
			}
			catch(NullPointerException e){
				continue;
			}
			gsd = new GSData(parsed_title, parsed_yr);

			gsd.cited_by_count = Integer.parseInt(paper_json.get("cited_by_count").toString());
			gsd.scholar_id = paper_json.get("scholar_id").toString();
			//this.year = (Long) parsed_object.get("year");
			//this.title_short = (String) parsed_object.get("title_short");
			JSONArray authors_and_links_arr = (JSONArray) paper_json.get("authors_and_links");
			ArrayList author_names = new ArrayList();
			ArrayList author_urls = new ArrayList();
			for (Object author_obj : authors_and_links_arr){
				Object[] author = ((JSONArray) author_obj).toArray();

				author_names.add(author[0].toString());
				author_urls.add(author[1].toString());
			}
			gsd.authors = author_names;
			gsd.author_urls = author_urls;
			//authors_and_links = (Hashtable<String, String>) parsed_object.get("authors_and_links");
			gsd.summary = paper_json.get("summary_short").toString();
			gsd.source_url = paper_json.get("source_url").toString();
			//gsd.scholar_id = (String) paper.get("scholar_id");
			gsd.cited_by_url = paper_json.get("cited_by_url").toString();
			//gsd.cited_by_count = (Long) paper.get("cited_by_count");
			///this.referenced_by;
			gsd.doc_url = paper_json.get("doc_url").toString();

			dba.insertNewEntry2(gsd, parent_id);
		}

		// send back the JSON file that was generated to the user
		//InputStream raw_json = getClass().getResourceAsStream(parsed_output_name);
		return result_json;
	}
}
