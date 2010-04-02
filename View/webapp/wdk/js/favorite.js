// cannot use $ for jQuery, since this script is also included in record page, 
// and might have conflict if gbrowse is included.

function showFavorites() {

}

function updateFavorite(holder, action) {
    // locate the favorite
    var record = getRecord(holder);
    var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
	var d = "type=" + rcName + "&action=" + action + "&data=" + record;
    var noUpdate = true;
	jQuery.ajax({ 
		url: "processFavorite.do",
        data : d,
		dataType: "html",
		type : "post",
        success: function(data){
        	
			var star = (action == "add") ? "color" : "gray";
			var new_action = (action == "add") ? "remove" : "add";
			star_arr = jQuery(holder).attr("src").split("_");
			star_arr[1] = star + ".gif";
			star_src = star_arr.join("_");
			jQuery(holder).attr("src",star_src);
			jQuery(holder).unbind("click");
			jQuery(holder).bind('click', function(){ updateFavorite(this, new_action)});
			// not sure what this is for
			//if (!noUpdate) { // For things like moving columns, do not need to refresh
            //	show();
            //}
		},
        error : function(data, msg, e){
        	alert("ERROR \n "+ msg + "\n" + e + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
        }	
    });
}

function updateFavoriteNote(holder) {

}

function updateFavoriteGroup(holder) {

}

function loadFavoriteGroups() {

}

function getRecord(holder) {
    // locate the record
    var record = jQuery(holder).parents(".wdk-record");
    var pkArray = new Array();
    record.find(".primaryKey").each(function() {
        var pkValues = new Object();
		jQuery(this).find("span").each(function(){
			var key = jQuery(this).attr("key");
        	var value = jQuery(this).text();
        	pkValues[key] = value;
		});
		pkArray.push(pkValues);
    });
    return jQuery.json.serialize(pkArray);
} 
