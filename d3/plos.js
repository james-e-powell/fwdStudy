var w = 500;
    h = 500;
 var colorscale = d3.scale.category10();
//Legend titles
var LegendOptions =
["arXiv status 404", "PLOS status 404"]
d = 
[[ 
 {"value": 1.12734864301, "axis": "interval 0"},
 {"value": 1.14822546973, "axis": "interval 1"},
 {"value": 1.16214335421, "axis": "interval 2"},
 {"value": 1.16910229645, "axis": "interval 3"},
 {"value": 1.17606123869, "axis": "interval 4"},
 {"value": 1.18302018093, "axis": "interval 5"},
 {"value": 1.25260960334, "axis": "interval 6"},
 {"value": 1.23173277662, "axis": "interval 7"},
 {"value": 1.54488517745, "axis": "interval 8"},
 {"value": 1.59359777314, "axis": "interval 9"},
 {"value": 1.60055671538, "axis": "interval 10"},
 {"value": 1.67710508003, "axis": "interval 11"},
 {"value": 1.6214335421, "axis": "interval 12"},
 {"value": 1.62839248434, "axis": "interval 13"},
 {"value": 1.59359777314, "axis": "interval 14"}
],[
 {"value": 0.235589759698, "axis": "interval 0"},
 {"value": 0.235589759698, "axis": "interval 1"},
 {"value": 0.235589759698, "axis": "interval 2"},
 {"value": 0.251295743678, "axis": "interval 3"},
 {"value": 0.235589759698, "axis": "interval 4"},
 {"value": 0.204177791739, "axis": "interval 5"},
 {"value": 0.251295743678, "axis": "interval 6"},
 {"value": 0.267001727658, "axis": "interval 7"},
 {"value": 0.282707711638, "axis": "interval 8"},
 {"value": 0.314119679598, "axis": "interval 9"},
 {"value": 0.329825663578, "axis": "interval 10"},
 {"value": 0.345531647558, "axis": "interval 11"},
 {"value": 0.329825663578, "axis": "interval 12"},
 {"value": 0.345531647558, "axis": "interval 13"},
 {"value": 0.361237631538, "axis": "interval 14"}
]];


//Options for the Radar chart, other than default
var mycfg = {
  w: w,
  h: h,
  maxValue: 0.6,
  levels: 6,
  ExtraWidthX: 300
}

//Call function to draw the Radar chart
//Will expect that data is in %'s
RadarChart.draw("#chart", d, mycfg);

////////////////////////////////////////////
/////////// Initiate legend ////////////////
////////////////////////////////////////////

var svg = d3.select('#body')
	.selectAll('svg')
	.append('svg')
	.attr("width", w+300)
	.attr("height", h)

//Create the title for the legend
var text = svg.append("text")
	.attr("class", "title")
	.attr('transform', 'translate(90,0)') 
	.attr("x", w - 70)
	.attr("y", 10)
	.attr("font-size", "12px")
	.attr("fill", "#404040")
	.text("HTTP 404 status comparison");
		
//Initiate Legend	
var legend = svg.append("g")
	.attr("class", "legend")
	.attr("height", 100)
	.attr("width", 200)
	.attr('transform', 'translate(90,20)') 
	;
	//Create colour squares
	legend.selectAll('rect')
	  .data(LegendOptions)
	  .enter()
	  .append("rect")
	  .attr("x", w - 65)
	  .attr("y", function(d, i){ return i * 20;})
	  .attr("width", 10)
	  .attr("height", 10)
	  .style("fill", function(d, i){ return colorscale(i);})
	  ;
	//Create text next to squares
	legend.selectAll('text')
	  .data(LegendOptions)
	  .enter()
	  .append("text")
	  .attr("x", w - 52)
	  .attr("y", function(d, i){ return i * 20 + 9;})
	  .attr("font-size", "11px")
	  .attr("fill", "#737373")
	  .text(function(d) { return d; })
	  ;

