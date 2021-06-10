// This is how outsiders interact with our API, the "Greeting" class
package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

// Defines "request mappings". In other words, when you see request "X" over the internet, what action "Y" should we do?
@RestController
public class GreetingController {
    private static String template = "Hello %s!";

    // Gives this interaction a unique ID? so for each interaction there is a unique incremented number
    private final AtomicLong counter = new AtomicLong();

    // Where we define the actual mappings
    // This mapping corresponds to if someone typed in "www.someurl.com/greeting"
    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name){
        // Calls our "Greeting" class with the name provided in the user's GET request. If there is no name defined in
        //      the request, then the name parameter defaults to "World" as defined in the line above this one.
        // String.format() call simply substitutes the provided name or the default value into the "template" String we
        //      defined in this class, so that our entire message string is passed to the "Greeting" class
        // IM STILL NOT SURE HOW/WHY THIS WORKS: apparently when you return a Class from this function, it automatically
        //      turns the class into a JSON that is sent back over the network
        // In order to test this:
        //      1. run the main() function in "RestServiceApplication"
        //      2. look for at the console output to determine what port it has started on, usually port 8080
        //      3. While it is running, go to your web browser and type in "localhost:8080/greeting" or whatever port it is
        //              you should now have a JSON file
        //      4. Now you can do it again with your chosen name by typing in "localhost:8080/greeting?name=POG-GROUP"
        System.out.println("bop");
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
