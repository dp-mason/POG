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
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

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
    @PostMapping(value = "/findPaper"/*, produces = "application/json"*/)
    //~ParseException
    public @ResponseBody ResponseEntity<GSData[]> Recv_Paper_Html(@RequestBody String paper_id) throws IOException, InterruptedException {
        //parameter will be "{"titleorid" : "foo", "isTitle": "true", "year": 1990}"
        //get istitle index
        //int Istitle=indexOf("\"isTitle\":") + 10;
        //get titleorid field
        //int idref = indexOf("\"titleorid\":")+12;
        //String Titleorid = arg.substring(idref,indexOf("\"",idref));



        //int parentId = 175;
        //String parentTitle = "What Every Programmer Needs to Know about Security (Advances in Information Security)";
        //int parentYear = 2006;
        DBAccesser dba = new DBAccesser();

        ArrayList<GSData> children = dba.getChildrenById(paper_id);
        GSData[] childArr = new GSData[children.size()];
        for(int i = 0; i < children.size(); i++){
            childArr[i] = children.get(i);
        }

        //if (arg.substring(Istitle,arg.indexOf("\"",Istitle)) == "true"){
        //get year
        //int yrindex = indexOf("\"year\":") + 8;
        //parentId = dba.getPaperId(Titleorid,arg.substring(yrindex, arg.indexOf("\"",yrindex)));
        //if(parentId == -1){return null;}
        //}
        //else{
        //parentId = Integer.parseInt(Titleorid);
        //}
        //then
        //if(dba.getPaperRow(parentId, parent) == 0){
        //return null;
        //}

        //if(dba.getPaperId(paper_id) == -1){
          //  return null;
        //}
        //Integer[] cids = dba.getCitedIds(parentId);
        //if (cids.length == 0){return null;}
        //dba.getPaperRow(parentId, parent);
/*
        GSData[] gsdArr = new GSData[cids.length];
        for (int i = 0; i < cids.length; i++) {
            GSData gsd = new GSData();

            dba.getPaperRow(cids[i], gsd);
            dba.getCitedCount(cids[i], gsd);
            dba.getAuthors(cids[i], gsd);
            gsdArr[i] = gsd;
        }*/
        dba.closeconnection();

        //parent.setChildren(gsdArr);
        //~String result_json_text = parent.toJSON(gsdArr);

        //JSONObject result_json_gsd = parent.toJSON(gsdArr); //~
        System.out.println("b4 result");

        //System.out.println("Result: " + result_json_gsd.toString());
        //System.exit(0);
        /*
        try {
            JSONArray tmpj = (JSONArray) result_json_gsd.get("papers");
            for (int i= 0; i < tmpj.length(); i++){
                //System.out.println(tmpj.get(i).toString());
                Iterator<String> keys =((JSONObject)tmpj.get(i)).keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    System.out.println(key);
                }
            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }
        */

        //~JSONParser parser = new JSONParser();
        //~Object obj = parser.parse(new FileReader(parsed_output_name));
        //Object obj = parser.parse(result_json_text);

        //~JSONObject result_json = (JSONObject) obj;

        //~JSONParser parser = new JSONParser();
        //~Object obj = parser.parse(new FileReader(parsed_output_name));

        //parsed_file.delete(); //uncomment

        // Initialize the data structure that will be used to enter stuff into SQL database
        // Loop over all papers on this page
        //      [papers at index i].initFromJSON("output.json");

        // send back the JSON file that was generated to the user
        //InputStream raw_json = getClass().getResourceAsStream(parsed_output_name);
        //~return result_json;
        //~return result_json_gsd.toString();
        //!return result_json_gsd;
        JSONObject js = new JSONObject();
        try {
            js.put("id", 1);
        } catch (JSONException e){
            e.printStackTrace();
        }
        //return js; //new ResponseEntity<>("Hello", HttpStatus.OK);
        return new ResponseEntity<GSData[]>(childArr, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "/submitPaper"/*, produces = "application/json"*/)
    //~ParseException
    public @ResponseBody boolean StorePaperInfo(@RequestBody String user_html) throws IOException, InterruptedException {
        //try {
        //~public @ResponseBody JSONObject Recv_Paper_Html(@RequestBody String user_html) throws IOException, InterruptedException {
        //~public @ResponseBody String Recv_Paper_Html(@RequestBody String user_html) throws IOException, InterruptedException {
        // TODO: in the future the "parent" scholar id and page number will be included as the first few characters of the sent string
        // TODO: if it is a "cited by" page.
        String htmlid = "raw_html4"; //XXX: badbad fix this soon


        String raw_file_name = htmlid + ".html";
        String parsed_output_name = htmlid + ".json";

        System.out.println("yooooouuu1: " + user_html.substring(0,150));
        //String pidtmp = user_html.substring(0, user_html.indexOf("*"));
        if(user_html.indexOf("pogdb*") == -1){
            //System.exit(-1);
            System.out.println("bad");
            Thread.sleep(5000);
            System.out.println("goodbye");
            //System.exit(-1);
            return false;
        }
        else{
            System.out.println("Good");
        }
        //System.out.println("pidtmp: {" + pidtmp+"}");
        //int pid = Integer.parseInt(pidtmp);
        System.out.println("yooooouuu: " + user_html.substring(0,150));
        user_html = user_html.substring(user_html.indexOf("*")+1);

        try {
            File myObj = new File(raw_file_name);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            // System.out.println(user_html);
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
            Runtime.getRuntime().exec("C:\\Users\\benya\\OneDrive\\Desktop\\sen_sem\\gitsem\\POG\\back_end\\rest-service\\parse.bat");


            // wait for output json to come back from Python
            File parsed_file = new File(parsed_output_name);
            int backoff_timeout = 1;
            while(!parsed_file.exists()) {
                Thread.sleep(backoff_timeout);
                backoff_timeout = backoff_timeout + 50;
                System.out.println("Waiting for Python parsing of " + parsed_output_name + "...");
                if(backoff_timeout > 1000) {
                    System.out.println("Took too long...");
                    break;
                }
            }
        String info = "";
        try {
            File rawhtml = new File(parsed_output_name);
            Scanner scan = new Scanner(rawhtml);

            while (scan.hasNextLine()) {
                info += scan.nextLine().trim();

            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File reading error");
            e.printStackTrace();
        }

        //JSONParser parser = new JSONParser();
        //Object obj = parser.parse(new FileReader(parsed_output_name));
        //JSONObject result_json = (JSONObject) obj;

        // System.out.println(info);

        String id = info.substring(info.indexOf("\"id\":")+7, info.indexOf("\",",info.indexOf("\"id\":")+7));
        String papers = info.substring(info.indexOf("papers\":"), info.lastIndexOf("]"));
        String regex = "},\\{";
        String[] papersArr = papers.split(regex);

        System.out.println("id: "+id);
        //System.exit(-1);

        DBAccesser dba = new DBAccesser();

        //if(doesnt have parent id in table){
        // add parent to table
        // }

        //try{
        for (String paper : papersArr){
            paper += "}";
            System.out.println("paper: "+paper);

            int doc = paper.indexOf("\"doc_url\":");
            int count = paper.indexOf("\"cited_by_count\":");
            int cited_url = paper.indexOf("\"cited_by_url\":");
            int yr = paper.indexOf("\"year\":");
            int summary = paper.indexOf("\"summary_short\":");
            int title = paper.indexOf("\"title_short\":");
            int authors = paper.indexOf("\"authors_and_links\":");
            int scholarid = paper.indexOf("\"scholar_id\":");
            int sourceurl = paper.indexOf("\"source_url\":");
            int ref = paper.indexOf("\"referenced_by\":");
            //Pattern p = Pattern.compile("\"}|},|:[0-9]+,|,\"");

            String doc_url = paper.substring(doc+12,paper.indexOf("\"", doc + 12));
            String cited_by_count;
            if(paper.indexOf(",", count) > -1) {
                cited_by_count = paper.substring(count+18, paper.indexOf(",", count));
            }
            else {
                cited_by_count = paper.substring(count+18, paper.indexOf("}", count));
            }
            String cited_by_url = paper.substring(cited_url+17, paper.indexOf("\"", cited_url+17));

            String year;
            if(paper.indexOf(",", yr) > -1){
                year = paper.substring(yr + 8, paper.indexOf(",", yr + 5));
            }
            else{
                year = paper.substring(yr + 8, paper.indexOf("}", yr + 5));
            }
            String referenced_by = paper.substring(ref+18, paper.indexOf("]", ref+18));;
            String summary_short = paper.substring(summary+18, paper.indexOf("\"", summary+18));
            String title_short = paper.substring(title + 16, paper.indexOf("\"", title + 16));
            String authors_and_links = paper.substring(authors + 21, paper.indexOf("}", authors + 17));
            String scholar_id = paper.substring(scholarid + 15, paper.indexOf("\"", scholarid + 15));
            String source_url = paper.substring(sourceurl + 15, paper.indexOf("\"", sourceurl+15));

            ArrayList<String> authorsArr = new ArrayList<String>();
            ArrayList<String> authorUrlsArr = new ArrayList<String>();
            String[] tmpAuthorsArr = authors_and_links.split("\",");
            int counter = 0;
            for (String author : tmpAuthorsArr){
                if(author == "{" || author.length() == 1){  //why doesnt this work????
                    break;
                }
                if(counter == 0){
                    // author = author.substring(33);
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
                //System.exit(0);
            }

            System.out.println(paper.charAt(sourceurl+14));

            //title_short = result_json.get("title_short");
            //year = result_json.get("year");
            //doc_url = result_json.get("doc_url");
            //cited_by_count = result_json.get("cited_by_count");
            //summary_short = result_json.get("summary_short");
            //summary_short = result_json.get("summary_short");
            //authors_and_links = result_json.get("authors_and_links");
            //scholar_id = result_json.get("scholar_id");
            //source_url = result_json.get("source_url");
            //referenced_by = result_json.get("referenced_by");


            System.out.println("title: " + title_short + " from: " + title + " to: " + paper.indexOf("\"", title + 15));
            System.out.println("year: " + year + " from: " + yr + " to: " + paper.indexOf("\"", yr + 5));
            System.out.println("doc_url: " + doc_url + " from: " + doc + " to: " + paper.indexOf("\"", doc + 10));
            System.out.println("cited_by_count: " + cited_by_count + " from: " + count + " to: " + paper.indexOf("\"", count));
            System.out.println("cited_by_url: " + cited_by_url + " from: " + cited_url + " to: " + paper.indexOf("\"", cited_url));
            System.out.println("summary_short: " + summary_short + " from: " + summary + " to: " + paper.indexOf("\"", summary));
            System.out.println("authors_and_links: " + authors_and_links + " from: " + authors + " to: " + paper.indexOf("}", authors));
            System.out.println("scholar_id: " + scholar_id + " from: " + scholarid + " to: " + paper.indexOf("\"", scholarid));
            System.out.println("source_url: " + source_url + " from: " + sourceurl + " to: " + paper.indexOf("\"", sourceurl));
            System.out.println("referenced_by: " + referenced_by + " from: " + ref + " to: " + paper.indexOf("]", ref));


            System.out.println(" ");
            GSData gsd = new GSData(title_short, Integer.parseInt(year));
            gsd.doc_url = doc_url;
            gsd.cited_by_count = Integer.parseInt(cited_by_count);
            gsd.cited_by_url = cited_by_url;
            gsd.summary = summary_short;
            gsd.scholar_id = scholar_id;
            gsd.source_url = source_url;
            gsd.queryUrl = title + ".com";
            gsd.authors = authorsArr;
            gsd.author_urls = authorUrlsArr;
            //DBAccesser dba = new DBAccesser();
            //dba.insertNewEntry(gsd, 173);
            dba.insertNewEntry2(gsd, id);

        }
        //}
        //catch (JSONException e){
          //  e.printStackTrace();
        //}
        return true;
    }
}
