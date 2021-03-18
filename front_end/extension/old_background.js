document.addEventListener('DOMContentLoaded', function() {
    var searchButton = document.getElementById('search');
    searchButton.addEventListener('click', function() {
            /*Python version
        def scholar_url_maker(srch_str):
            terms_str = srch_str.replace(" ", "+")
            before_terms = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q="
            after_terms = "&btnG="
            req_url = before_terms + terms_str + after_terms
            return req_url
        */

        console.log("Beginning Request...\n");

        var srch_str = document.getElementById("searchterms").value;

        var before_terms = "http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
        var terms_str = srch_str.replace(" ", "+");
        var after_terms = "&btnG=";
        var req_url = before_terms + terms_str + after_terms;

        // Example POST method implementation:
        //function postData(url = req_url, html = {}) {
            console.log("YO\n");
            // Default options are marked with *
            fetch(req_url, {
                method: 'GET', // *GET, POST, PUT, DELETE, etc.
                mode: "no-cors", //, *cors, same-origin
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                credentials: 'same-origin', // include, *same-origin, omit
                headers: {
                    'Content-Type': 'text/html'
                    // 'Content-Type': 'application/x-www-form-urlencoded',
                },
                redirect: 'follow', // manual, *follow, error
                referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
                //body: String(html) // body data type must match "Content-Type" header
            }).then(data => console.log(data));
        // }

        // this triggers the schloar tab to send back all of the html data in a message it sends back to us
        return;    
    }, false);
}, false);