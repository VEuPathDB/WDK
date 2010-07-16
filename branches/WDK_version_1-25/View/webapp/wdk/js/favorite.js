// cannot use $ for jQuery, since this script is also included in record page, 
// and might have conflict if gbrowse is included.

jQuery(document).ready(function(){
	var firstType = jQuery(".menubar li a:first");
	if (firstType.length > 0) {
		loadFavoriteGroups();
		var current = getCurrentTabCookie("favorites");
		if (current != null) {
			showFavorites(current);
		}
		else {
			firstType.click();
		}
	}
});

function showFavorites(recordClass) {
    jQuery(".menubar li.selected_type").removeClass("selected_type");
    jQuery("#tab_" + recordClass).parent().addClass("selected_type");
    jQuery(".favorites_panel").hide();
    jQuery("#favorites_" + recordClass).show();
    setCurrentTabCookie("favorites", recordClass);
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
			var new_title = (action == "add") ? "Click to remove this item from the favorites." : "Click to add this item to the favorites";
			var new_prompt = (action == "add") ? "Remove from Favorites" : "Add to Favorites";
			jQuery("#favoritesrp").text(new_prompt);
			star_arr = jQuery(holder).attr("src").split("_");
			star_arr[1] = star + ".gif";
			star_src = star_arr.join("_");
			jQuery(holder).attr("src",star_src);
			jQuery(holder).attr("title",new_title);
			jQuery(holder).removeAttr("onclick");
			jQuery(holder).unbind('click');
			jQuery(holder).bind('click', function(){ updateFavorite(this, new_action); });
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
var old_holder = null;

function showInputBox(holder, type, callback){
	if(old_holder != null){
		jQuery("input[id$='_Cancel']").click();
	}
	var noteSpan = jQuery(holder).parents("td").find("span.favorite-" + type);
	old_holder = holder;
	jQuery(holder).remove();
	var note = jQuery(noteSpan).text();
	var opacity = jQuery(noteSpan).css("opacity");
	jQuery(noteSpan).css("opacity","1");
	var value = note;
	if(type == 'group') {
		var perc = 60;
		var maxlen = 42;
	}else{
		var perc = 80;
		var maxlen = 198;
	}
	jQuery(noteSpan).html("<input style='width:" + perc  + "%' type='text' name='favorite-" + type + "' value='" + value + "' maxlength='" + maxlen + "'/>");
	jQuery(noteSpan).append("<input id='" + type + "_Save' type='button' value='Save'/>");
	jQuery(noteSpan).append("<input id='" + type + "_Cancel' type='button' value='Cancel'/>");
	jQuery("input#" + type + "_Save", noteSpan).click(function(){ 
		eval(callback); 
		jQuery(this).parent().after(old_holder);
		old_holder = null;
	});
	jQuery("input#" + type + "_Cancel", noteSpan).click(function(){ CancelChange(this, value, opacity); });
	if(type == 'group' && jQuery("div#groups-list ul li").length > 0){
		var groupList = jQuery("div#groups-list").clone();
		jQuery("li", groupList).each(function(){
			jQuery(this).click(function(){
				jQuery("span.favorite-group input[type='text']").val(jQuery(this).text());
			});
		});
		jQuery(noteSpan).append(groupList.css("display","block"));
		jQuery("input[type='text']",noteSpan).attr("size","");
	}
	jQuery("input", noteSpan).focus().select();
}

function CancelChange(h, v, o){
	h = jQuery(h).parent();
	h.html(v);
	h.after(old_holder);
	old_holder = null;
	jQuery("div#groups-list", h).remove();
	if(o != "1")
		h.css("opacity",o);
}

function updateFavoriteNote(holder) {
	var record = getRecord(holder);
    var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
	var noteSpan = jQuery(holder).parents("td").find("span.favorite-note");
	var note = jQuery("input",noteSpan).val();
	var d = "action=note&note=" + note + "&type=" + rcName + "&data=" + record;
	jQuery.ajax({
		url: "processFavorite.do",
		data: d,
		dataType: "html",
		type: "post",
		success: function(data){
			jQuery(noteSpan).html(note);
		}
	});
}

function updateFavoriteGroup(holder) {
	var record = getRecord(holder);
    var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
	var groupSpan = jQuery(holder).parents("td").find("span.favorite-group");
	var group = jQuery("input",groupSpan).val();
	var d = "action=group&group=" + group + "&type=" + rcName + "&data=" + record;
	jQuery.ajax({
		url: "processFavorite.do",
		data: d,
		dataType: "html",
		type: "post",
		success: function(data){
			jQuery(groupSpan).css("opacity","1.0").html(group);
			loadFavoriteGroups();
		}
	});
	jQuery("div#groups-list", groupSpan).remove();

}

function loadFavoriteGroups() {
	jQuery.ajax({
		url: "showFavorite.do?showGroup=true",
		dataType: "json",
		type: "post",
		success: function(data){
			var l = jQuery("div#groups-list ul");
			jQuery(l).html("");
			for(i in data){
				var li = document.createElement('li');
				jQuery(li).css({
					cursor: "pointer",
					padding: "3px"
				});
				if(i%2 == 0)
					jQuery(li).css("background-color","#FFFFFF");
				else
					jQuery(li).css("background-color","#DDDDDD");
				jQuery(li).html(data[i]);
				jQuery(l).append(li);
			}
		}
	});
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
