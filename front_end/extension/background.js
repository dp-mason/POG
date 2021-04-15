// TODO: DOCUMENT THISBETTER AND CLEANUP

//function makejson(){
    //JSON.parse()
//    document.getElementById("jsonstuff").innerHTML = document.getElementById("searchterms").value;
//    return;
//}


//catches messages from graph js
chrome.runtime.onMessage.addListener(
    function(request, sender, sendResponse) {
      console.log(sender.tab ?
                  "from a content script:" + sender.tab.url :
                  "from the extension");
        //check if this is a url
        var cited_by_url= request.url;
        sendResponse({search: "goodbye"});
    }
  );


chrome.browserAction.onClicked.addListener(function(tab) {
    chrome.tabs.executeScript( {file: "/d3.js"});
    //chrome.tabs.executeScript( {file: "reviews.js" });
});

// catches messages sent from the scholar script, currently forwards data to our server for parsing
chrome.runtime.onMessage.addListener((msg) => {
    if(msg.type == "html"){
        raw_html = msg.html;
        console.log('recved html msg', raw_html);
        // send this raw html to your server, wait for response, then update the user with info

        // send raw html to our server for parsing
        // recv the result of the parsing and print it to the console
        //const url = "http://localhost:8080/papers/submitPaper";
        const url = "http://104.198.137.246:8080/papers/submitPaper";
        fetch(url, {
            method : "POST",
            body: raw_html,
            // -- or --
            // body : JSON.stringify({
            // user : document.getElementById('user').value,
            // ...
            // })
        })
        .then(function(response) {
            console.log(response.headers.get('Content-Type'));
        
            console.log(response.status);
            console.log(response.statusText);
            console.log(response.type);
            console.log(response.url);
            console.log(response.json());
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    
    // button script used to test the page update functionality with hard coded data
    //var updateButton = document.getElementById('test_update');
    //var new_url = "https://scholar.google.com/scholar?cites=7599416368689954089&as_sdt=5,43&sciodt=0,43&hl=en"

    //updateButton.addEventListener('click', function() {
    //    console.log("Updating...")
    //    updating = chrome.tabs.update({url: "https://developer.mozilla.org"})
    //    updating.then(onUpdated, onError)
    //    console.log("completed update")
    //})
    
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

        // TODO: RESORE THIS vvv BLOCK ONE DAY :(
        /*// var that stores whether a scholar tab is the currently active tab
        var scholarActive = false;
        // stores an inactive scholar tab if one exists
        var targetTabID = null;

        chrome.tabs.query({currentWindow: true, active: true}, function(tabsArray) {
            targetTabID = tabsArray[0].id;
        });

        // if the active tab is a a scholar tab, target it
        chrome.tabs.query({currentWindow: true, active: true}, function(tabsArray) {
            if(tabsArray[0].url.search("www.scholar.google.com/") > -1){
                scholarActive = true;
                console.log("active tab is a scholar tab");
            }
        });

        if (scholarActive = false){
            // else find a scholar tab that already exists that is inactive, if there is one.
            chrome.tabs.query({}, function(tabsArray) {
                // look for scholar tabs
                for (var i = 0; i < tabsArray.length; i++) {
                    if (tabsArray[i].url.search("www.scholar.google.com/") > -1){
                        console.log("found an inactive scholar tab");
                        targetTabID = tabsArray[i].id;
                        break;
                    }
                }
                // make this inactive tab the active one
                chrome.tabs.update(targetTabID, {active: true});
                scholarActive = true;
                console.log("made inactive scholar tab the active tab")
            });
        }

        //if(scholarActive){
        //    // update the active tab now that we know the active tab is a scholar tab
        //    chrome.tabs.update(targetTabID, { active: true, url: req_url }, function(tab){});
        //    console.log("updated the active tab")
        //}

        // PLACEHOLDER FOR SOMETHING MORE NUANCED, JUST UPDATES ACTIVE TAB NOW. 
        // TODO: IF ACTIVE ALREADY SCHOLAR, NO UPDATE, JUS)T HARVEST HTML
        chrome.tabs.update(targetTabID, { active: true, url: req_url }, function(tab){});
        console.log("updated the active tab")*/
        // TODO: RESORE THIS ^^^ BLOCK ONE DAY :(

        //else{
            // create a scholar tab if there is none
        //    chrome.tabs.create({'url':req_url})
        //    console.log("created new tab")
            // TODO: DO WE NEED TO SWITCH THE ACTIVE TAB TO THIS NEW TAB???
        //}
        
        // XXX: ????
        // if no scholar tab exists:
        // chrome.tabs.create({'url':req_url})
        // else, switch the current tab to the scholar tab and navigate to the new page

        // updates the current tab with the search query page
        //var activeTab = arrayOfTabs[0].id
        //chrome.tabs.update(activeTab, {
        //    active: true,
        //    url: req_url
        //}, function(tab){});

        // sends message to the scholar tab asking for it to send its document.
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
