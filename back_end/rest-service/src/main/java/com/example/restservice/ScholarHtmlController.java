package com.example.restservice;

import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

<<<<<<< HEAD

=======
>>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5
@CrossOrigin
@RestController
@RequestMapping("/papers")
public class ScholarHtmlController {

    // Gives this interaction a unique ID? so for each interaction there is a unique incremented number
    private final AtomicLong counter = new AtomicLong();

    // A POST request on "www.someurl.com/papers" will trigger this controller.
    //      the post request body should contain the entire raw html for the google scholar search result page
    //      or or "cited by" page for a paper
    // Eventually we want this to return a JSON with parsed paper info, but for now "void" is ok
    @CrossOrigin
//<<<<<<< HEAD
    @PostMapping(value = "/findPaper"/*, produces = "application/json"*/)
    //~ParseException
    public @ResponseBody ResponseEntity<GSData[]> Recv_Paper_Html(@RequestBody String paper_id) throws IOException, InterruptedException {
        //Connect to database
        DBAccesser dba = new DBAccesser();

        //Get children of paper that we received id of
        ArrayList<GSData> children = dba.getChildrenById(paper_id);

        //Put children into regular array
        GSData[] childArr = new GSData[children.size()];
        for(int i = 0; i < children.size(); i++){
            childArr[i] = children.get(i);
        }

        //Close connection to db
        dba.closeconnection();


        return new ResponseEntity<GSData[]>(childArr, HttpStatus.OK);
    }


//=======
//>>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5
    @CrossOrigin
    @PostMapping(value = "/submitPaper"/*, produces = "application/json"*/)
    //~ParseException
    public @ResponseBody boolean StorePaperInfo(@RequestBody String user_html) throws IOException, InterruptedException {
        // TODO: in the future the "parent" scholar id and page number will be included as the first few characters of the sent string
//<<<<<<< HEAD

//=======
        // TODO: if it is a "cited by" page

        String parentScholarId = user_html.substring(0,user_html.indexOf("---"));
//>>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5

        //Front end needs to append pogdb* to message to deal with duplicate messages
        //if(user_html.indexOf("---") == -1){

//<<<<<<< HEAD
            //System.out.println("bad");
            //wait until handling of correct message has probably completed
            //Thread.sleep(5000);
            //System.out.println("goodbye");

            //return false;
        //}
        //else{
          //  System.out.println("Good");
        //}

        //Remove pogdb* part from html
        //user_html = user_html.substring(user_html.indexOf("*")+1);

        //Python parsing logic
//=======
        String tmp_dir = System.getProperty("user.dir") + "/tmp/";

//>>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5
        String raw_file_name = parentScholarId + ".html";
        String parsed_output_name = parentScholarId + ".json";
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
                FileWriter myWriter = new FileWriter(raw_file_name);
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
            //Runtime.getRuntime().exec("C:\\Users\\benya\\OneDrive\\Desktop\\sen_sem\\gitsem\\POG\\back_end\\rest-service\\parse.bat");


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
            //Read JSON file

        try {
            FileWriter myWriter = new FileWriter(tmp_dir + raw_file_name);
            myWriter.write(user_html);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println(tmp_dir);






        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(tmp_dir + parsed_output_name));
        JSONObject result_json = (JSONObject) obj;
        JSONArray result_json_array = result_json.get("papers");

        try {
            parsed_file.delete();
        } catch (SecurityException e) {
            System.out.println("Permission to delete processed json file was denied");
        }
        // Initialize the data structure that will be used to enter stuff into SQL database
        // Loop over all papers on this page
        //      [papers at index i].initFromJSON("output.json");
<<<<<<< HEAD


        //Use if simple JSON will work
        //JSONParser parser = new JSONParser();
        //Object obj = parser.parse(new FileReader(parsed_output_name));
        //JSONObject result_json = (JSONObject) obj;

        //Connect to database
        DBAccesser dba = new DBAccesser();

        //TODO: Get length of papers field from json
        //For every paper, parse it out, put it into GSData object and store in database
        for (JSONObject paper : result_json_array){

//=======

        //>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5

//<<<<<<< HEAD

            //ArrayList<String> authorsArr = new ArrayList<String>();
            //ArrayList<String> authorUrlsArr = new ArrayList<String>();

//=======

//>>>>>>> c3bc2d8515ea7983b7349db8049ad88426b3b4e5

            //Use if simple.json works
            String title_short = paper.get("title_short");
            int year = paper.get("year");
            String doc_url = paper.get("doc_url");
            int cited_by_count = paper.get("cited_by_count");
            String summary_short = paper.get("summary_short");
            //summary_short = result_json.get("summary_short");
            JSONObject authors_and_links_obj = paper.get("authors_and_links");
            String authors_and_links = authors_and_links_obj.toString();

            //TODO: maybe change author and link storage in json (separate into two fields)
            ArrayList<String> authorsArr = new ArrayList<String>();
            ArrayList<String> authorUrlsArr = new ArrayList<String>();
            String[] tmpAuthorsArr = authors_and_links.split("\",");
            int counter = 0;
            for (String author : tmpAuthorsArr){
                if(author == "{" || author.length() == 1){  //why doesnt this work???? - lol I guess it's fixed -\(`_`)/-
                    break;
                }
                if(counter == 0){
                    System.out.println("new: "+author);
                }
                if(author.indexOf("\"}") > -1){
                    author.substring(0, author.length()-2);
                }
                System.out.println("now: " + author + " " + author.indexOf("\"") + " " + author.indexOf("\"",3));
                authorsArr.add(author.substring(author.indexOf("\"")+1,author.indexOf("\"",3)));
                int endindex = author.indexOf("\"", author.indexOf(": \"")+ 3);
                String tmpauthurl;
                if(endindex > -1) {
                    tmpauthurl = author.substring(author.indexOf(": \"") + 3, endindex);
                }
                else{
                    tmpauthurl = author.substring(author.indexOf(": \"") + 3);
                }
                authorUrlsArr.add(tmpauthurl);
                System.out.println("author: " + authorsArr.get(counter) + " url: " + authorUrlsArr.get(counter));
                counter++;
            }

            String scholar_id = paper.get("scholar_id");
            String source_url = paper.get("source_url");
            JSONArray referenced_by = paper.get("referenced_by");
            String cited_by_url = paper.get("cited_by_url");

            //Sanity checks
            //System.out.println("title: " + title_short + " from: " + title + " to: " + paper.indexOf("\"", title + 15));
            //System.out.println("year: " + year + " from: " + yr + " to: " + paper.indexOf("\"", yr + 5));
            //System.out.println("doc_url: " + doc_url + " from: " + doc + " to: " + paper.indexOf("\"", doc + 10));
            //System.out.println("cited_by_count: " + cited_by_count + " from: " + count + " to: " + paper.indexOf("\"", count));
            //System.out.println("cited_by_url: " + cited_by_url + " from: " + cited_url + " to: " + paper.indexOf("\"", cited_url));
            //System.out.println("summary_short: " + summary_short + " from: " + summary + " to: " + paper.indexOf("\"", summary));
            //System.out.println("authors_and_links: " + authors_and_links + " from: " + authors + " to: " + paper.indexOf("}", authors));
            //System.out.println("scholar_id: " + scholar_id + " from: " + scholarid + " to: " + paper.indexOf("\"", scholarid));
            //System.out.println("source_url: " + source_url + " from: " + sourceurl + " to: " + paper.indexOf("\"", sourceurl));
            //System.out.println("referenced_by: " + referenced_by + " from: " + ref + " to: " + paper.indexOf("]", ref));


            GSData gsd = new GSData(title_short, year);
            gsd.doc_url = doc_url;
            gsd.cited_by_count = cited_by_count;
            gsd.cited_by_url = cited_by_url;
            gsd.summary = summary_short;
            gsd.scholar_id = scholar_id;
            gsd.source_url = source_url;
            //gsd.queryUrl = title_short + ".com";
            gsd.authors = authorsArr;
            gsd.author_urls = authorUrlsArr;

            dba.insertNewEntry2(gsd, parentScholarId); //this was dba.insertNewEntry2(gsd, id);
        }
        return true;
    }
}
