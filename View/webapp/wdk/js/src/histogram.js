wdk.util.namespace("wdk.result.histogram", function(ns, $) {
  "use strict";

  var previousPoint = null;

  var init = function(histogram, attrs) {
    // register the tab
    histogram.tabs();

    // get the label for histogram
    var mode = histogram.find("#data").attr("mode");
    if (mode == "") mode = null;
    var header = histogram.find("#data thead tr");
    var binLabel = header.children(".bin").text();
    var sizeLabel = header.children(".size").text();
    
    // get the data for histogram
    var data = [];
    histogram.find("#data .plot-data .data").each(function() {
      var bin = $(this).children(".bin").text();
      var size = parseInt($(this).children(".size").text());
      data.push( [ bin, size ] );
    });
    var options = {
      series: {
        color: "#0044AA",
        bars: { show: true, align: "center", barWidth: 0.9, fill: true, fillColor: { colors: [{ opacity: 0.8 }, "#CCEEFF", { opacity: 0.8 } ] } },
        points: { show: true }
      },
      grid: { hoverable: true, clickable: true },
      xaxis: { mode: "categories", tickLength: 0, axisLabel: binLabel },
      yaxis: { axisLabel: sizeLabel }
    };
    var graph = histogram.find("#graph .plot");
    var plot = $.plot(graph, [ data ], options);
    configurePlot(graph, binLabel, sizeLabel);

    // register the datatable on summary
    histogram.find("#data .datatable").dataTable( {
          "bDestroy": true,
          "bJQueryUI": true,
          "aaSorting": [[ 0, "asc" ]],
      } );

  };

  function showTooltip(x, y, contents) {
    $("<div id='flot-tooltip'>" + contents + "</div>").css({
      top: y - 20,
      left: x + 5,
    }).appendTo("body").fadeIn(500);
  }

  function configurePlot(graph, binLabel, sizeLabel) {
    graph.bind("plothover", function (event, pos, item) {
      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;
          $("#flot-tooltip").remove();

          var data = item.series.data[item.dataIndex];
          var content = sizeLabel + " = " + data[1] + ", in " + binLabel + " = " + data[0];
          showTooltip(item.pageX, item.pageY, content);
        }
      } else {
        $("#flot-tooltip").remove();
        previousPoint = null;            
      }
    });
    graph.find(".flot-x-axis .flot-tick-label").addClass("rotate45");
  }

  ns.init = init;

});
