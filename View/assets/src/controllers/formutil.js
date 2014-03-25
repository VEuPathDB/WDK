wdk.util.namespace("window.wdk.formUtil", function(ns, $) {
	"use strict";
	/*
	  {
        "txt1": [
          "rdoherty@pcbi.upenn.edu"
        ],
        "sel1": [
          "val3"
        ],
        "hdn1": [
          ""
        ],
        "rad2": [
          "on"
        ],
        "sel3": [
          "val1",
          "val3"
        ],
        "sel2": [
          "val2"
        ],
        "cbx1": [
          "on"
        ],
        "pwd1": [
          "staff1"
        ],
        "texta1": [
          "blah in textarea"
        ],
        "cbx3": [
          "on"
        ]
      }
	*/
	function populateForm($form, paramValues) {
		alert('populating form!');
		
		// scroll through names of params
		for (var inputName in paramValues) {

			// input elements
			$form.find('input[name='+inputName+']').each(function(){
				var $inputElement = $(this);
				var type = $(this).attr('type');

				// if checkbox or radio and value matches, then check the box
				if ((type == 'checkbox' || type == 'radio') &&
					$.inArray($inputElement.attr('value'), paramValues[inputName]) >= 0) {
					// element should be selected
					$inputElement.prop(':checked', true);
				}
				
				// if hidden, text, or password, simply fill in the value
				else if (type == 'hidden' || type == 'text' || type == 'password'){
					// should only be one value, so take the first
					$inputElement.attr('value', paramValues[inputName][0]);
				}
			});
			
			// textarea elements
			$form.find('textarea[name='+inputName+']').each(function() {
				var $textareaElement = $(this);
				$textareaElement.html(paramValues[inputName][0]);
			});
			
			// select elements
			$form.find('select[name='+inputName+']').each(function() {
				var $selectElement = $(this);
				for (var i=0; i < paramValues[inputName].length; i++) {
					$selectElement.find('input[value='+paramValues[inputName][i]+']').prop(':selected', true);
				}
			});
		}
	}

	ns.populateForm = populateForm;
});
