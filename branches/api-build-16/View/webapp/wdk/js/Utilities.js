//
// Static library of utility functions
//
var Utilities = {

	// Credit to Jason Bunting and Alex Nazarov for this helpful function
	// See: http://stackoverflow.com/questions/359788/how-to-execute-a-javascript-function-when-i-have-its-name-as-a-string
	executeFunctionByName : function(functionName, context /*, args... */) {
	    var args = Array.prototype.slice.call(arguments, 2);
	    var namespaces = functionName.split(".");
	    var func = namespaces.pop();
	    for (var i = 0; i < namespaces.length; i++) {
	        context = context[namespaces[i]];
          if (typeof context === "undefined") {
            return false;
          }
	    }
      if (context[func] instanceof Function) {
        return context[func].apply(context, args);
      } else {
        return false;
      }
	},
	
	executeOnloadFunctions : function(selector) {
		jQuery(selector + " .onload-function").each(function(){
			var functionName=jQuery(this).attr("data-function");
			var functionArgStr=jQuery(this).attr("data-arguments");
			if (functionArgStr === "") {
				// no arguments
				Utilities.executeFunctionByName(functionName, window);
			}
			else {
				var functionArg = jQuery.parseJSON(functionArgStr);
				Utilities.executeFunctionByName(functionName, window, functionArg);
			}
			// remove element so it is not executed again
			jQuery(this).remove();
		});
	}
};


var WdkAjax = {
		
	sendContactRequest : function(form, successCallback) {	
		// send request
		jQuery.post(jQuery(form).attr("action"), jQuery(form).serialize(), function(data) {
		  switch (data.status) {
		
		    case "success":
		  	  successCallback(data);
		      break;
		
		    case "error":
		      var response = "<h3>Please try to correct any errors below</h3>" +
		          "<br/>" + data.message;
		      jQuery("<div></div>").html(response).dialog({
		        title: "Oops! An error occurred.",
		        buttons: [{
		          text: "OK",
		          click: function() { jQuery(this).dialog("close"); }
		        }],
		        modal: true
		      });
		      break;
		  }
		}, "json").error(function(jqXHR, textStatus, errorThrown) {
		  var response = "<h3>A " + textStatus + " error occurred.</h3><br/>" +
		      "<p>This indicates a problem with our server. Please email " +
		      "support directly.";
		  jQuery("<div></div>").html(response).dialog({
		    title: "Oops! An error occurred.",
		    buttons: [{
		      text: "OK",
		      click: function() { jQuery(this).dialog("close"); }
		    }],
		    modal: true
		  });
		});
	}
};

var ErrorUtils = {
		
	playSadTrombone: function() {
		jQuery('body').append("<iframe width=\"0px\" height=\"0px\" src=\"http://sadtrombone.com/?play=true\"></iframe>");
	},

	submitError: function() {
		var errorForm = jQuery('#error-submission-form')[0];
		WdkAjax.sendContactRequest(errorForm, function(){
			jQuery('#open-error-thanks-link').click();
		});
	},

	toggleErrorDetails: function() {
		var jqExceptionDiv = jQuery('#exception-information');
		jqExceptionDiv.toggle();
		jQuery('#exception-details-link').html(jqExceptionDiv.is(':hidden') ?
				"Show Details" : "Hide Details");
	}
};
