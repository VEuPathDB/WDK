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
	    }
	    return context[func].apply(context, args);
	},
	
	executeOnloadFunctions : function(selector) {
		$(selector + " .onload-function").each(function(){
			var functionName=$(this).attr("data-function");
			var functionArgStr=$(this).attr("data-arguments");
			if (functionArgStr === "") {
				// no arguments
				Utilities.executeFunctionByName(functionName, window);
			}
			else {
				var functionArg = jQuery.parseJSON(functionArgStr);
				Utilities.executeFunctionByName(functionName, window, functionArg);
			}
			// remove element so it is not executed again
			$(this).remove();
		});
	}
};
