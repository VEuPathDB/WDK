/* global wdk */

import { debounce } from 'lodash';

wdk.namespace("wdk.result.histogram", function(ns, $) {
  "use strict";

  function init($el, attrs) {
    require([
      'lib/jquery-flot',
      'lib/jquery-flot-categories',
      'lib/jquery-flot-selection',
      'lib/jquery-flot-time'
    ], function() {
      // initialize the properties
      const type = attrs.type;
      let min, max;
      if (type == "int") {
        min = parseInt(attrs.min);
        max = parseInt(attrs.max);
      } else if (type == "float") {
        min = parseFloat(attrs.min);
        max = parseFloat(attrs.max);
      }

      const view = { $el, type, min, max }

      // initialize UI controls
      initializeControls(view);

      // draw the graph
      drawPlot(view);
    });
  }


  function initializeControls(view) {
    let dataTable;
    const { $el, min, max, type } = view;
    const drawPlot_ = debounce(drawPlot, 300);

    // register tabs
    $el.tabs({
      activate: function(event, ui) {
        if (ui.newPanel.attr('id') === 'data') {
          dataTable.draw();
        }
      }
    });

    // register data table
    dataTable = $el.find("#data .datatable").wdkDataTable( {
      "bDestroy": true,
      "aaSorting": [[ 0, "asc" ]],
    }).DataTable();

    // register bin size control
    const binControl = $el.find("#graph .bin-control");

    // input for selecting/displaying the size of the bins
    const binSizeInput = binControl.find(".bin-size");
    const binSizeSlider = binControl.find(".bin-size-slider");

    // input and slider for selecting/displaying the number of bins (1-100)
    const binCountInput = binControl.find('.bin-count');
    const binCountSlider = binControl.find(".bin-count-slider");

    // the min allowed bin size should create no more than the max allowed number of bins
    const minAllowedSize = Math.max(min.toFixed(2), (max / binCountInput.attr('max')).toFixed(2));
    binSizeInput.attr('min', minAllowedSize);
    binSizeSlider.attr('min', minAllowedSize);

    const handleSizeEvent = handleEvent(sizeToData);
    const handleCountEvent = handleEvent(countToData);

    binSizeInput.on('change input keydown', handleSizeEvent);
    binSizeSlider.on('change input keydown', handleSizeEvent);
    binCountInput.on('change input keydown', handleCountEvent);
    binCountSlider.on('change input keydown', handleCountEvent);

    // refresh display when value radio changed
    $el.find("#graph .value-control input").click(function() {
      drawPlot_(view);
    });

    function sizeToData(rawSize) {
      return {
        size: Number(rawSize).toFixed(2),
        count: Math.ceil(max / rawSize)
      };
    }

    function countToData(rawCount) {
      // if type is int, make sure size is a whole number
      if (type === 'int') {
        const size = Math.round(max / rawCount);
        const count = Math.ceil(max / size)
        return { size, count };
      }

      return {
        size: (max / rawCount).toFixed(2),
        count: rawCount
      };
    }

    function handleEvent(valueToData) {
      return function update(event) {
        // capture ENTER for inputs
        if (event.which === 13) {
          event.stopPropagation();
          event.preventDefault();
        }

        // Ignore invalid input
        if (!event.target.validity.valid) return;

        const { count, size } = valueToData(event.target.value);
        binSizeInput.val(size);
        binSizeSlider.val(size);
        binCountInput.val(count);
        binCountSlider.val(count);

        if (event.type === 'change') drawPlot_(view);
      }
    }
  }

  
  function drawPlot(view) {
    const { $el, type } = view;
    var graph = $el.find("#graph");
    // get user inputs
    var binSize = graph.find(".bin-control .bin-size").val();
    binSize = (type == "float") ? parseFloat(binSize) : parseInt(binSize);
    var logOption = graph.find(".value-control .logarithm");
    var logarithm = (logOption.attr("checked") == "checked");

    // get data and labels
    var plotDetails = loadData(view, binSize, logarithm);
    var data = plotDetails[0];
    var labels = plotDetails[1];
    
    // get plot options
    // use .first() because dataTable plugin creates two tables for the
    // scrollable body
    var header = $el.find("#data thead tr").first();
    var binLabel = header.children(".label").text();
    var sizeLabel = header.children(".count").text();
    var options = getOptions(view, binSize, binLabel, sizeLabel, labels);

    // draw plot
    var plotCanvas = graph.find(".plot");
    var previousPoint = null;
    $.plot(plotCanvas, [ data ], options);
    plotCanvas
      .off("plothover")
      .on("plothover", function (event, pos, item) {
        if (item) {
          if (previousPoint != item.dataIndex) {
            previousPoint = item.dataIndex;
            $("#flot-tooltip").remove();
            var data = item.series.data[item.dataIndex];
            var typeValue = (logOption.attr("checked") == "checked" ? 'log('+data[1]+')' : data[1]);
            var content = sizeLabel + " = " + typeValue + ", in " + binLabel + " = " + labels[item.dataIndex][1];
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


  function loadData(view, binSize, logarithm) {
    const { $el, type } = view;
    // load original data.
    var data = [];
    $el.find("#data .data span").each(function() {
      var bin = $(this).text();
      if (type == "int") bin = parseInt(bin);
      else if (type == "float") bin = parseFloat(bin);
      var count = parseInt($(this).attr("data-count"));
      data.push( [ bin, count ] );
    });

    // convert data into bins and/or logarithm display
    if (type == "category") data = convertCategoryData(data, binSize, logarithm);
    else data = convertNumericData(data, view, binSize, logarithm);

    return data;
  }


  function convertCategoryData(data, binSize, logarithm) {
    //if (binSize == 1 && !logarithm) return data; // no need to convert

    var bins = [];
    var labels = [];
    var bin;
    for (var i = 0; i < data.length; i += binSize) {
      bin = [];
      var count = 0;
      var upper = Math.min(i + binSize, data.length);
      for (var j = i; j < upper; j++) {
        bin.push(data[j][0]);
        count += data[j][1];
      }

      // now compute new label;
      var label = "";
      if (bin.length === 1) {
        label = bin[0];
        if(label.length > 20) {
          label = label.substring(0,20) + "...";
        }
      }
      else {
        for (var k = 0; k < bin.length; k++) {
          label += (k === 0) ? "[" : ", ";
          label += bin[k];
        }
        label += "]";
      }

      // add data into bins
      if (logarithm) count = Math.log(count);
      bins.push([ i, count ]);
      labels.push([ i, label ]);
    }
    return [bins,labels];
  }


  function convertNumericData(data, view, binSize, logarithm) {
    const { type, min, max } = view;
    var tempBins = [];
    var bin;
    var label;
    var i;
    var j;
    // create bins
    for (i = min; i <= max; i += binSize) {
      bin = [i, i + binSize];
      tempBins.push([ bin, 0 ]);
    }

    // assign rows into each bin
    for (i = 0; i < data.length; i++) {
      label = data[i][0];
      for (j = 0; j < tempBins.length; j++) {
        bin = tempBins[j][0];
        if (bin[0] <= label && label < bin[1]) {
          tempBins[j][1] = tempBins[j][1] + data[i][1];
          break;
        }
      }
    }

    // now compute new labels
    var bins = [];
    var labels = [];
    for (j = 0; j < tempBins.length; j++) {
      bin = tempBins[j][0];
      if (binSize == 1 && type == "int") label = bin[0];
      else {
        if (type == "float") {
          bin[0] = bin[0].toFixed(2);
          bin[1] = bin[1].toFixed(2);
        }
        var upper = (type == "int") ? (bin[1] - 1) + "]" : bin[1] + ")";
        label = "[" + bin[0] + ", " + upper;
      }
      var count = tempBins[j][1];
      if (logarithm) count = Math.log(count);
      bins.push([ j, count ]);
      labels.push([ j, label ]);
    }
    return [ bins, labels ];
  }


  function getOptions(view, binSize, binLabel, sizeLabel, labels) {
    // determine the mode
    // const { type } = view;
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
