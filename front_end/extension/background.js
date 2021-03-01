//function makejson(){
    //JSON.parse()
//    document.getElementById("jsonstuff").innerHTML = document.getElementById("searchterms").value;
//    return;
//}

chrome.runtime.onMessage.addListener((msg) => {
    if(msg.type == "html"){
        raw_html = msg.html;
        console.log('recved html msg', raw_html);
        // send this raw html to your server, wait for response, then update the user with info
    }

    // send html to our server for storage
    const url = "http://localhost:8080/papers/submitPaper";
    fetch(url, {
        method : "POST",
        body: raw_html,
        // -- or --
        // body : JSON.stringify({
        // user : document.getElementById('user').value,
        // ...
        // })
    }).then(
        response => response.text() //.json(), etc.
        // same as function(response) {return response.text();}
    ).then(
        html => console.log(html)
    );
});

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

        console.log("Hellow World\n");

        var srch_str = document.getElementById("searchterms").value;

        var before_terms = "http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
        var terms_str = srch_str.replace(" ", "+");
        var after_terms = "&btnG=";
        var req_url = before_terms + terms_str + after_terms;

        //var gs_request = new XMLHttpRequest();                
        //gs_request.open('GET', req_url, false);

        //chrome.tabs.update({
        //    url: req_url
        //});
        
        // XXX: CORS makes this impossible, copy the sending stuff in order to send html post request to our server
        //try {
        //    gs_request.send();
        //    if (gs_request.status != 200) {
        //        alert(`Error ${gs_request.status}: ${gs_request.statusText}`);
        //    } else {
        //        alert(gs_request.response);
        //    }
        //} catch(err) { // instead of onerror
        //    alert("Request failed");
        //}
        
        //gs_request.onload = function(){
            //console.log(gs_request.responseText());
        //}
        
        //gs_request.send();

        //sends message to the scholar tab asking for it to send its document.
        var raw_html;
        chrome.tabs.query({active: true}, function(tabs) {
            chrome.tabs.sendMessage(tabs[0].id, {msg:"html_request"}, function(response) {
                    console.log(response.answer);
                    return;
                }
            );
        });
        // this triggers the schloar tab to send back all of the html data in a message it sends back to us
        return;    
    }, false);
}, false);