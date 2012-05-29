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
  var cell = jQuery(holder).parents("td");
	var noteSpan = cell.find("span.favorite-" + type);
	var noteInput = cell.find(".input.favorite-" + type);
	old_holder = holder;
	jQuery(holder).remove();
  noteSpan.addClass("hidden");
  noteInput.removeClass("hidden");
	var opacity = jQuery(noteSpan).css("opacity");
	jQuery(cell).append("<input id='" + type + "_Save' type='button' value='Save'/>")
	  .append("<input id='" + type + "_Cancel' type='button' value='Cancel'/>");
	jQuery("input#" + type + "_Save", cell).click(function(){ 
		eval(callback); 
	});
	jQuery("input#" + type + "_Cancel", cell).click(function(){
    jQuery("input#" + type + "_Save", cell).remove();
    jQuery("input#" + type + "_Cancel", cell).remove();
    noteInput.addClass('hidden');
    noteSpan.removeClass('hidden');
    cell.append(old_holder);
		old_holder = null;
    jQuery("div#groups-list", cell).remove();
  });
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

function updateFavoriteNote(holder) {
	var record = getRecord(holder);
  var cell = jQuery(holder).parents("td");
  var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
	var noteSpan = cell.find("span.favorite-note");
	var noteInput = cell.find(".input.favorite-note");
	var note = noteInput.val();
	var d = "action=note&note=" + note + "&type=" + rcName + "&data=" + record;

	jQuery.ajax({
		url: "processFavorite.do",
		data: d,
		dataType: "html",
		type: "post",
		success: function(data){
			//jQuery(noteSpan).html(note);
      noteSpan.text(noteInput.val());
      jQuery("input#note_Save", cell).remove();
      jQuery("input#note_Cancel", cell).remove();
      noteInput.addClass('hidden');
      noteSpan.removeClass('hidden');
      cell.append(old_holder);
      old_holder = null;
		}
	});
}

function updateFavoriteGroup(holder) {
	var record = getRecord(holder);
  var cell = jQuery(holder).parents("td");
  var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
	var groupSpan = cell.find("span.favorite-group");
  var groupInput = cell.find("input.favorite-group");
	var group = groupInput.val();
	var d = "action=group&group=" + group + "&type=" + rcName + "&data=" + record;
	jQuery.ajax({
		url: "processFavorite.do",
		data: d,
		dataType: "html",
		type: "post",
		success: function(data){
      groupSpan.text(groupInput.val());
      groupSpan.css("opacity", 1); // this is for user-defined favorite-group
      jQuery("input#group_Save", cell).remove();
      jQuery("input#group_Cancel", cell).remove();
      groupInput.addClass('hidden');
      groupSpan.removeClass('hidden');
      cell.append(old_holder);
      old_holder = null;
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

/***************** Favorite functions to support favorites manipulation from GBrowse ********************/

function performIfItemIsFavorite(projectId, primaryKey, recordType, yesFunction, noFunction) {
	var stop = (primaryKey == 'PFIT_PFE0020c');
	doAjaxFavoritesRequest('check', projectId, primaryKey, recordType,
	    function(result) {
		    if (stop) {
		    	var stoppingPoint = true;
		    }
		    if (result.countProcessed > 0) {
	  			yesFunction();
	  		} else {
	  			noFunction();
	  		}
	  	});
}

function addToFavorites(projectId, primaryKey, recordType, successFunction) {
	doAjaxFavoritesRequest('add', projectId, primaryKey, recordType, successFunction);
}

function removeFromFavorites(projectId, primaryKey, recordType, successFunction) {
	doAjaxFavoritesRequest('remove', projectId, primaryKey, recordType, successFunction);
}

function doAjaxFavoritesRequest(action, projectId, primaryKey, recordType, successFunction) {
	var data = "[{\"source_id\":\"" + primaryKey + "\",\"project_id\":\"" + projectId + "\"}]";
	var requestParams = "action=" + action + "&type=" + recordType + "&data=" + data; // single id data string
	jQuery.ajax({
		url: getWebAppUrl() + "processFavorite.do",
		type: "post",
		data: requestParams,
		dataType: "json",
		beforeSend: function(){ /* do nothing here */ },
		success: successFunction,
		error: function(msg){ alert("Error occurred while executing this operation!"); }
	});
}
