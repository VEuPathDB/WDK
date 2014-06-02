wdk.util.namespace("window.wdk.formUtil", function(ns, $) {
	"use strict";

	// find input elements in form based on type, then assign values/properties
	//   as necessary based on values in passed params object
	function populateForm($form, paramValues) {
		
		// handle radio/checkboxes
		$form.find('input[type=checkbox],input[type=radio]').each(function(){
			var $inputElement = $(this);
			var valueArray = paramValues[$inputElement.attr('name')];
			// uncheck box/radio if no array present or value is not in array
			var checked = !(typeof valueArray === 'undefined' || $.inArray($inputElement.val(), valueArray) < 0);
			$inputElement.prop('checked', checked);
		});
		
		// handle select boxes
		$form.find('select').each(function(){
			$(this).val(paramValues[$(this).attr('name')]);
		});
		
		// handle hidden, text, or password
		$form.find('input[type=hidden],input[type=text],input[type=password]').each(function(){
			$(this).val(paramValues[$(this).attr('name')][0]);
		});
		
		// handle textareas
		$form.find('textarea').each(function() {
			$(this).text(paramValues[$(this).attr('name')][0]);
		});
		
		// handle checkbox trees
		$form.find('.checkbox-tree').each(function() {
			var treeId = $(this).attr("id");
			handleCheckboxTree(treeId, paramValues);
		});
	}

	// Often this code will be called after a form is newly added to a page
	//   (e.g. via AJAX); make sure tree is configured before trying to set values
	function handleCheckboxTree(treeId, paramValues) {
		if (wdk.checkboxTree.isConfigured(treeId)) {
			var inputName = wdk.checkboxTree.getInputName(treeId);
			var values = paramValues[inputName]; // array of values
			if (values == null) {
				// no values were previously selected; send empty array
				values = [ ];
			}
			wdk.checkboxTree.selectListOfNodes(treeId, values);
		}
		else {
			// wait, then try again
			setTimeout(function() { handleCheckboxTree(treeId, paramValues); }, 10);
		}
	}

	ns.populateForm = populateForm;
});
