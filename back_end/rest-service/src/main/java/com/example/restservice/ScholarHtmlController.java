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

        String raw_file_name = parent_id + ".html";
        String parsed_output_name = parent_id + ".json";

        String tmp_dir = System.getProperty("user.dir") + "/tmp/";

        try {
            File myObj = new File(tmp_dir + raw_file_name);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(tmp_dir + raw_file_name);
            myWriter.write(user_html);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        PaperInfo user_paper = new PaperInfo();

        // TODO: deprecate this in the future in favor of local/frontend JavaScript parsing or native backend Java parsing
        // call the python program to parse the file (file name as command line argument?)
        // Runs the proper shell script depending on the OS it is running on
        System.out.println(tmp_dir);
	//Runtime.getRuntime().exec(tmp_dir + "parse.sh");
        //if (OSValidator.isUnix()) {
	//    System.out.println("executing " + tmp_dir + "parse.sh");
        //    Runtime.getRuntime().exec(tmp_dir + "parse.sh");
        //} else if (OSValidator.isWindows()) {
        //    Runtime.getRuntime().exec(tmp_dir + "parse.bat");
        //} else {
        //    System.out.println("System is neither Windows nor UNIX, Mac is not yet supported");
        //}

        // wait for output json to come back from Python
        File parsed_file = new File(tmp_dir + parsed_output_name);
        int backoff_timeout = 1;
        
	// background process will parse the raw info, wait for that to happen
	
	while(!parsed_file.exists()) {
            Thread.sleep(backoff_timeout);
            backoff_timeout = backoff_timeout + 50;
            System.out.println("Waiting for Python parsing of " + parsed_output_name + "...");
	    //Runtime.getRuntime().exec(tmp_dir + "parse.sh");
            if(backoff_timeout > 500) {
                System.out.println("Took too long...");
                break;
            }
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(tmp_dir + parsed_output_name));
        JSONObject result_json = (JSONObject) obj;

        try {
            parsed_file.delete();
        } catch (SecurityException e) {
            System.out.println("Permission to delete processed json file was denied");
        }
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
		JSONObject paper_json = (JSONObject) paper;
		//gsd = new GSData(((String) paper.get("title_short")), ((int)((Long) paper.get("year"))));
		 	gsd = new GSData(paper_json.get("title_short").toString(), Integer.parseInt(paper_json.get("year").toString()));

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
