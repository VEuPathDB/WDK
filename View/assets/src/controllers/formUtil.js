wdk.util.namespace("window.wdk.formUtil", function(ns, $) {
	"use strict";
	
	function populateForm($form, paramValues) {
		
		// scroll through names of params
		for (var inputName in paramValues) {

			// input elements
			$form.find('input[name='+inputName+']').each(function(){
				var $inputElement = $(this);
				var type = $(this).attr('type');

				// if checkbox or radio and value matches, then check the box
				if ((type == 'checkbox' || type == 'radio') &&
					($.inArray($inputElement.val(), paramValues[inputName]) >= 0)) {
					// element should be selected
					$inputElement.prop('checked', true);
				}
				
				// if hidden, text, or password, simply fill in the value
				else if (type == 'hidden' || type == 'text' || type == 'password'){
					// should only be one value, so take the first
					$inputElement.val(paramValues[inputName][0]);
				}
			});
			
			// textarea elements
			$form.find('textarea[name='+inputName+']').each(function() {
				$(this).text(paramValues[inputName][0]);
			});
			
			// select elements
			$form.find('select[name='+inputName+']').each(function() {
				$(this).val(paramValues[inputName]);
			});
		}
	}

	ns.populateForm = populateForm;
});
