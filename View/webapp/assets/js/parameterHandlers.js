var dependedParams = new Array();
var displayTermMap = new Array();

function initParamHandlers() {
	initTypeAhead();
	initDependentParamHandlers();
}

function initDependentParamHandlers() {
	$("input.dependentParam, select.dependentParam").each(function() {
		$(this).attr('disabled',true);
		var name = $(this).attr('name');
		name = name.substring(name.indexOf("myMultiProp(") + 12, name.indexOf(")"));
		if (!dependedParams[name]) {
			dependedParams[name] = $(this).attr('class');
			dependedParams[name] = dependedParams[name].substr(dependedParams[name].indexOf('dependsOn')+9)
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
		$("#" + paramName + "_display").change(function() {
			$("td#" + paramName + "aaa input[name='myMultiProp(" + paramName + ")']").val(displayTermMap[paramName][$(this).val()]);
		});
		if(!$(this).hasClass('dependentParam')) {
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
	$("#" + name + "_display").autocomplete(def,{
		matchContains: true
	});
	$("#" + name + "_display").removeAttr('disabled');
}

function updateDependentParam(paramName, dependedValue) {
	var dependentParam = $("td#" + paramName + "aaa input[name='myMultiProp(" + paramName + ")']");
	if (dependentParam.length == 0) dependentParam = $("td#" + paramName + "aaa select[name='myMultiProp(" + paramName + ")']");
	var questionName = dependentParam.closest("form").children("input:hidden[name=questionFullName]").val();
	var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&dependedValue=' + dependedValue;
	if (dependentParam.hasClass('typeAhead')) {
		var sendReqUrl = sendReqUrl + '&xml=true';
		$.ajax({
			url: sendReqUrl,
			dataType: "xml",
			success: function(data){
				dependentParam.removeAttr('disabled');
				createAutoComplete(data, paramName);
			}
		});
	} else {
		$.ajax({
			url: sendReqUrl,
			type: "POST",
			dataType: "html",
			success: function(data){
				var parentElt = $("td#" + paramName + "aaa > div");
				var newContent = $("div.param, div.param-multiPick",data);
				parentElt.html(newContent.html());
			}
		});
	}
}
