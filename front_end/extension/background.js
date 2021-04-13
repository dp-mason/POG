

/***** FKA new_graph.js  ***************/

 var width = window.innerWidth,
     height = window.innerHeight, 
     root;
//window.resizeTo(800, 700);

var counter= 0;
var notes = d3.select('#notes')
    .style({
        'width': width * 1/6 + 'px',
        'height': height + 'px'
    });
    
var force= d3.layout.force()
    .linkDistance(150)
    .charge(-350)
    .gravity(.05)
    .size([width, height])
    .on("tick", tick);



var svg = d3.select("div").append("svg")
    .attr("viewBox", "0 0 " + width + " " + height)
    .attr("width", "100%")
    .attr("height", height);


var link = svg.selectAll(".link"),
    node = svg.selectAll(".node");
    
// d3.json("graph.json", function(error, json) {
//   if (error) throw error;
//   root = json;
//   update();
// })

//try it with map
var data= {
    "name": "Network science",
    "cited_by_url": "https://scholar.google.com/scholar?cites=14955132727279659603&as_sdt=5,43&sciodt=0,43&hl=en&oe=ASCII",
    "children": [
    {
        "name": "A new technique for building maps",
        "children": [
            {

                "summary_short": "It is a sobering fact that some 90% of papers that have been published in academic journals are never cited. Indeed, as many as 50% of papers are never read by anyone other than their authors, referees and journal editors. We know this thanks to citation analysis, a branch\u00a0\u2026",
                "name": "The rise of citation analysis",
                "cited_by_url": "https://scholar.google.com",
                // "children": [],
                "year": 2007
            },
            {
                
                "summary_short": "The past few years have seen intensive research efforts carried out in some apparently unrelated areas of dynamic systems\u2013delay-tolerant networks, opportunistic-mobility networks and social networks\u2013obtaining closely related insights. Indeed, the concepts\u00a0\u2026",
                "name": "Time-varying graphs and dynamic networks",
                "cited_by_url": "https://scholar.google.com",
                // "children": [],
                "year": 2012
            }
        ],
        "summary_short": "Our objective is the generation of schematic visualizations as interfaces for scientific domain analysis. We propose a new technique that uses thematic classification (classes and categories) as entities of cocitation and units of measure, and demonstrate the viability of\u00a0\u2026",
        "year": 2004
    },
    {
        "children": [
            {
                "summary_short": "Abstract The Institute for Scientific Information's (ISI, now Thomson Scientific, Philadelphia, PA) citation databases have been used for decades as a starting point and often as the only tools for locating citations and/or conducting citation analyses. The ISI databases (or Web of\u00a0\u2026",
                "name": "Impact of data sources on citation counts",
                "cited_by_url": "https://scholar.google.com",
                //"children": [],
                "year": 2007
            },
            {
                "summary_short": "Although a large body of knowledge about both brain structure and function has been gathered over the last decades, we still have a poor understanding of their exact relationship. Graph theory provides a method to study the relation between network structure\u00a0\u2026",
                "name": "Functional neural network analysis",
                "cited_by_url": "https://scholar.google.com",
                //"children": [],
                "year": 2009
            },
            {
                "summary_short": "Webometrics is concerned with measuring aspects of the web: web sites, web pages, parts of web pages, words in web pages, hyperlinks, web search engine results. The importance of the web itself as a communication medium and for hosting an increasingly wide array of\u00a0\u2026",
                "name": "Introduction to webometrics",
                "cited_by_url": "https://scholar.google.com",
                // "children": [],
                "year": 2009
            }
        ],
        "name": "Graph signal processing",
        "summary_short": "blah blah blah",
        "year": 2018
    },
    {
        "summary_short": "The development of new technologies for mapping structural and functional brain connectivity has led to the creation of comprehensive network maps of neuronal circuits and systems. The architecture of these brain networks can be examined and analyzed with a\u00a0\u2026",
        "name": "Modular brain networks",
        "cited_by_url": "https://scholar.google.com",
        //"children": [],
        "year": 2016
    },
    {
        "summary_short": "Tourism destinations have a necessity to innovate in order to remain competitive in an increasingly global environment. A pre-requisite for innovation is the understanding of how destinations source, share and use knowledge. This conceptual paper examines the nature\u00a0\u2026",
        "name": "Knowledge transfer in a tourism destination",
        "cited_by_url": "https://scholar.google.com",
        //"children": [],
        "year": 2010
    },
    {
        "summary_short": "Recent developments in the quantitative analysis of complex networks, based largely on graph theory, have been rapidly translated to studies of brain network organization. The brain's structural and functional systems have features of complex networks\u2014such as small\u00a0\u2026",
        "name": "Complex brain networks",
        "cited_by_url": "https://scholar.google.com",
        //"children": [],
        "year": 2009
    }
    
    ],
    "summary_short": "This chapter reviews the highly interdisciplinary field of network science, which is concerned with the study of networks, be they biological, technological, or scholarly in character. It contrasts, compares, and integrates techniques and algorithms developed in disciplines as\u00a0\u2026",
};

