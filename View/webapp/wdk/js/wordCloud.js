/*  Javascript file to control the actions of certain buttons in the menubar
Cary  Feb. 15, 2010
Basket Button
*/

$(document).ready(function(){
	var cloud = new WordCloud();
	cloud.init();
	cloud.layout();
});

function WordCloud() {
	
	this.init = function(){
	    var cloud = this;
		// register events
		$("#word-cloud #amount").slider({
		    slide: function(event, ui) { cloud.layout(); }
		});
		$("#word-cloud input#sort").change( cloud.layout );
	};
	
	this.layout = function() {
		// get parameters
		var amount = $("#word-cloud #amount").slider("value");
		var sortBy = $("#word-cloud input#sort").val();
		
		var layout = $("#word-cloud #layout");
		layout.html("");
		
		// get the sorted list
		$("#word-cloud #" + sortBy + " span").each(function() {
		    var word = $(this).text();
			var tag = $("#word-cloud #tags span[word=" + word + "]");
			if (tag.val("score") >= amount) {
			    var span = "<span class='word' style='font-size: "
				           + tag.val("weight") + "'>" + word + "</span>";
				layout.append(span);
			}
		});
	};
}