/*  Javascript file to control the actions of certain buttons in the menubar
Cary  Feb. 15, 2010
Basket Button
*/

$(document).ready(function(){
    var cloud = new WordCloud();
    cloud.init(cloud);
});

function WordCloud() {
    
    this.init = function(cloud){
        var tags = $("#word-cloud #tags");
        if (tags.length == 0) return;

        var total = tags.attr("total");
        var value = (total > 50) ? 50 : total;
        // register events
        $("#word-cloud #amount").slider({
            range: true,
            min: 1,
            max: total,
            values: [1, value],
            slide: function(event, ui) {
               var from = $("#word-cloud #amount").slider("values", 0);
               var to = $("#word-cloud #amount").slider("values", 1);
               $("#word-cloud #amount-display").text(from + " - " + to);
            },
            stop: function(event, ui) { cloud.layout(cloud); }
        });
        $("#word-cloud input[name=sort]").change( function() {
                    cloud.layout(cloud); 
        });
        cloud.layout(cloud);
    };
    
    this.layout = function(cloud) {
        // get parameters
        var from = $("#word-cloud #amount").slider("values", 0);
        var to = $("#word-cloud #amount").slider("values", 1);
        var sortBy = $("#word-cloud input[name=sort]:checked").val();

        $("#word-cloud #amount-display").text(from + " - " + to);

        var layout = $("#word-cloud #layout");
        layout.html("");

        var tags = new Array();
        var maxCount = Number.MIN_VALUE;
        var minCount = Number.MAX_VALUE;
        var rank = 0;
        $("#word-cloud #tags span").each(function() {
            rank++;
            if (rank < from) return;
            if (rank > to) return;

            var count = parseInt($(this).attr("count"));
            if (count > maxCount) maxCount = count;
            if (count < minCount) minCount = count;
            tags.push($(this).clone());
        });
        // compute the font size
        cloud.computeSize(tags, minCount, maxCount);

        // sort word alphabetically if needed
        if (sortBy == "word") tags.sort(cloud.sortTags);

        $.each(tags, function (index, tag) {
            layout.append(tag).append(" ");
        });
    };

    this.computeSize = function(tags, minCount, maxCount) {
        // words are sorted by occurence.
        var MAX_FONT = 50.0;
        var MIN_FONT = 6.0;
        var scale = (MAX_FONT - MIN_FONT) / (maxCount - minCount);
        $.each(tags, function (index, tag) {
            var count = parseInt($(tag).attr("count"));
            var fontSize = (count - minCount) * scale + MIN_FONT;
            $(tag).css("font-size", fontSize + "pt");
        });
    };

    this.sortTags = function(left, right) {
        var leftWord = $(left).text();
        var rightWord = $(right).text();
        if (leftWord > rightWord) return 1;
        else if (leftWord < rightWord) return -1;
        else return 0;
    };
}