d3.select("div").data(data, function(data){
    update()
});


function update() {
    var nodes = flatten(data),
    links = d3.layout.tree().links(nodes);


    force
    .nodes(nodes)
    .links(links)
    .start();

// Update links.
    link = link.data(links, function(d) { return d.target.name; });

    link.exit().remove();

    link.enter().insert("line", ".node")
    .attr("class", "link");

// Update nodes.
    node = node.data(nodes, function(d) { return d.name; });

    node.exit().remove();

    var nodeEnter = node.enter().append("g")
        .attr("class", "node")
        .on('click', click)
        .call(force.drag);
    
    nodeEnter.append("circle")
        .attr("r", 20);

    nodeEnter.append("text")
        .attr("dy", ".35em")
        .text(function(d) { return d.name; });

    node.select("circle")
        .style("fill", color);
}   

function tick() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
}


function color(d) {
    return d.children ? "#c6dbef" // collapsed package
        : d._children ? "#3182bd" // expanded package
        : "#fd8d3c";
    
}

// Function to handle clicks on node elements


// function addnew(d, newchild){
//   counter++;
//   if(!d.children){
//     d.children= [];
//   }
//   d.children.push(newchild);
// }


function click(d){
    if (d3.event.defaultPrevented) return;

    if(document.getElementById("expand").checked  ){


    if (d.children) {
        d._children=d.children;
        d.children= null;
    }else {
        d.children = d._children;
        d._children= null;
    }

    if(!d.children && !d._children){
        var children= getChildren(d.cited_by_url);    //new try
        d.children= children;

    //   chrome.runtime.sendMessage({url: d.cited_by_url, type: "url"}, function(response){
    //       children= response.children;
    //       console.log(response.children);
    //       d.children=children;
    //       update();
    //     });
    
    }
    update();

    }

    else if(document.getElementById("info").checked  ){

    if (!d.selected) {

        d3.select(this).select("text").transition()
            .duration(500)
            .style("font", "15px sans-serif");

        d3.select(this).select("circle").transition()
            .duration(500)
            .attr("r", 30)
            .style("fill", "#7ef084");


        notes.selectAll('*').remove();

        // Fill in the notes section with informationm
        // from the node. 

        notes.style({'opacity': 0});

        // Now add the notes content.

        notes.append('h1').text(d.name);
        notes.append('p').text(d.summary_short);
        
        // With the content in place, transition
        // the opacity to make it visible.

        notes.transition().style({'opacity': 1});

        d.selected= true;

    } else {
        // Since we're de-selecting the current
        // node, transition the notes section
        // and then remove it.

        d3.select(this).select("text").transition()
            .duration(500)
            .style("font", "10px sans-serif");

        d3.select(this).select("circle").transition()
            .duration(500)
            .attr("r", 20)
            .style("fill", color(d));

        notes.transition()
            .style({'opacity': 0})
            .each('end', function(){
                notes.selectAll('*').remove();
            });

        d.selected = false;
        }
        
    }
};

// Returns a list of all nodes under the root.
function flatten(root) {
    var nodes = [], i = 0;
    
    function recurse(node) {
    if (node.children)node.children.forEach(recurse);
    if (!node.id) node.id = ++i;
    nodes.push(node);
    }

    recurse(root);
    return nodes;
}
    












