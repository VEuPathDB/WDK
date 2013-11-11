wdk.util.namespace("window.wdk.favorite", function(ns, $) {
  "use strict";

  function init(element) {
    var firstType = element.find(".menubar li a:first");
    if (firstType.length > 0) {
      loadFavoriteGroups();
      var current = wdk.stratTabCookie.getCurrentTabCookie("favorites");
      if (current != null) {
        showFavorites(current);
      }
      else {
        firstType.click();
      }
    }
  }

  function showFavorites(recordClass) {
    $(".menubar li.selected_type").removeClass("selected_type");
    $("#tab_" + recordClass).parent().addClass("selected_type");
    $(".favorites_panel").hide();
    $("#favorites_" + recordClass).show();
    wdk.stratTabCookie.setCurrentTabCookie("favorites", recordClass);
  }

  function updateFavorite(holder, action) {
    // locate the favorite
    var record = getRecord(holder);
    var rcName = $(holder).parents(".wdk-record").attr("recordClass");
    var d = "type=" + rcName + "&action=" + action + "&data=" + record;
    var noUpdate = true;
    $.ajax({ 
      url: "processFavorite.do",
      data : d,
      dataType: "html",
      type : "post",
      success: function(data) {
        var star = (action == "add") ? "color" : "gray";
        var new_action = (action == "add") ? "remove" : "add";
        var new_title = (action == "add") ? "Click to remove this item from the favorites." : "Click to add this item to the favorites";
        var new_prompt = (action == "add") ? "Remove from Favorites" : "Add to Favorites";
        $("#favoritesrp").text(new_prompt);
        var star_arr = $(holder).attr("src").split("_");
        star_arr[1] = star + ".gif";
        var star_src = star_arr.join("_");
        $(holder).attr("src",star_src);
        $(holder).attr("title",new_title);
        $(holder).removeAttr("onclick");
        $(holder).unbind('click');
        $(holder).bind('click', function(){ updateFavorite(this, new_action); });
        // not sure what this is for
        //if (!noUpdate) { // For things like moving columns, do not need to refresh
        //  show();
        //}
      },
      error : function(data, msg, e){
        alert("ERROR \n "+ msg + "\n" + e + ". \n" +
            "Reloading this page might solve the problem. \n" +
            "Otherwise, please contact site support.");
      }
    });
  }

  var old_holder = null;

  function showInputBox(holder, type, callback) {
    if (old_holder != null) {
      $("input[id$='_Cancel']").click();
    }

    var cell = $(holder).parents("td");
    var noteSpan = cell.find("span.favorite-" + type);
    var noteInput = cell.find(".input.favorite-" + type);
    old_holder = holder;
    $(holder).remove();
    noteSpan.addClass("hidden");
    noteInput.removeClass("hidden");
    var opacity = $(noteSpan).css("opacity");
    $(cell).append("<input id='" + type + "_Save' type='button' value='Save'/>")
        .append("<input id='" + type + "_Cancel' type='button' value='Cancel'/>");
    $("input#" + type + "_Save", cell).click(function() {
      eval(callback); 
    });
    $("input#" + type + "_Cancel", cell).click(function() {
      $("input#" + type + "_Save", cell).remove();
      $("input#" + type + "_Cancel", cell).remove();
      noteInput.addClass('hidden');
      noteSpan.removeClass('hidden');
      cell.append(old_holder);
      old_holder = null;
      $("div#groups-list", cell).remove();
    });

    if (type == 'group' && $("div#groups-list ul li").length > 0) {
      var groupList = $("div#groups-list").clone();
      $("li", groupList).each(function(){
        $(this).click(function(){
          $("span.favorite-group input[type='text']").val($(this).text());
        });
      });
      $(noteSpan).append(groupList.css("display","block"));
      $("input[type='text']",noteSpan).attr("size","");
    }

    $("input", noteSpan).focus().select();
  }

  function updateFavoriteNote(holder) {
    var record = getRecord(holder);
    var cell = $(holder).parents("td");
    var rcName = $(holder).parents(".wdk-record").attr("recordClass");
    var noteSpan = cell.find("span.favorite-note");
    var noteInput = cell.find(".input.favorite-note");
    var note = noteInput.val();
    var d = "action=note&note=" + note + "&type=" + rcName + "&data=" + record;

    if (note.length > 200) {
      alert("Your note exceeds the maximum of 200 characters. Please update " +
          "your note to use no more than 200 characters.\n\n" +
          "Current number of characters: " + note.length);
      return false;
    }

    $.ajax({
      url: "processFavorite.do",
      data: d,
      dataType: "html",
      type: "post",
      success: function(data) {
        //$(noteSpan).html(note);
        noteSpan.text(noteInput.val());
        $("input#note_Save", cell).remove();
        $("input#note_Cancel", cell).remove();
        noteInput.addClass('hidden');
        noteSpan.removeClass('hidden');
        cell.append(old_holder);
        old_holder = null;
      }
    });
  }

  function updateFavoriteGroup(holder) {
    var record = getRecord(holder);
    var cell = $(holder).parents("td");
    var rcName = $(holder).parents(".wdk-record").attr("recordClass");
    var groupSpan = cell.find("span.favorite-group");
    var groupInput = cell.find("input.favorite-group");
    var group = groupInput.val();
    var d = "action=group&group=" + group + "&type=" + rcName + "&data=" + record;
    $.ajax({
      url: "processFavorite.do",
      data: d,
      dataType: "html",
      type: "post",
      success: function(data) {
        groupSpan.text(groupInput.val());
        groupSpan.css("opacity", 1); // this is for user-defined favorite-group
        $("input#group_Save", cell).remove();
        $("input#group_Cancel", cell).remove();
        groupInput.addClass('hidden');
        groupSpan.removeClass('hidden');
        cell.append(old_holder);
        old_holder = null;
        loadFavoriteGroups();
      }
    });
    $("div#groups-list", groupSpan).remove();
  }

  function loadFavoriteGroups() {
    $.ajax({
      url: "showFavorite.do?showGroup=true",
      dataType: "json",
      type: "post",
      success: function(data){
        var i;
        var l = $("div#groups-list ul");
        $(l).html("");

        for (i in data) {
          var li = document.createElement('li');
          $(li).css({
            cursor: "pointer",
            padding: "3px"
          });

          if (i%2 == 0) {
            $(li).css("background-color","#FFFFFF");
          } else {
            $(li).css("background-color","#DDDDDD");
          }

          $(li).html(data[i]);
          $(l).append(li);
        }
      }
    });
  }

  function getRecord(holder) {
    // locate the record
    var record = $(holder).parents(".wdk-record");
    var pkArray = [];
    record.find(".primaryKey").each(function() {
      var pkValues = {};
      $(this).find("span").each(function() {
        var key = $(this).attr("key");
        var value = $(this).text();
        pkValues[key] = value;
      });
      pkArray.push(pkValues);
    });
    return $.json.serialize(pkArray);
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
    $.ajax({
      url: wdk.getWebAppUrl() + "processFavorite.do",
      type: "post",
      data: requestParams,
      dataType: "json",
      beforeSend: function(){ /* do nothing here */ },
      success: successFunction,
      error: function(msg){ alert("Error occurred while executing this operation!"); }
    });
  }

  ns.init = init;
  ns.performIfItemIsFavorite = performIfItemIsFavorite;
  ns.showFavorites = showFavorites;
  ns.showInputBox = showInputBox;
  ns.updateFavorite = updateFavorite;
  ns.updateFavoriteGroup = updateFavoriteGroup;
  ns.updateFavoriteNote = updateFavoriteNote;

});
