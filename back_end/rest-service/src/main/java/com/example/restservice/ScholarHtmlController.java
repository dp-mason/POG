package com.example.restservice;

import org.springframework.web.bind.annotation.*;
import java.util.concurrent.atomic.AtomicLong;

import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;   // Import the FileWriter class

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
    // Eventually we want this to return a JSON with parsed paper info, but for now "void" is ok
    @CrossOrigin//(origins = "chrome-extension://koinmoamanbigcmkkamgnagpaecopkoc/")
    @PostMapping("/submitPaper")
    public void Recv_Html(@RequestBody String user_html){
        // prints out the data that was unpacked from the post request so we can test this function
        try {
            File myObj = new File("raw_html.txt");
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
            FileWriter myWriter = new FileWriter("raw_html.txt");
            myWriter.write(user_html);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