/****************BACKGROUND  *******************/


// catches messages sent from the scholar script, currently forwards data to our server for parsing
var counter=0;
var citedby;
function getChildren(url){
    if (!(String(url).startsWith("https://scholar.google.com"))){
            //error
    }

    //DUMMY STUFF TO ADD CHILDREN
    // console.log(String(url));
    // var citedby= [
    //     {
    //         "summary_short": "newchild summary",
    //         "name": "child "+String(counter),
    //         "cited_by_url": "https://scholar.google.com",
    //         //"children": [],
    //         "year": 2021
    //     },
    //     {
    //         "summary_short": "newchild 2 summary",
    //         "name": "child " +String (counter+1),
    //         "cited_by_url": "https://scholar.google.com",
    //         //"children": [],
    //         "year": 2021
    //     }]
    // counter= counter+2;
    // return citedby;


    
    //FIRST check database if we have it
    //url is IP adress of host database/papers/findPaper
   // var citedby;
    // var inDB= false;
    // const ipurl =  "http://localhost:8080/papers/submitPaper";
    // fetch(ipurl, {
    //     method : "POST",
    //     body: citedby
    // }).then(
    //         function(response){
    //             if (response != null){
    //                 inDB= true;
    //             }
    //             console.log(response);
    //         }
    // )

    
    //if yes, check children, return children
    // if(inDB){

    // }
    //if not, fetch children from scholar 
    // else{        
        // sends message to the scholar tab asking for it to send its document.
        chrome.tabs.query({active: true}, function(tabs) {
            chrome.tabs.sendMessage(tabs[0].id, {msg:"html_request"}, function(response) {
                    console.log(response.answer);
                    return response.answer;
                }
            );
        });
        //do formatting? what does return html get
       // citedby= response.answer;    
        //return citedby;
   // }
    

    

//CURRENTLY INSIDE THE FUNCTION, NEED TO GET THE PARSED DATA OUT AND IN CITEDBY
chrome.runtime.onMessage.addListener((msg, citedby) => {
    if(msg.type == "html"){
        raw_html = msg.html;
        console.log('received html msg');
        // send this raw html to your server, wait for response, then update the user with info
        // send raw html to our server for parsing
        // recv the result of the parsing and print it to the console
        //const url = "http://localhost:8080/papers/submitPaper"; // OLD 1LOCAL SERVER URL
        const url =  "http://104.198.137.246:8080/papers/submitPaper" // new google cloud server url
        fetch(url, {
            method : "POST",
            body: raw_html,
            // -- or --
            // body : JSON.stringify({
            // user : document.getElementById('user').value,
            // ...
            // })
        })
        .then(function(response, citedby) {
            //parsed json 
            response.json().then(data=> {
                console.log(data.papers);
                citedby= data.papers;
                return citedby;
            })
            return citedby;
        });
    }
});
return citedby;

}

document.addEventListener('DOMContentLoaded', function() {
    // var searchButton = document.getElementById('search');
    // searchButton.addEventListener('click', function() {
            /*Python version
        def scholar_url_maker(srch_str):
            terms_str = srch_str.replace(" ", "+")
            before_terms = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q="
            after_terms = "&btnG="
            req_url = before_terms + terms_str + after_terms
            return req_url
        */

        // console.log("Beginning Request...\n");

        // var srch_str = document.getElementById("searchterms").value;

        // var before_terms = "http://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
        // var terms_str = srch_str.replace(" ", "+");
        // var after_terms = "&btnG=";
        // var req_url = before_terms + terms_str + after_terms;

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
    //     var raw_html;
    //     // chrome.tabs.query({active: true}, function(tabs) {
    //     //     chrome.tabs.sendMessage(tabs[0].id, {msg:"html_request"}, function(response) {
    //     //             console.log(response.answer);
    //     //             return;
    //     //         }
    //     //     );
    //     // });
    //     // this triggers the schloar tab to send back all of the html data in a message it sends back to us
    //     return;    
    // }, false);
}, false);