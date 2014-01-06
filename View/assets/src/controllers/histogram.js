wdk.util.namespace("wdk.result.histogram", function(ns, $) {
  "use strict";

  var type;
  var min;
  var max;
  var previousPoint = null;

  var init = function(histogram, attrs) {
    // initialize the properties
    type = attrs.type;
    if (type == "int") {
      min = parseInt(attrs.min);
      max = parseInt(attrs.max);
    } else if (type == "float") {
      min = parseFloat(attrs.min);
      max = parseFloat(attrs.max);
    }

    // initialize UI controls
    initializeControls(histogram);

    // draw the graph
    drawPlot(histogram);
  };


  function initializeControls(histogram) {
    // register tabs
    histogram.tabs();

    // register data table
    histogram.find("#data .datatable").dataTable( {
          "bDestroy": true,
          "bJQueryUI": true,
          "aaSorting": [[ 0, "asc" ]],
    });

    // register bin size control
    var binControl = histogram.find("#graph .bin-control");
    var binSizeInput = binControl.find(".bin-size");
    var binSizeDisplay = binControl.find(".bin-size-display");
    var sliderMin = (type == "float" && min != max) ? ((max - min) / 100) : 1;
    var sliderMax = (type == "float" && min != max) ? (max - min) : (max - min + 1);
    binControl.find(".bin-slider").slider({
      value: binSizeInput.val(),
      min: sliderMin,
      max: sliderMax,
      change: function( event, ui ) {
        binSizeInput.val(ui.value);
        // refresh display after value is changed
        drawPlot(histogram);
      },
      slide: function( event, ui ) {
        binSizeDisplay.html(ui.value);
      }
    });

    // refresh display when value radio changed
    histogram.find("#graph .value-control input").click(function() {
      drawPlot(histogram);
    });
  }


  function drawPlot(histogram) {
    var graph = histogram.find("#graph");
    // get user inputs
    var binSize = graph.find(".bin-control .bin-size").val();
    binSize = (type == "float") ? parseFloat(binSize) : parseInt(binSize);
    var logOption = graph.find(".value-control .logarithm");
    var logarithm = (logOption.attr("checked") == "checked");

    // get data and labels
    var plotDetails = loadData(histogram, binSize, logarithm);
    var data = plotDetails[0];
    var labels = plotDetails[1];
    
    // get plot options
    var header = histogram.find("#data thead tr");
    var binLabel = header.children(".bin").text();
    var sizeLabel = header.children(".count").text();
    var options = getOptions(histogram, binSize, binLabel, sizeLabel, labels);

    // draw plot
    var plotCanvas = graph.find(".plot");
    var plot = $.plot(plotCanvas, [ data ], options);
    plotCanvas.bind("plothover", function (event, pos, item) {
      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;
          $("#flot-tooltip").remove();
          var data = item.series.data[item.dataIndex];
          var typeValue = (logOption.attr("checked") == "checked" ? 'log('+data[1]+')' : data[1]);
          var content = sizeLabel + " = " + typeValue + ", in " + binLabel + " = " + data[0];
          showTooltip(item.pageX, item.pageY, content);
        }
      } else {
        $("#flot-tooltip").remove();
        previousPoint = null;
      }
    });
    // rotate label so it can be displayed without overlap.
    plotCanvas.find(".flot-x-axis .flot-tick-label").addClass("rotate45");
  }


  function loadData(histogram, binSize, logarithm) {
    // load original data.
    var data = [];
    histogram.find("#data .data span").each(function() {
      var bin = $(this).text();
      if (type == "int") bin = parseInt(bin);
      else if (type == "float") bin = parseFloat(bin);
      var count = parseInt($(this).attr("data-count"));
      data.push( [ bin, count ] );
    });

    // convert data into bins and/or logarithm display
    if (type == "category") data = convertCategoryData(data, binSize, logarithm);
    else data = convertNumericData(data, binSize, logarithm);

    return data;
  }


  function convertCategoryData(data, binSize, logarithm) {
    if (binSize == 1 && !logarithm) return data; // no need to convert

    var bins = [];
    var labels = [];
    for (var i = 0; i < data.length; i += binSize) {
      var bin = [];
      var count = 0;
      var upper = Math.max(i + binSize, data.length);
      for (var j = i; j < upper; j++) {
        bin.push(data[j][0]);
        count += data[j][1];
      }

      // now compute new label;
      var label;
      if (bin.length == 0) label = bin[0];
      else {
        for (var k = 0; k < bin.length; k++) {
          label += (k == 0) ? "[" : ", ";
          label += bin[k];
        }
        label += "]";
      }

      // add data into bins
      if (logarithm) count = Math.log(count);
      bins.push([ i, count ]);
      labels.push([ i, label ]);
    }
    return bin;
  }


  function convertNumericData(data, binSize, logarithm) {
    var tempBins = [];
    // create bins
    for (var i = min; i <= max; i += binSize) {
       var bin = [i, i + binSize];
       tempBins.push([ bin, 0 ]);
    }

    // assign rows into each bin
    for (var i = 0; i < data.length; i++) {
      var label = data[i][0];
      for (var j = 0; j < tempBins.length; j++) {
        var bin = tempBins[j][0];
        if (bin[0] <= label && label < bin[1]) {
          tempBins[j][1] = tempBins[j][1] + data[i][1];
          break;
        }
      }
    }

    // now compute new labels
    var bins = [];
    var labels = [];
    for (var j = 0; j < tempBins.length; j++) {
      var bin = tempBins[j][0];
      var label;
      if (binSize == 1 && type == "int") label = bin[0];
      else {
        var upper = (type == "int") ? (bin[1] - 1) + "]" : (bin[1] + ")");
        label = "[" + bin[0] + ", " + upper;
      }
      var count = tempBins[j][1];
      if (logarithm) count = Math.log(count);
      bins.push([ j, count ]);
      labels.push([ j, label ]);
    }
    return [ bins, labels ];
  }


  function getOptions(histogram, binSize, binLabel, sizeLabel, labels) {
    // determine the mode
    //var mode = (type == "float" || binSize != 1) ? "categories" : null;

    var options = {
      series: {
        color: "#0044AA",
        bars: { show: true, align: "center", barWidth: 0.9, fill: true, fillColor: { colors: [{ opacity: 0.8 }, { opacity: 0.6 } ] } },
        points: { show: true }
      },
      grid: { hoverable: true, clickable: true },
      xaxis: { ticks: labels, axisLabel: binLabel },
      yaxis: { axisLabel: sizeLabel }
    };
    return options;
  }


  function showTooltip(x, y, contents) {
    $("<div id='flot-tooltip'>" + contents + "</div>").css({
      top: y - 20,
      left: x + 5,
    }).appendTo("body").fadeIn(500);
  }


  ns.init = init;

});
