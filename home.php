<!DOCTYPE HTML>
<html>
<head>
</head>
    <body>
    
        <?php
            // Report all errors
            error_reporting(E_ALL);

            if(isset($_POST['gs_search']) && isset($_POST['searchterms'])){
            //Crete new instance of Document Object Model Document to get HTML code from google scholar
            $doc = new DOMDocument('1.0', 'UTF-8');

            //To allow use of getElementByID
            $doc->validateOnParse = true;

            //Separate tags onto different lines
            $doc->formatOutput = true;

                $before_terms = "http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
                $terms_str = str_replace(" ", "+", $_POST['searchterms']);
                $after_terms = "&btnG=";
                $req_url = $before_terms . $terms_str . $after_terms;

            //Load HTML code using google scholar url. @ suppresses any warnings
            @ $doc->loadHTMLFile($req_url);
            
            /*
                Could also use cURL and do $dom->loadHTML($gs_html)
                curlin = curl_init(); //lol
                curl_setopt($curlin, CURLOPT_URL, 'http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=hello&btnG=');
                curl_setopt($curlin, CURLOPT_RETURNTRSNSFER, true);
                $gs_html = curl_exec($curl_in);
            */

            //Get rid of <!DOCTYPE> tag 
            $doc->removeChild($doc->firstChild);     

            //To remove extra, unwanted tags. Returns a DOMNode - failed
            //$body = $doc->getElementsByTagName('body');
            //$doc->setIdAttribute('gs_bdy_ccl', true);

            //Better to use gs_res_ccl but gs_bdy_ccl or gs_bdy might have info we need to filter by year if we want
            //$body = $doc->getElementById('gs_bdy_ccl');
            
            //Lists all the papers and their info
            $body = $doc->getElementById('gs_res_ccl');

            //echo($body->nodeValue);   //gets only text. Might be useful
            //Old code
            //$doc->replaceChild($body->item(0), $doc->firstChild); //Call to undefined method DOMElement::item() undefined

            //Replaces HTML tag element with gs_res_ccl and its children
            $doc->replaceChild($body, $doc->firstChild);

            //Maybe use this in the future
            //$doc->saveHTMLFile('http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=hello&btnG=');

            //I think this also only gets text
            //die(file_put_contents("newgsquery.txt", $body->nodeValue);
            
            //Write HTML text to file and exit
            //Pls don't die thank you very much
            //die(file_put_contents("newgsquery.txt", $doc->saveHTML()));
            file_put_contents('newgsquery.txt', $doc->saveHTML());
            echo($req_url);
            }
        ?>
        
        <h1>Hello welcome to the form</h1>

        <form action="" method="post">
            <input type="text" id="searchterms" name="searchterms" />
            <!-- only use if can get javascript method to work 
                <button onclick="getGSfromsearch()">Search</button>-->
                <input type="submit" name=gs_search value="Search" />
        </form>
        <p id="jsonstuff">Hello</p>
        <div id="storage" style="display: none;"></div>
    </body>
</html>
