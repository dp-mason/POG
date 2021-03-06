package com.example.restservice;

import org.apache.commons.io.IOUtils;
//import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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
    public @ResponseBody JSONObject Recv_Paper_Html(@RequestBody String user_html) throws IOException, ParseException, InterruptedException {
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

        // TODO: deprecate this in the future in favor of local/frontend JavaScript parsing or backend Java parsing
        // call the python program to parse the file (file name as command line argument?)
        Runtime.getRuntime().exec("/home/david/Desktop/sda1/Senior/POG/back_end/rest-service/parse.sh");

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

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(parsed_output_name));
        JSONObject result_json = (JSONObject) obj;

        // Initialize the data structure that will be used to enter stuff into SQL database
        // Loop over all papers on this page
        //      [papers at index i].initFromJSON("output.json");

        // send back the JSON file that was generated to the user
        //InputStream raw_json = getClass().getResourceAsStream(parsed_output_name);
        return result_json;
    }
}
