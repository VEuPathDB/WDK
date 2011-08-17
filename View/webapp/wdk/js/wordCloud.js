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
            min: 1,
            max: total,
            value: value,
            slide: function(event, ui) {
               var amount = $("#word-cloud #amount").slider("value");
               $("#word-cloud #amount-display").text(amount);
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
        var amount = $("#word-cloud #amount").slider("value");
        var sortBy = $("#word-cloud input[name=sort]:checked").val();

        $("#word-cloud #amount-display").text(amount);

        var layout = $("#word-cloud #layout");
        layout.html("");

        var words = new Array();
        var tags = new Array();
        var count = 0;
        $("#word-cloud #tags span").each(function() {
            if (count >= amount) return;

            var word = $(this).text();
            words[count++] = word;
            tags[word] = $(this).clone();
        });

        // sort word alphabetically if needed
        if (sortBy == "word") words.sort();

        for (var i = 0; i < count; i++) {
            var word = words[i];
            var tag = tags[word];
            layout.append(tag).append(" ");
        }
    };
}
