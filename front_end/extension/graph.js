// window.onmessage = (event) => {
//     append(event.data);
// };

var width = window.innerWidth,
  height = window.innerHeight,
  root;

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
  .attr("width", '100%')
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
 "children": [
    {
        "name": "A new technique for building maps",
        "children": [
            {

                "summary_short": "It is a sobering fact that some 90% of papers that have been published in academic journals are never cited. Indeed, as many as 50% of papers are never read by anyone other than their authors, referees and journal editors. We know this thanks to citation analysis, a branch\u00a0\u2026",
                "name": "The rise of citation analysis",
               // "children": [],
                "year": 2007
            },
            {

                "summary_short": "The past few years have seen intensive research efforts carried out in some apparently unrelated areas of dynamic systems\u2013delay-tolerant networks, opportunistic-mobility networks and social networks\u2013obtaining closely related insights. Indeed, the concepts\u00a0\u2026",
                "name": "Time-varying graphs and dynamic networks",
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
                //"children": [],
                "year": 2007
            },
            {
                "summary_short": "Although a large body of knowledge about both brain structure and function has been gathered over the last decades, we still have a poor understanding of their exact relationship. Graph theory provides a method to study the relation between network structure\u00a0\u2026",
                "name": "Functional neural network analysis",
                //"children": [],
                "year": 2009
            },
            {
                "summary_short": "Webometrics is concerned with measuring aspects of the web: web sites, web pages, parts of web pages, words in web pages, hyperlinks, web search engine results. The importance of the web itself as a communication medium and for hosting an increasingly wide array of\u00a0\u2026",
                "name": "Introduction to webometrics",
               // "children": [],
                "year": 2009
            }
        ],
        "name": "Graph signal processing",
        "year": 2018
    },
    {
        "summary_short": "The development of new technologies for mapping structural and functional brain connectivity has led to the creation of comprehensive network maps of neuronal circuits and systems. The architecture of these brain networks can be examined and analyzed with a\u00a0\u2026",
        "name": "Modular brain networks",
        //"children": [],
        "year": 2016
    },
    {
        "summary_short": "Tourism destinations have a necessity to innovate in order to remain competitive in an increasingly global environment. A pre-requisite for innovation is the understanding of how destinations source, share and use knowledge. This conceptual paper examines the nature\u00a0\u2026",
        "name": "Knowledge transfer in a tourism destination",
        //"children": [],
        "year": 2010
    },
    {
        "summary_short": "Recent developments in the quantitative analysis of complex networks, based largely on graph theory, have been rapidly translated to studies of brain network organization. The brain's structural and functional systems have features of complex networks\u2014such as small\u00a0\u2026",
        "name": "Complex brain networks",
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
  link = link.data(links, function(d) { return d.target.id; });

  link.exit().remove();

  link.enter().insert("line", ".node")
    .attr("class", "link");

// Update nodes.
  node = node.data(nodes, function(d) { return d.id; });

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


function addnew(d, newchild){
  if(!d.children){
    d.children= [];
  }
  d.children.push(newchild);
}
var newchild=
            {
                "summary_short": "newchild summary",
                "name": "New Child",
                //"children": [],
                "year": 2021
            };

function click(d){
  if (d3.event.defaultPrevented) return;

  if(document.getElementById("expand").checked  ){

    // // Event listener for msg from background script
    // // Msg has source and list of children
    // //since we clicked, msg should just be list of children
    // window.addEventListener('message', function(e) {
    //     d = e.data[0]
    //     e.data[1]
    //   });



    if (d.children) {
      d._children=d.children;
      d.children= null;
    }else {
      d.children = d._children;
      d._children= null;
    }

    //this needs to be the event listener actually
    if(!d.children && !d._children){
      addnew(d, newchild);
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
