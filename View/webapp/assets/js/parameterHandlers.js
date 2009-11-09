var dependedParams = new Array();
var displayTermMap = new Array();

function initParamHandlers(isPopup) {
	initTypeAhead();
	initDependentParamHandlers();
	if (!isPopup){
		$("#form_question").submit(function() {
			mapTypeAheads();
			return true;
		});
	}
}

function initDependentParamHandlers() {
	$('div.dependentParam').each(function() {
		$('input, select', this).attr('disabled',true);
		var name = $(this).attr('name');
		if (!dependedParams[name]) {
			dependedParams[name] = $(this).attr('dependson');
		}
		var dependedParam = $("td#" + dependedParams[name] + "aaa input[name='myMultiProp(" + dependedParams[name] + ")'], td#" + dependedParams[name] + "aaa select[name='myMultiProp(" + dependedParams[name] + ")']");
		dependedParam.unbind('change');
		dependedParam.change(function() {
			updateDependentParam(name, $(this).val());
		});
	});

	//Trigger the change function so dependent params are initialized correctly
	for (var name in dependedParams) {
		dependedParam =  $("td#" + dependedParams[name] + "aaa input[name='myMultiProp(" + dependedParams[name] + ")'], td#" + dependedParams[name] + "aaa select[name='myMultiProp(" + dependedParams[name] + ")']");
		dependedParam.change();
	}
}

function initTypeAhead() {
	$("input:hidden.typeAhead").each(function() {
		var questionName = $(this).closest("form").children("input:hidden[name=questionFullName]").val();
		var paramName = $(this).attr('name');
		paramName = paramName.substring(paramName.indexOf("myMultiProp(") + 12, paramName.indexOf(")"));
		$("#" + paramName + "_display").attr('disabled',true);
		if(!$(this).parent('div').hasClass('dependentParam')) {
			$("#" + paramName + "_display").val('Loading options...');
			var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&xml=true';
			$.ajax({
				url: sendReqUrl,
				dataType: "xml",
				success: function(data){
					createAutoComplete(data, paramName);
				}
			});
		}
	});
}

function createAutoComplete(obj, name) {
	$("div.ac_results").remove(); // Remove any pre-existing type-ahead results.
	var def = new Array()
	displayTermMap[name] = new Array();
	var term;
	var display;
	if( $("term",obj).length != 0 ){
		$("term",obj).each(function(){
			term = this.getAttribute('id');
			display = this.firstChild.data;
			def.push(display);
			displayTermMap[name][display] = term;
		});		
	}
	$("#" + name + "_display").unautocomplete().autocomplete(def,{
		matchContains: true
	});
	$("#" + name + "_display").val('').removeAttr('disabled');
}

function updateDependentParam(paramName, dependedValue) {
	if (dependedValue && dependedValue != 'Choose one:') {
		var dependentParam = $("td#" + paramName + "aaa > div.dependentParam[name='" + paramName + "']");
		var questionName = dependentParam.closest("form").children("input:hidden[name=questionFullName]").val();
		var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&dependedValue=' + dependedValue;
		if ($('input.typeAhead',dependentParam).length > 0) {
			var sendReqUrl = sendReqUrl + '&xml=true';
			$("#" + paramName + "_display").attr('disabled',true).val('Loading options...');
			$.ajax({
				url: sendReqUrl,
				dataType: "xml",
				success: function(data){
					$('input',dependentParam).removeAttr('disabled');
					createAutoComplete(data, paramName);
				}
			});
		} else {
			$.ajax({
				url: sendReqUrl,
				type: "POST",
				data: {},
				dataType: "html",
				success: function(data){
					var newContent = $("div.param, div.param-multiPick",data);
					dependentParam.html(newContent.html());
				}
			});
		}
	}
}

function mapTypeAheads() {
	$("input:hidden.typeAhead").each(function() {
		var paramName = $(this).attr('name');
		paramName = paramName.substring(paramName.indexOf("myMultiProp(") + 12, paramName.indexOf(")"));
		var newValue = displayTermMap[paramName][$("#" + paramName + "_display").val()];
		if (!newValue) newValue = $("#" + paramName + "_display").val();
		$(this).val(newValue);
	});
}
