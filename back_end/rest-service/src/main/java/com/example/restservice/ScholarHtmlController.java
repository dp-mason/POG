package com.example.restservice;

import org.apache.commons.io.IOUtils;
//import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.*;
//~import org.json.simple.JSONObject;
//~import org.json.simple.parser.JSONParser;
//~import org.json.simple.parser.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

@CrossOrigin//(origins = "chrome-extension://koinmoamanbigcmkkamgnagpaecopkoc/")
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
    @CrossOrigin//(origins = "chrome-extension://koinmoamanbigcmkkamgnagpaecopkoc/")
    @PostMapping(value = "/submitPaper"/*, produces = "application/json"*/)
    //~ParseException
    public @ResponseBody ResponseEntity<GSData> Recv_Paper_Html(@RequestBody String user_html) throws IOException, InterruptedException {
    //~public @ResponseBody JSONObject Recv_Paper_Html(@RequestBody String user_html) throws IOException, InterruptedException {
    //~public @ResponseBody String Recv_Paper_Html(@RequestBody String user_html) throws IOException, InterruptedException {
        // TODO: in the future the "parent" scholar id and page number will be included as the first few characters of the sent string
        // TODO: if it is a "cited by" page.
        String id = "raw_html"; //XXX: badbad fix this soon

        String raw_file_name = id + ".html";
        String parsed_output_name = id + ".json";

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

        int parentId = 175;
        String parentTitle = "What Every Programmer Needs to Know about Security (Advances in Information Security)";
        //int parentYear = 2006;
        DBAccesser dba = new DBAccesser();
        if(dba.getPaperId(parentTitle) == -1){
            return null;
        }
        Integer[] cids = dba.getCitedIds(parentId);
        GSData parent = new GSData();
        dba.getPaperRow(parentId, parent);

        GSData[] gsdArr = new GSData[cids.length];
        for (int i = 0; i < cids.length; i++) {
            GSData gsd = new GSData();

            dba.getPaperRow(cids[i], gsd);
            dba.getCitedCount(cids[i], gsd);
            dba.getAuthors(cids[i], gsd);
            gsdArr[i] = gsd;
        }
        dba.closeconnection();

        parent.setChildren(gsdArr);
        //~String result_json_text = parent.toJSON(gsdArr);
        JSONObject result_json_gsd = parent.toJSON(gsdArr); //~
        System.out.println("b4 result");

        System.out.println("Result: " + result_json_gsd.toString());
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
        return new ResponseEntity<GSData>(parent, HttpStatus.OK);
    }
}
