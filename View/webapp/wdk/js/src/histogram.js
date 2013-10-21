wdk.util.namespace("wdk.result.histogram", function(ns, $) {
  "use strict";

  var init = function(histogram, attrs) {
    // register the tab
    histogram.tabs();
    
    // draw the histogram
    var data = [];
    histogram.find("#data tr.data").each(function() {
      var bin = $(this).children(".bin").text();
      var size = $(this).children(".size").text();
      data.push(bin, size);
    });
    var options = {
      series: {
          bars: { show: true, fill: true, horizontal:false, align: "center" },
          points: { show: true },
      }
    };
    var plot = histogram.find("#graph .plot").plot(data, options);

    $.each(plot.getData()[0].data, function(i, el){
      var o = plot.pointOffset({x: el[0], y: el[1]});
      $('<div class="data-point-label">' + el[1] + '</div>').css( {
          position: 'absolute',
          left: o.left,
          top: o.top - 15,
          display: 'none'
      }).appendTo(plot.getPlaceholder()).fadeIn('slow');
    });

    // register the datatable on summary
    histogram.find("#data .datatable").dataTable( {
          "bJQueryUI": true,
          "bPaginate": false,
          "aoColumns": [ null, null, { "bSortable": false } ],
          "aaSorting": [[ 0, "asc" ]],
          "sDom": 'lrti'
      } );

  };

  ns.init = init;

});
