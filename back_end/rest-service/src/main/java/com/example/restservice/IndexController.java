// This is how outsiders interact with our API, the "Greeting" class
package com.example.restservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {
	@RequestMapping("/")
	public String index() {
		return "index.html";
	}
}
