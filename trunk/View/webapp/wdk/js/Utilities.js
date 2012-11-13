//
// Static library of utility functions
//
var Utilities = {

	// Credit to Jason Bunting and Alex Nazarov for this helpful function
	// See: http://stackoverflow.com/questions/359788/how-to-execute-a-javascript-function-when-i-have-its-name-as-a-string
	executeFunctionByName : function(functionName, ns, context /*, args... */) {
	    var args = Array.prototype.slice.call(arguments, 3);
	    var namespaces = functionName.split(".");
	    var func = namespaces.pop();
	    for (var i = 0; i < namespaces.length; i++) {
	        ns = ns[namespaces[i]];
          if (typeof ns === "undefined") {
            return false;
          }
	    }
      if (ns[func] instanceof Function && context instanceof Object) {
        return ns[func].apply(context, args);
      } else {
        return false;
      }
	},
	
	executeOnloadFunctions : function(selector) {
		jQuery(selector).find(".onload-function").each(function(){
      var $this = jQuery(this),
          data = $this.data(),
          // name of function to execute, with namespace if necessary
			    functionName = data["function"],
          // arguments to provide
			    functionArg = data["arguments"],
          // determine of function has already been invoked
          invoked = data["invoked"];
      if (invoked) return;
      // call function using window as root namespace, and
      // provide 'this' as context for function call.
      // This can be useful for jQuery function calls (e.g., $.fn.dataTable)
      Utilities.executeFunctionByName(functionName, window, this, functionArg);
			// remove element so it is not executed again
			$this.data("invoked", true);
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
