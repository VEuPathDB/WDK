wdk.util.namespace("window.wdk.util", function(ns, $) {
  "use strict";

  // TODO - change wdk.util.namespace to wdk.namespace

  //show the loading icon in the upper right corner of the strategy that is being operated on
  function showLoading(divId) {
    var d;
    var le;
    var t;
    var l_gif;
    var sz;

    if (divId == undefined) {
      d = $("#Strategies");
      le = "10px";
      t = "15px";
      l_gif = "loading.gif";
      sz = "35";
    } else if($("#diagram_" + divId).length > 0) {
      d = $("#diagram_" + divId);
      le = "10px";
      t = "12px";
      l_gif = "loading.gif";
      sz = "35";
    } else {
      d = $("#" + divId);
      le = "405px";
      t = "160px";
      l_gif = "loading.gif";
      sz = "50";
    }

    var l = document.createElement('span');
    $(l).attr("id","loadingGIF");
    var i = document.createElement('img');
    $(i).attr("src","wdk/images/" + l_gif);
    $(i).attr("height",sz);
    $(i).attr("width",sz);
    $(l).prepend(i);
    $(l).css({
      "text-align": "center",
      position: "absolute",
      left: le,
      top: t
    });
    $(d).append(l);
  }

  // remove the loading icon for the given strategy
  function removeLoading(divId) {
    if (divId == undefined) {
      $("#Strategies span#loadingGIF").remove();
    } else {
      $("#diagram_" + divId + " span#loadingGIF").remove();
    }
  }

  // parses the inputs of the question form to be sent via ajax call
  function parseInputs() {
    // has to use find in two steps, IE7 cannot find the form using 
    // $("#query_form form#form_question")
    var quesForm = $("#query_form").find("form#form_question");
          
    // if the questionForm is popupped by other ways, get it from the opened popup under body.
    if (quesForm.length == 0) {
      quesForm = $("body").children("div.crumb_details").find("form#form_question");
    }

    // Jerric - use ajax to serialize the form data
    var d = quesForm.serialize();
    return d;
  }

  function checkEnter(ele,evt) {
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if(charCode == 13) $(ele).blur();
  }

  function parseUrlUtil(name,url) {
     name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
     var regexS = "[\\?&]" + name + "=([^&#]*)";
     var regex = new RegExp( regexS,"g" );
     var res = [];
     var results = regex.exec( url );
     if ( results != null ) {
       res.push(results[1]);
     }
     if (res.length == 0) {
       return "";
     } else {
       return res;
     }
  }

/* cris 3-26-13: added function below using values from backend
  function getDisplayType(type, number) {
    if (number == 1) {
      return type;
    } else if (type.charAt(type.length-1) === 'y') {
      return type.replace(/y$/,'ies');

    } else {
      return type + 's';
    }
  }
*/
  function getDisplayType(myStep) {
 		return ( (myStep.results > 1) ? myStep.shortDisplayTypePlural : myStep.shortDisplayType );
	}

  function initShowHide(details) {
    $(".param-group[type='ShowHide']",details).each(function() {
      // register the click event
      var name = $(this).attr("name") + "_details";
      var expire = 365;   // in days
      $(this).find(".group-handle").unbind('click').click(function() {
        var handle = this;
        var path = handle.src.substr(0, handle.src.lastIndexOf("/"));
        var detail = $(this).parents(".param-group").children(".group-detail");
        detail.toggle();
        if (detail.css("display") == "none") {
          handle.src = path + "/plus.gif";
          wdk.createCookie(name, "hide", expire);
        } else {
          handle.src = path + "/minus.gif";
          wdk.createCookie(name, "show", expire);
        }
      });

      // decide whether need to change the display or not
      var showFlag = wdk.readCookie(name);
      if (showFlag == null) return;
          
      var status = $(this).children(".group-detail").css("display");
      if ((showFlag == "show") && (status == "none")) {   
        // should show, but it is hidden
        $(this).find(".group-handle").trigger("click");
      } else if ((showFlag == "hide") && (status != "none")) {
        // should hide, bit it is shown
        $(this).find(".group-handle").trigger("click");
      }
    });
  }

  function setFrontAction(action, strat, step) {
    $("#loginForm form[name=loginForm]").append("<input type='hidden' name='action' value='" + action + "'/>");
    $("#loginForm form[name=loginForm]").append("<input type='hidden' name='actionStrat' value='" + strat + "'/>");
    $("#loginForm form[name=loginForm]").append("<input type='hidden' name='actionStep' value='" + step + "'/>");
  }

  function setDraggable(e, handle) {
    var rlimit,
        tlimit,
        blimit;
    rlimit = $("div#contentwrapper").width() - e.width() - 18;
    if (rlimit < 0) rlimit = 525;
    blimit = $("body").height();
    tlimit = $("div#contentwrapper").offset().top;
    $(e).draggable({
      handle: handle,
      containment: [0, tlimit, rlimit, blimit]
    });
  }

  function popLogin() {
    $.blockUI({message: '<h1>You have to be logged in to do that!</h1><input type="button" value="OK" onclick="jQuery.unblockUI();" />'});
  }

  // Credit to Jason Bunting and Alex Nazarov for this helpful function
  // See: http://stackoverflow.com/questions/359788/how-to-execute-a-javascript-function-when-i-have-its-name-as-a-string
  function executeFunctionByName(functionName, ns, context /*, args... */) {
      var args = Array.prototype.slice.call(arguments, 3);
      var namespaces = functionName.split(".");
      var func = namespaces.pop();
      for (var i = 0; i < namespaces.length; i++) {
          ns = ns[namespaces[i]];
      }
      if (ns[func] instanceof Function &&
          (context instanceof Object ||
          /* Node is not an object in IE < 9 */ context.nodeType)) {
        return ns[func].apply(context, args);
      } else {
        if (typeof console === "object") {
          console.error("Reference error: " + functionName + " is not a function");
        }
        return false;
      }
  }
  
  function executeOnloadFunctions(selector) {
    $(".onload-function", selector).each(function() {
      var data = $(this).data();
      if (data.invoked) return true;
      executeFunctionByName(data["function"], window, this, data.arguments);
      $(this).data("invoked", true).removeClass("onload-function");
    });
   }

  function sendContactRequest(form, successCallback) {
    // send request
    $.post($(form).attr("action"), $(form).serialize(), function(data) {
      switch (data.status) {
    
        case "success":
          successCallback(data);
          break;
    
        case "error":
          var response = "<h3>Please try to correct any errors below</h3>" +
              "<br/>" + data.message;
          $("<div></div>").html(response).dialog({
            title: "Oops! An error occurred.",
            buttons: [{
              text: "OK",
              click: function() { $(this).dialog("close"); }
            }],
            modal: true
          });
          break;
      }
    }, "json").error(function(jqXHR, textStatus, errorThrown) {
      var response = "<h3>A " + textStatus + " error occurred.</h3><br/>" +
          "<p>This indicates a problem with our server. Please email " +
          "support directly.";
      $("<div></div>").html(response).dialog({
        title: "Oops! An error occurred.",
        buttons: [{
          text: "OK",
          click: function() { $(this).dialog("close"); }
        }],
        modal: true
      });
    });
  }

  function playSadTrombone() {
    $('body').append("<iframe width=\"0px\" height=\"0px\" src=\"http://sadtrombone.com/?play=true\"></iframe>");
  }

  function submitError() {
    var errorForm = $('#error-submission-form')[0];
    sendContactRequest(errorForm, function(){
      $('#open-error-thanks-link').click();
    });
  }

  function toggleErrorDetails() {
    var jqExceptionDiv = $('#exception-information');
    jqExceptionDiv.toggle();
    $('#exception-details-link').html(jqExceptionDiv.is(':hidden') ?
        "Show Details" : "Hide Details");
  }

  ns.getDisplayType = getDisplayType;
  ns.initShowHide = initShowHide;
  ns.parseUrlUtil = parseUrlUtil;
  ns.parseInputs = parseInputs;
  ns.removeLoading = removeLoading;
  ns.setDraggable = setDraggable;
  ns.setFrontAction = setFrontAction;
  ns.showLoading = showLoading;
  ns.checkEnter = checkEnter;
  ns.executeOnloadFunctions = executeOnloadFunctions;
  ns.sendContactRequest = sendContactRequest;
  ns.playSadTrombone = playSadTrombone;
  ns.submitError = submitError;
  ns.toggleErrorDetails = toggleErrorDetails;

});
