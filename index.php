<?php
            $doc = new DOMDocument('1.0', 'UTF-8');
// load the string into the DOM (this is your page's HTML), see below for more info
$doc->loadHTML('http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=hello&btnG=');

// since we are working with HTML fragments here, remove <!DOCTYPE 
$doc->removeChild($doc->firstChild);            

// remove <html></html> and any junk
$body = $doc->getElementsByTagName('body'); 
$doc->replaceChild($body->item(0), $doc->firstChild);

// now, you can get any portion of the html (target a div, for example) using familiar DOM methods

// echo the HTML (or desired portion thereof)
die($doc->saveHTML());
echo($doc->$HTTP_RAW_POST_DATA)
        ?>


<html>
    <head>
        <script>
            function makejson(){
                //JSON.parse()
                document.getElementById("jsonstuff").innerHTML = document.getElementById("searchterms").value;
                return;
            }
            //var responseHTML = document.createElement("body");
            function getGSfromsearch(){
                /*Python version
                def scholar_url_maker(srch_str):
                    terms_str = srch_str.replace(" ", "+")
                    before_terms = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q="
                    after_terms = "&btnG="
                    req_url = before_terms + terms_str + after_terms
                    return req_url
                */
                var srch_str = document.getElementById("searchterms").value;

                var before_terms = "http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
                var terms_str = srch_str.replace(" ", "+");
                var after_terms = "&btnG=";
                var req_url = before_terms + terms_str + after_terms;

                /*var gs_request = new XMLHttpRequest();
                gs_request.open('GET', req_url);

                gs_request.onload = function(){
                    console.log(gs_request.responseText());

                }
                gs_request.send();*/

                
                /*fetch(req_url, {
                    mode: 'no-cors',
                    credentials: 'include'

                }).then(res => res.text()).then(data => {
                document.getElementById("jsonstuff").innerHTML = data;
                console.log(data)
                }).then(() => {
                    console.log("ok...")
                // after fetch write js code here  
                })*/


                //const response = await 
                /*
                fetch(req_url, {
                    mode: 'no-cors',
                    method: 'get'
                })
                .then(response => response.type)
                .then(data => console.log(data));

                //response.json();

                const xhr = new XMLHttpRequest();
                */
/*
                var myHeaders = new Headers();
                myHeaders.append('Content-Type', 'text/html');
                fetch(req_url, {
                    mode: 'cors',
                    method: 'get'
                })
                .then(function(response) {
                return response.text();
                })
                .then(function(text) {
                console.log('Request successful', text);
                })
                .catch(function(error) {
                    console.log('Request failed', error)
                });
*/



                /*

                // create a `GET` request
                xhr.open('GET', req_url);
                xhr.responseType = 'json';
                xhr.onerror = () => {
                    console.error('Request failed.');
                }
                
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.setRequestHeader("Accept","application/json");
                // send request
                xhr.send();

                xhr.onload = () => {
                    // process response
                    if (xhr.status == 200) {
                        // parse JSON data
                        console.log(JSON.parse(xhr.response));
                    } else {
                        console.error('Error!');
                    }
                }*/

                //callOtherDomain(xhr, req_url)

                /*var responseHTML = document.getElementById("storage"); 
                var target = document.getElementById("gs_bdy_ccl"); 
                loadHTML(req_url, callback, responseHTML, target);
*/
                //document.getElementById("storage").innerHTML = "<iframe src= " + req_url + "width='100%' height='560' frameboarder='0'></iframe>";

                $.ajax({
                    type: "POST",
                    url: req_url,
                    //data: {url: "index.php"}
                    }).done(function( html ) {
                    // do something with your HTML!
                });

                return;
            }


            /**
	responseHTML
	(c) 2007-2008 xul.fr		
	Licence Mozilla 1.1
	


/**
	Searches for body, extracts and return the content
	New version contributed by users



function getBody(content) 
{
   test = content.toLowerCase();    // to eliminate case sensitivity
   var x = test.indexOf("<body");
   if(x == -1) return "";

   x = test.indexOf(">", x);
   if(x == -1) return "";

   var y = test.lastIndexOf("</body>");
   if(y == -1) y = test.lastIndexOf("</html>");
   if(y == -1) y = content.length;    // If no HTML then just grab everything till end

   return content.slice(x + 1, y);   
} 

/**
	Loads a HTML page
	Put the content of the body tag into the current page.
	Arguments:
		url of the other HTML page to load
		id of the tag that has to hold the content
		
function callback(responseHTML, target){
    var x = responseHTML.getElementsByTagName("div").namedItem("two");
target.innerHTML = x.innerHTML;
var y = responseHTML.getElementsByTagName("form").namedItem("ajax");
target.innerHTML += y.dyn.value;

}


function loadHTML(url, fun, storage, param)
{
	//var xhr = createXHR();
    var xhr = new XMLHttpRequest()
	xhr.onreadystatechange=function()
	{ 
		if(xhr.readyState == 4)
		{
			//if(xhr.status == 200)
			{
				storage.innerHTML = getBody(xhr.responseText);
				fun(storage, param);
			}
		} 
	}; 

	xhr.open("GET", url , true);
	xhr.send(null); 

} 

	/**
		Callback
		Assign directly a tag
			


	function processHTML(temp, target)
	{
		target.innerHTML = temp.innerHTML;
	}

	function loadWholePage(url)
	{
		var y = document.getElementById("storage");
		var x = document.getElementById("displayed");
		loadHTML(url, processHTML, x, y);
	}	


	/**
		Create responseHTML
		for acces by DOM's methods
		
	
	function processByDOM(responseHTML, target)
	{
		target.innerHTML = "Extracted by id:<br />";

		// does not work with Chrome/Safari
		//var message = responseHTML.getElementsByTagName("div").namedItem("two").innerHTML;
		var message = responseHTML.getElementsByTagName("div").item(1).innerHTML;
		
		target.innerHTML += message;

		target.innerHTML += "<br />Extracted by name:<br />";
		
		message = responseHTML.getElementsByTagName("form").item(0);
		target.innerHTML += message.dyn.value;
	}
	
	function accessByDOM(url)
	{
		//var responseHTML = document.createElement("body");	// Bad for opera
		var responseHTML = document.getElementById("storage");
		var y = document.getElementById("displayed");
		loadHTML(url, processByDOM, responseHTML, y);
	}	

/*            
            function callOtherDomain(request, url){
                if(request){
                    request.open('GET', url, true);
                    request.withCredentials = "true";
                    request.onreadystatechange = () => {
                    // process response
                    if (request.status == 200) {
                        // parse JSON data
                        console.log(JSON.parse(request.response));
                    } else {
                        console.error('Error!');
                    }};
                    request.send();
                }
            }
*/
            /*
            var responseHTML = document.createElement("body");

            function getBody(content){
                var start_pos = content.indexOf("<body");
                start_pos = content.indexOf(">", start_pos);
                var end_pos = content.lastIndexOf("</body>");
                return content.slice(start_pos + 1, end_pos);
            }

            function getContent(content, target){
                target.innerHTML = getBody(content);
            }
            */
                

        </script>
        
    </head>

    <body>
<h1>Hello welcome to the form</h1>
<!--<form>-->
    <input type="text" id="searchterms" />
    <button onclick="getGSfromsearch()">Magic button</button>
<!--</form>-->
<p id="jsonstuff"></p>
<div id="storage" style="display: none;"></div>
    </body>

</html>
