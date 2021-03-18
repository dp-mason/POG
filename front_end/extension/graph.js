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


d3.json("graph.json", function(error, json) {
    if (error) throw error;
    root = json;
    update();
});

    
// var nodeSelection = svg.selectAll('.node')
//     .data(nodes)
//     .enter()
//     .append('g')
//     .classed('node', true)
//     .call(force.drag());

// nodeSelection.append('circle')
//     .attr('r', 20)
//     .attr('data-node-index', function(d,i) { return i;})
//     .style('fill', "#ccc")

function update() {
    var nodes = flatten(root),
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
    return d._children ? "#3182bd" // collapsed package
        : d.children ? "#c6dbef" // expanded package
        : "#fd8d3c"; // leaf node
}

// Function to handle clicks on node elements

function click(d){

    if (d3.event.defaultPrevented) return;

    if(document.getElementById("expand").checked){
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
        update();
    }

    else if(document.getElementById("info").checked  ){

        // In all cases we start by resetting
        // all the nodes and edges to their
        // de-selected state.

        // nodeSelection
        //     .each(function(d) { d.selected = false; })
        //     .selectAll('circle')
        //         .transition()
        //         .attr('r', nodeRadius)
        //         .style('fill', color(d));

        if (!d.selected) {
                
            // d3.selectAll('circle[data-node-index="'+node.index+'"]')
            //   .transition()
            //   .attr('r', 50)
            //   .style('fill', '#7ef084');

            // Delete the current notes section to prepare
            // for new information.

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
        if (node.children) node.children.forEach(recurse);
        if (!node.id) node.id = ++i;
        nodes.push(node);
    }

    recurse(root);
    return nodes;
}
