/*  Javascript file to control the actions of certain buttons in the menubar
Cary  Feb. 15, 2010
Basket Button
*/

wdk.util.namespace("window.wdk.result.wordCloud", function(ns, $) {
  "use strict";

  var init = function(wordCloud, attrs) {
    var tags = wordCloud.find("#tags");
    if (tags.length == 0) return;

    // register tabs
    wordCloud.tabs();

    // register data table
    wordCloud.find("#data .datatable").dataTable( {
          "bDestroy": true,
          "bJQueryUI": true,
          "aaSorting": [[ 1, "desc" ]],
    });

    var total = tags.attr("total");
    var from = 1;
    var to = (total > 50) ? 50 : total;
    wordCloud.find("input[name=from]").val(from);
    wordCloud.find("input[name=to]").val(to);

    // register events
    wordCloud.find("#amount").slider({
        range: true,
        min: 1,
        max: total,
        values: [from, to],
        slide: function(event, ui) {
          assignRange(wordCloud);
        },
        stop: function(event, ui) { 
          assignRange(wordCloud);
          layout(wordCloud); 
        }
    });
    wordCloud.find("input[name=sort], input[name=from], input[name=to]").change( function() {
        layout(wordCloud); 
    });

    layout(wordCloud);
  }
    
  function assignRange(wordCloud) {
      var amount = wordCloud.find("#amount");
      var from = amount.slider("values", 0);
      var to = amount.slider("values", 1);
      wordCloud.find("input[name=from]").val(from);
      wordCloud.find("input[name=to]").val(to);
  }
    
  function layout(wordCloud) {
      // get parameters
      var from = wordCloud.find("input[name=from]").val();
      var to = wordCloud.find("input[name=to]").val();
      var sortBy = wordCloud.find("input[name=sort]:checked").val();

      var layout = wordCloud.find("#layout");
      layout.html("");

      var tags = [];
      var maxCount = Number.MIN_VALUE;
      var minCount = Number.MAX_VALUE;
      var rank = 0;
      wordCloud.find("#tags span").each(function() {
        rank++;
        if (rank < from) return;
        if (rank > to) return;

        var count = parseInt($(this).attr("count"), 10);
        if (count > maxCount) maxCount = count;
        if (count < minCount) minCount = count;
        tags.push($(this).clone());
      });
      // compute the font size
      computeSize(tags, minCount, maxCount);

      // sort word alphabetically if needed
      if (sortBy == "word") tags.sort(sortTags);

      $.each(tags, function (index, tag) {
        layout.append(tag).append(" ");
      });
    }

    function computeSize(tags, minCount, maxCount) {
      // words are sorted by occurence.
      var MAX_FONT = 50.0;
      var MIN_FONT = 6.0;
      var scale = (MAX_FONT - MIN_FONT) / (maxCount - minCount);
      $.each(tags, function (index, tag) {
        var count = parseInt($(tag).attr("count"), 10);
        var fontSize = (count - minCount) * scale + MIN_FONT;
        $(tag).css("font-size", fontSize + "pt");
      });
    }

    function sortTags(left, right) {
      var leftWord = $(left).text();
      var rightWord = $(right).text();
      if (leftWord > rightWord) return 1;
      else if (leftWord < rightWord) return -1;
      else return 0;
    }

  ns.init = init;

});
