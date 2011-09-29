//CONSTANTS
var booleanClasses = "box row2 arrowgrey operation ";
var firstClasses = "box row2 arrowgrey simple";
var transformClasses = "row2 transform"; 
var operandClasses = "box row1 arrowgrey simple";

//Popup messages
var insert_popup = "Insert a new step to the left of this one, by either running a new query or choosing an existing strategy";
var delete_popup = "Delete this step from the strategy; if this step is the only step in this strategy, this will delete the strategy also";
var x_popup = "Close this popup";

	//Simple Steps
var ss_rename_popup = "Rename this search";
var ss_view_popup = "View the results of this search in the Results area below";
var ss_edit_popup = "Revise the parameters of this search and/or its combine operation";
var ss_expand_popup = "Expand this step in a new panel to add nested steps. (Use this to build a non-linear strategy)";							
	//Substrategies
var sub_edit_expand_openned = "Revise the nested strategy in the open panel below";	
var sub_rename_popup = "Rename this nested strategy";             
var sub_view_popup = "View the results of this nested strategy in the Results area below";
var sub_edit_popup = "Open this nested step to revise";
var sub_expand_popup = "Open into a new panel to add or edit nested steps";
var sub_collapse_popup = "Convert a single-step nested strategy back to a normal step";

//Variables
var has_invalid = false;

// MANAGE THE DISPLAY OF THE STRATEGY BASED ON THE ID PASSED IN
function displayModel(strat){
	if(strats){
	  $("#strat-instructions").remove();
	  $("#strat-instructions-2").remove();
	  // For IE : when instructions are shown, need to specify 'overflow : visible'
	  // Need to remove this inline style when instructions are removed
	  $("#Strategies").removeAttr("style");
	  if(strat.isDisplay){
		var div_strat = document.createElement("div");
		var div_steps_section = document.createElement("div");
		div_steps_section.setAttribute('class','diagramSection scrollableSteps');
		var div_steps = document.createElement("div");
		div_steps.setAttribute('class','stepWrapper');
		$(div_steps).css({"width":(118 * strat.Steps.length) + "px"});
		$(div_strat).attr("id","diagram_" + strat.frontId).addClass("diagram");
		var close_span = document.createElement('span');
		$(close_span).addClass("closeStrategy").html(""+
		"	<a onclick='closeStrategy(" + strat.frontId + ")' href='javascript:void(0)'>"+
		"		<img alt='Click here to close the strategy (it will only be removed from the display)' src='wdk/images/Close-X.png' title='Click here to close the strategy (it will only be removed from the display)' height='15' width='15' src='wdk/images/Close-X.png'/>"+
		"	</a>");
		$(div_strat).append(close_span);
		var stratNameMenu = createStrategyName(strat);
		$(div_strat).append(stratNameMenu[0]);
		$(div_strat).append(stratNameMenu[1]);
		$(div_strat).append(createParentStep(strat));
		displaySteps = createSteps(strat,div_steps);
		$(div_strat).append(createRecordTypeName(strat));
		button = document.createElement('a');
		lsn = strat.getStep(strat.Steps.length,true).back_boolean_Id;
		if(lsn == "" || lsn == null)
			lsn = strat.getStep(strat.Steps.length, true).back_step_Id;	
		dType = strat.dataType;
		$(button).attr("id","filter_link").attr("href","javascript:openFilter('" + dType + "'," + strat.frontId + "," + lsn + ",true)").attr("onclick","this.blur()").addClass("filter_link redbutton").attr("title","CLICK to run a new query and combine its result with your current result.     Alternatively, you could obtain the orthologs to your current result or run another available transform.").html("<span>Add Step</span>");
		buttonDiv = document.createElement('div');
		buttonDiv.setAttribute('class','diagramSection row2');
		$(buttonDiv).append(button);
		$(div_steps_section).append(div_steps);
		$(div_strat).append(div_steps_section);
		$(div_strat).append(buttonDiv);
		if (has_invalid) {
			$(div_strat).append(createInvalidText());
		}
			has_invalid = false;
	    return div_strat;
	  }
    }
    return null;
}


// HANDLES THE CREATION OF THE STEP BOX -- This function could be broken down to smaller bites based on the type of step -- future work
function createSteps(strat,div_strat){
	var zIndex = 80;
	var stepdiv;
	for(var ind=0; ind < strat.Steps.length; ind++){  //cStp in strat.Steps){
		cStp = strat.getStep(ind+1,true);
		jsonStep = strat.JSON.steps[cStp.frontId];
		prevJsonStep = (ind == 0) ? null : strat.JSON.steps[strat.getStep(ind,true).frontId];
		if(cStp.isboolean || cStp.isSpan){	
			//Create the two layered Boolean Steps
			stepdiv = multiStep(cStp, prevJsonStep, jsonStep, strat.frontId);
		}else{
			//Create Single Layered Steps like First Step or Transforms
			stepdiv = singleStep(cStp, prevJsonStep, jsonStep,strat.frontId);
		}
		$(stepdiv).css({'z-index' : zIndex});
		$(div_strat).append(stepdiv);
		zIndex--; // DO NOT DELETE, needed for correct display in IE7.
	}
}

//Creates the boolean Step and the operand step displayed above it
function multiStep(modelstep, prevjsonstep, jsonstep, sid){
	// Create the boolean venn diagram box
	var filterImg = "";
	var bool_link = "";
	var details_link = "showDetails(this)";
	if(modelstep.isSpan) {
		details_link = "void(0)";
		jsonstep.operation = "SPAN " + getSpanOperation(jsonstep.params);
	}
	if(jsonstep.isValid) bool_link = "NewResults(" + sid + "," + modelstep.frontId + ", true)";
	if(jsonstep.filtered) filterImg = "<span class='filterImg'><img src='wdk/images/filter.gif' height='10px' width='10px'/></span>";
	boolinner = ""+
		"			<a id='" + sid + "|" + modelstep.back_boolean_Id + "|" + jsonstep.operation + "' title='CLICK to modify this operation.' class='operation' href='javascript:void(0)' onclick='showDetails(this)'>"+
		"				<img src='wdk/images/transparent1.gif'>"+
		"			</a>"+
		"			<div class='crumb_details'></div>"+
		"			<h6 class='resultCount'>"+
		"				<a title='CLICK to show these results in the area below.' class='operation' onclick='" + bool_link + "' href='javascript:void(0)'>" + jsonstep.results + "&nbsp;" + getDisplayType(jsonstep.shortDisplayType, jsonstep.results) + "</a>"+
		"			</h6>" + filterImg;
		if(!modelstep.isLast){
			if(modelstep.nextStepType == "transform"){
				boolinner = boolinner + 
				"<div class='arrow right size3'></div>";
			}else{
				boolinner = boolinner + 
				"<div class='arrow right size2'></div>";
			}
		}
	boolDiv = document.createElement('div');
	$(boolDiv).attr("id","step_" + modelstep.frontId).addClass(booleanClasses + jsonstep.operation).html(boolinner);

	$(".crumb_details", boolDiv).replaceWith(createDetails(modelstep, prevjsonstep, jsonstep, sid));
	stepNumber = document.createElement('span');
	$(stepNumber).addClass('stepNumber').text("Step " + modelstep.frontId);
	//Create the operand Step Box
	childStp = jsonstep.step;	
	uname = "";
	fullName = "";
	if(childStp.name == childStp.customName){
		uname = childStp.shortName;
		fullName = childStp.name;
	}else{
		uname = (childStp.customName.length > 15)?childStp.customName.substring(0,12) + "...":childStp.customName; 
		fullName = childStp.customName;
	}
	var childfilterImg = "";
	if(childStp.filtered)
		childfilterImg = "<span class='filterImg'><img src='wdk/images/filter.gif' height='10px' width='10px'/></span>";
	childinner = ""+
		"		<h4>"+
		"			<a style='text-decoration:underline' title='CLICK to make changes to this step and/or how it is combined with the previous step' id='stepId_" + modelstep.frontId + "' class='crumb_name' onclick='showDetails(this)' href='javascript:void(0)'>"+
		"				<span id='name'>"+uname+"</span>"+
		"				<img src='wdk/images/plus.gif' width='13' height='13'/>"+
		"				<span class='collapsible' style='display: none;'>false</span>"+
		"			</a>"+
		"			<span id='fullStepName' style='display: none;'>" + fullName + "</span>"+
		"			<div class='crumb_details'></div>"+
		"		</h4>"+
		"		<h6 class='resultCount'><a title='CLICK to show these results in the area below.' class='results_link' href='javascript:void(0)' onclick='NewResults(" + sid + "," + modelstep.frontId + ", false)'> " + childStp.results + "&nbsp;" + getDisplayType(childStp.shortDisplayType, childStp.results) + "</a></h6>"+
		childfilterImg +
		"<img class='arrow down' src='wdk/images/arrow_chain_down2.png' alt='equals'>";

	var child_invalid = null;
	if(!childStp.isValid){
		child_invalid = createInvalidDiv();
		$(child_invalid).attr("id",sid+"_"+modelstep.frontId);
		$("img", child_invalid).click(function(){
			var iv_id = $(this).parent().attr("id").split("_");
			$("div#diagram_" + iv_id[0] + " div#step_" + iv_id[1] + "_sub div.crumb_menu a.edit_step_link").click();
		});
	}
	
	childDiv = document.createElement('div');
	if(child_invalid != null){
		childDiv.appendChild(child_invalid);
	}
	$(childDiv).attr("id","step_" + modelstep.frontId + "_sub").addClass(operandClasses).append(childinner);
	$(".crumb_details", childDiv).replaceWith(createDetails(modelstep, prevjsonstep, childStp, sid));
	
	// Create the background div for a collapsed step if step is expanded
	var bkgdDiv = null;
	if(childStp.isCollapsed){
		var ss_name = childStp.strategy.name.length > 15 ? childStp.strategy.name.substring(0,12) + "...":childStp.strategy.name; 
		$(".crumb_name span#name", childDiv).text(ss_name);
		$("span#fullStepName", childDiv).text(childStp.strategy.name);
		bkgdDiv = document.createElement("div");
		$(bkgdDiv).addClass("expandedStep");
	}
	var stepbox = document.createElement('div');
	stepbox.setAttribute('class', 'stepBox');
	if(bkgdDiv != null)
		stepbox.appendChild(bkgdDiv);
	stepbox.appendChild(childDiv);
	stepbox.appendChild(boolDiv);
	stepbox.appendChild(stepNumber);
	return stepbox;
}

//Creates all steps that are on the bottom line only ie. this first step and transform steps
function singleStep(modelstep, prevjsonstep, jsonstep, sid){
	uname = "";
	fullName = "";
	if(jsonstep.name == jsonstep.customName){
		uname = jsonstep.shortName;
		fullName = jsonstep.name;
	}else{
		uname = (jsonstep.customName.length > 15)?jsonstep.customName.substring(0,12) + "...":jsonstep.customName; 
		fullName = jsonstep.customName;
	}
	var filterImg = "";
	if(jsonstep.filtered)
		filterImg = "<span class='filterImg'><img src='wdk/images/filter.gif' height='10px' width='10px'/></span>";
	inner = ""+
		"		<h4>"+
		"			<a style='text-decoration:underline' title='CLICK to make changes to this step.' id='stepId_" + modelstep.frontId + "' class='crumb_name' onclick='showDetails(this)' href='javascript:void(0)'>"+
		"				<span id='name'>"+uname+"</span>"+
		"				<img src='wdk/images/plus.gif' width='13' height='13'/>"+
		"				<span class='collapsible' style='display: none;'>false</span>"+
		"			</a>"+ 
		"			<span id='fullStepName' style='display: none;'>" + fullName + "</span>"+
		"			<div class='crumb_details'></div>"+
		"		</h4>"+
		"		<h6 class='resultCount'><a title='CLICK to show these results in the area below.' class='results_link' href='javascript:void(0)' onclick='NewResults(" + sid + "," + modelstep.frontId + ", false)'> " + jsonstep.results + "&nbsp;" + getDisplayType(jsonstep.shortDisplayType,jsonstep.results) + "</a></h6>"+
		 filterImg;
	if(!modelstep.isLast){
		if(modelstep.isTransform){
			if(modelstep.nextStepType == "transform"){
				inner = inner + 
					"<div class='arrow right size5'></div>";
			} else {
				inner = inner + 
					"<div class='arrow right size4'></div>";
			}

		}
		else{
			if(modelstep.nextStepType == "transform"){
				inner = inner + 
					"<div class='arrow right size5'></div>";
			} else {
				inner = inner + 
					"<div class='arrow right size1'></div>";
			}
		}
	}
	var step_invalid = null;
	if(!modelstep.isTransform && !jsonstep.isValid){
		step_invalid = createInvalidDiv();
		$(step_invalid).attr("id",sid+"_"+modelstep.frontId);
	}
	singleDiv = document.createElement('div');
	if(step_invalid != null){
		singleDiv.appendChild(step_invalid);
	}
	$(singleDiv).attr("id","step_" + modelstep.frontId + "_sub").append(inner);
	stepNumber = document.createElement('span');
	$(stepNumber).addClass('stepNumber').text("Step " + modelstep.frontId);
	if(modelstep.isTransform){
		$(singleDiv).addClass(transformClasses);
	}else{
		$(singleDiv).addClass(firstClasses);
	}
	$(".crumb_details", singleDiv).replaceWith(createDetails(modelstep, prevjsonstep, jsonstep, sid));
	var stepbox = document.createElement('div');
	stepbox.setAttribute('class', 'stepBox');
	stepbox.appendChild(singleDiv);
	stepbox.appendChild(stepNumber);
	return stepbox;	
}

//HANDLE THE CREATION OF THE STEP DETAILS BOX
function createDetails(modelstep, prevjsonstep, jsonstep, sid){
	strat = getStrategy(sid);
	var detail_div = document.createElement('div');
	$(detail_div).addClass("crumb_details").attr("disp","0");
	$(detail_div).attr("style","text-align:center");
	if (jsonstep.isboolean && !jsonstep.isCollapsed) $(detail_div).addClass("operation_details");
	var name = jsonstep.customName;
	var questionName = jsonstep.questionName;
	
	var filteredName = "";
	if(jsonstep.filtered){
		filteredName = "<span class='medium'><b>Applied Filter:&nbsp;</b>" + jsonstep.filterName + "</span><hr>";
	}
	if(jsonstep.isCollapsed){
		name = jsonstep.strategy.name;
	} else if (jsonstep.isboolean){
		if (jsonstep.step.isCollapsed) {
			name = jsonstep.step.strategy.name;
		} else {
			name = jsonstep.step.customName;
		}
	}
	var collapsedName = escape(name);//"Nested " + name;

	if (jsonstep.isboolean && !jsonstep.isCollapsed){
		name = "<ul class='question_name'><li>STEP " + modelstep.frontId + " : Step " + (modelstep.frontId - 1) + "</li><li class='operation " + jsonstep.operation + "'></li><li>" + name + "</li></ul>";
	} else {
		name = "<p class='question_name'><span>STEP " + modelstep.frontId + " : " + name + "</span></p>";
	}

	var parentid = modelstep.back_step_Id;
	if(modelstep.back_boolean_Id != null && modelstep.back_boolean_Id.length != 0){
		parentid = modelstep.back_boolean_Id;
	}
	
	var params = jsonstep.params;
	var params_table = "";
	if(jsonstep.isboolean && !jsonstep.isCollapsed){
		// var url = "processFilter.do?strategy=" + strat.backId + "&revise=" + modelstep.back_boolean_Id + "&checksum=" + strat.checksum;
		// var oform = "<form id='form_question' class='clear' enctype='multipart/form-data' action='javascript:validateAndCall(\"edit\",\""+ url + "\", \"" + strat.backId + "\");' method='post' name='questionForm'>";
                var url = "wizard.do?action=revise&step=" + modelstep.back_boolean_Id + "&";
		var oform = "<form id='form_question' class='clear' enctype='multipart/form-data' "
                    + " action='wizard.do/' method='post' name='questionForm' "
                    + " onsubmit='callWizard(\"" + url + "\",this,null,null,\"submit\", " + strat.frontId + ");hideDetails(this);'>";
		var cform = "</form>";
		var stage_input = "<input type='hidden' id='stage' value='process_boolean'/>";
		var params_table = "<div class='filter operators'><span class='form_subtitle' style='padding-right:20px'><b>Revise Operation</b></span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck' valign='middle'><input type='radio' name='boolean' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td valign='middle'>&nbsp;" + (parseInt(modelstep.frontId)-1) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (modelstep.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='boolean' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (parseInt(modelstep.frontId)-1) + "&nbsp;<b>UNION</b>&nbsp;" + (modelstep.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='boolean' value='MINUS'></td><td class='operation MINUS'></td><td>&nbsp;" + (parseInt(modelstep.frontId)-1) + "&nbsp;<b>MINUS</b>&nbsp;" + (modelstep.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='boolean' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (modelstep.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(modelstep.frontId)-1) + "</td></tr></table></div></div>"
		var button = "<div style='text-align:center'><input type='submit' value='Revise' /></div>";
		params_table = oform + stage_input + params_table + button + cform;
        } else if (jsonstep.isCollapsed) {
                params_table = "<div>The nested strategy is opened below.</div>";
	}else if(params != undefined && params.length != 0) {
		params_table = createParameters(params, modelstep.isSpan && jsonstep.id == modelstep.back_boolean_Id);
        }
	var hideOp = false;
	var hideQu = false;
	if(jsonstep.isCollapsed){                              /* substrategy */

		rename_step = 	"<a title='" + sub_rename_popup + "' class='rename_step_link' href='javascript:void(0)' onclick='Rename_Step(this, " + sid + "," + modelstep.frontId + ");hideDetails(this)'>Rename</a>&nbsp;|&nbsp;";

		view_step = 	"<a title='" + sub_view_popup + "' class='view_step_link' onclick='NewResults(" + sid + "," + modelstep.frontId + ");hideDetails(this)' href='javascript:void(0)'>View</a>&nbsp;|&nbsp;";

	    disab = "";
		ocExp = "onclick='ExpandStep(this," + sid + "," + modelstep.frontId + ",\"" + collapsedName + "\");hideDetails(this)'";
		oM = "Show Nested Strategy";
		moExp = sub_expand_popup;
		moEdit = sub_edit_popup;
		if(jsonstep.strategy.order > 0){
			disab = "disabled";
			ocExp = "";
			oM = "Already Open Below...";
			moExp = sub_edit_expand_openned;
			moEdit = sub_edit_expand_openned;
		}
		
		
		collapseDisabled = "";
		ocCol = "onclick='ExpandStep(this," + sid + "," + modelstep.frontId + ",\"" + collapsedName + "\", true);hideDetails(this)'";
		if (!jsonstep.isUncollapsible) {
		    collapseDisabled = "disabled";
		    ocCol = "";
		}
		
		edit_step = 	"<a title='" + moEdit + "' class='edit_step_link " + disab + "' href='javascript:void(0)' " + ocExp + ">Revise</a>&nbsp;|&nbsp;";
		
		collapse_step = "<a title='" + sub_collapse_popup + "' class='collapse_step_link " + collapseDisabled + "' href='javascript:void(0)' " + ocCol + ">Unnest Strategy</a>&nbsp;|&nbsp;";
		
		expand_step = "<a title='" + moExp + "' class='expand_step_link " + disab + "' href='javascript:void(0)' " + ocExp + ">" + oM + "</a>&nbsp;|&nbsp;";

	}else{   							/* simple step */
		disab = "";
		if (jsonstep.isboolean){
			rename_step = 	"<a title='" + ss_rename_popup + "' class='rename_step_link disabled' href='javascript:void(0)'>Rename</a>&nbsp;|&nbsp;";
		} else{
			rename_step = 	"<a title='" + ss_rename_popup + "' class='rename_step_link' href='javascript:void(0)' onclick='Rename_Step(this, " + sid + "," + modelstep.frontId + ");hideDetails(this)'>Rename</a>&nbsp;|&nbsp;";
		}

		view_step = 	"<a title='" + ss_view_popup + "' class='view_step_link' onclick='NewResults(" + sid + "," + modelstep.frontId + "," + jsonstep.isboolean + ");hideDetails(this)' href='javascript:void(0)'>View</a>&nbsp;|&nbsp;";

		if(modelstep.isTransform || modelstep.frontId == 1){
			hideOp = true;
		}
		if(jsonstep.isboolean) {
			hideQu = true;
			disab = "disabled";
		}
		parms = jsonstep.urlParams;

		edit_step =	"<a title='" + ss_edit_popup + "'  class='edit_step_link " + disab 
                    + "' href='javascript:void(0)' onclick='Edit_Step(this,\"" + questionName 
                    + "\",\"" + parms + "\"," + hideQu + "," + hideOp + "," + jsonstep.assignedWeight + ");hideDetails(this)' id='" + sid + "|" + jsonstep.id + "|" + modelstep.operation + "'>Revise</a>&nbsp;|&nbsp;";

		if(modelstep.frontId == 1 || modelstep.isTransform || jsonstep.isboolean){
			expand_step = 	"<a title='" + ss_expand_popup + "' class='expand_step_link disabled' href='javascript:void(0)'>Make Nested Strategy</a>&nbsp;|&nbsp;";
		}else{
			expand_step = 	"<a title='" + ss_expand_popup + "' class='expand_step_link' href='javascript:void(0)' onclick='ExpandStep(this," + sid + "," + modelstep.frontId + ",\"" + collapsedName + "\");hideDetails(this)'>Make Nested Strategy</a>&nbsp;|&nbsp;";
		}
		
		collapse_step = "";
	}
	insertRecName = (prevjsonstep == null) ? jsonstep.dataType : prevjsonstep.dataType;			
	insert_step = 	"<a title='" + insert_popup + "'  class='insert_step_link' id='" + sid + "|" + parentid + "' href='javascript:void(0)' onclick='Insert_Step(this,\"" + insertRecName + "\");hideDetails(this)'>Insert Step Before</a>&nbsp;|&nbsp;";
	var customMenu = "";

// this code (function in html/assets/js/customStrategy.js)  adds the ortholog link 
	try {
		customMenu = customCreateDetails(jsonstep, modelstep);
	}
	catch(err) {
		// Do nothing?
	}

	var delete_strat = '';
	if(modelstep.frontId == 1 && strat.nonTransformLength == 1){
		delete_step = "<a title='" + delete_popup + "' class='delete_step_link' href='javascript:void(0)' onclick=\"deleteStrategy('" + strat.backId + "',false);hideDetails(this)\">Delete</a>";
	} else {
		delete_step = "<a title='" + delete_popup + "' class='delete_step_link' href='javascript:void(0)' onclick='DeleteStep(" + sid + "," + modelstep.frontId + ");hideDetails(this)'>Delete</a>";
	}

	close_button = 	"<a title='" + x_popup + "' class='close_link' href='javascript:void(0)' onclick='hideDetails(this)'><img src=\"wdk/images/close.gif\" /></a>";

	inner = ""+	
	    "		<div class='crumb_menu'>" + close_button + rename_step + view_step + edit_step + expand_step + collapse_step + insert_step + customMenu + delete_step +
		"		</div>"+ name +
		"		<table></table><hr class='clear' />" + filteredName +
		"		<p><b>Results:&nbsp;</b>" + jsonstep.results + "&nbsp;" + getDisplayType(jsonstep.shortDisplayType,jsonstep.results);// + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='downloadStep.do?step_id=" + modelstep.back_step_Id + "'>Download</a>";
       
    inner += "<hr class='clear' />" + createWeightSection(jsonstep,modelstep,sid);

	$(detail_div).html(inner);
	if (!jsonstep.isValid)
	    $(".crumb_menu a:not(.edit_step_link,.delete_step_link,.close_link)", detail_div).removeAttr('onclick').addClass('disabled');
	$("table", detail_div).replaceWith(params_table);
	return detail_div;       
}

function createWeightSection(jsonstep,modelstep,sid){
	// display & assign weight
if(!jsonstep.useweights || modelstep.isTransform || jsonstep.isboolean || modelstep.isSpan) return "";
	
var set_weight = "<div name='All_weighting' class='param-group' type='ShowHide'>"+
					"<div class='group-title'> "+
	    				"<img style='position:relative;top:5px;'  class='group-handle' src='wdk/images/plus.gif' onclick=''/>"+
	    				"Give this search a weight"+
					"</div>"+
					"<div class='group-detail' style='display:none;text-align:center'>"+
	    				"<div class='group-description'>"+
							"<p><input type='text' name='weight' value='" + jsonstep.assignedWeight + "'>  </p> "+
							"<input type='button' value='Assign' onclick='SetWeight(this, " + sid + "," + modelstep.frontId + ");hideDetails(this)' >"+
							"<p>Optionally give this search a 'weight' (for example 10, 200, -50).<br>In a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches.</p>"+
						"</div>"+
						"<br>"+
					"</div>"+
				"</div>";
	return set_weight;
}

// HANDLE THE DISPLAY OF THE PARAMETERS IN THE STEP DETAILS BOX
function createParameters(params, isSpan){
	var table = document.createElement('table');
	if (isSpan) {
		// TODO:  if span logic moves into WDK, the code
		// for the span logic details box should move here.
		try {	//params includes the sentence with formatting and all
			var contents = customSpanParameters(params);
			$(table).append(contents);
		}
		catch(err) {}
	}
	else {
		$(params).each(function(){
        	if (this.visible) {
			var tr = document.createElement('tr');
			var prompt = document.createElement('td');
			var space = document.createElement('td');
			var value = document.createElement('td');
			$(prompt).addClass("medium param name");
			$(prompt).html("<b><i>" + this.prompt + "</i></b>");
			$(space).addClass("medium param");
			$(space).html("&nbsp;:&nbsp;");
			$(value).addClass("medium param value");
			$(value).html( this.value );
			$(tr).append(prompt);
			$(tr).append(space);
			$(tr).append(value);
			$(table).append(tr);
        	}
		});
	}
	return table;
}

// HANDLE THE DISPLAY OF THE STRATEGY RECORD TYPE DIV
function createRecordTypeName(strat){
	if (strat.subStratOf == null){
		var div_sn = document.createElement("div");
		$(div_sn).attr("id","record_name").addClass("strategy_small_text").text("(" + getDisplayType(strat.displayType) + ")"   );
		return div_sn;
   	}
}

function createParentStep(strat){
	var parentStep = null;
	var pstp = document.createElement('div');
	if(strat.subStratOf != null)
		parentStep = strat.findParentStep(strat.backId.split("_")[1],false);
	if(parentStep == null){
		return;
	}else{
		$(pstp).attr("id","record_name");
		$(pstp).append("Expanded View of Step <i>" + strat.JSON.name + "</i>");
		return pstp;
	}
}

// HANDLE THE DISPLAY OF THE STRATEGY NAME DIV
function createStrategyName(strat){
	var json = strat.JSON;
	var id = strat.backId;
	var name = json.name;
	var append = '';
	if (!json.saved) append = "<span class='append'>*</span>";
	var exportURL = exportBaseURL + json.importId;
	var share = "";

	if(json.saved){
		share = "<a id='share_" + id + "' title='Email this URL to your best friend.' href=\"javascript:void(0)\" onclick=\"showExportLink('" + id + "')\"><b style='font-size:120%'>Share</b></a>"+
		"<div class='modal_div export_link' id='export_link_div_" + id + "'>" +
	        "<div class='dragHandle'>" +
		"<div class='modal_name'>"+
		"<span class='h3left'>Copy and paste URL below to email</span>" + 
		"</div>"+ 
		"<a class='close_window' href='javascript:closeModal()'>"+
		"<img alt='Close' src='wdk/images/Close-X.png' height='16' />" +
		"</a>"+
		"</div>"+
		"<input type='text' size=" + (exportURL.length-6) + " value=" + exportURL + " readonly='true' />" +
		"</div>";
	}else if(guestUser == 'true'){
		share = "<a id='share_" + id + "' title='Please LOGIN so you can SAVE and then SHARE (email) your strategy.' href='javascript:void(0)' onclick=\"popLogin();setFrontAction('share'," + id + ");\"><b style='font-size:120%'>Share</b></a>";
	}else{
		share = "<a id='share_" + id + "' title='SAVE this strategy so you can SHARE it (email its URL).' href='javascript:void(0)' onclick=\"showSaveForm('" + id + "', true,true)\"><b style='font-size:120%'>Share</b></a>";
	}



	var save = "";
	var sTitle = "Save As";
	// if(json.saved) sTitle = "COPY AS";
	if (guestUser == 'true') {
		save = "<a id='save_" + id + "' title='Please LOGIN so you can SAVE (make a snapshot) your strategy.' class='save_strat_link' href='javascript:void(0)' onclick=\"popLogin();setFrontAction('save'," + id + ");\"><b style='font-size:120%'>" + sTitle + "</b></a>";
	}
	else {
		save = "<a id='save_" + id + "' title='A saved strategy is like a snapshot, it cannot be changed.' class='save_strat_link' href='javascript:void(0)' onclick=\"showSaveForm('" + id + "', true)\"><b style='font-size:120%'>" + sTitle + "</b></a>";
	}
	save += "<div id='save_strat_div_" + id + "' class='modal_div save_strat'>" +
		"<div class='dragHandle'>" +
		"<div class='modal_name'>"+
		"<span class='h3left'>" + sTitle + "</span>" + 
		"</div>"+ 
		"<a class='close_window' href='javascript:closeModal()'>"+
		"<img alt='Close' src='wdk/images/Close-X.png' height='16' />" +
		"</a>"+
		"</div>"+
		"<form onsubmit='return validateSaveForm(this);' action=\"javascript:saveStrategy('" + id + "', true)\">"+
		"<input type='hidden' value='" + id + "' name='strategy'/>"+
		"<input type='text' value='' name='name'/>"+
		"<input style='margin-left:5px;' type='submit' value='Save'/>"+
		"</form>"+
		"</div>";
        var copy = "<a title='Create a copy of the strategy.' class='copy_strat_link'" +
                   " href='javascript:void(0)' onclick=\"copyStrategy('" + id + "')\">" +
                   "<b style='font-size:120%'>Copy</b></a>";

var rename = "<a id='rename_" + strat.frontId + "' href='javascript:void(0)' title='Click to rename.'  onclick=\"showSaveForm('" + id + "', false)\"><b style='font-size:120%'>Rename</b></a>";

var deleteStrat = "<a id='delete_" + strat.frontId + "' href='javascript:void(0)' title='Click to delete.'  onclick=\"deleteStrategy('" + id + "', false)\"><b style='font-size:120%'>Delete</b></a>";

	var div_sn = document.createElement("div");
	var div_sm = document.createElement("div");
	$(div_sn).attr("class","strategy_name");
	$(div_sm).attr("class","strategy_menu");
	if (strat.subStratOf == null){
		$(div_sn).html("<span style='font-size:14px;font-weight:bold' title='Name of this strategy. The (*) indicates this strategy is NOT saved.'>" + name + "</span>" + append + "<span id='strategy_id_span' style='display: none;'>" + id + "</span>");
		$(div_sm).html("<span class='strategy_small_text'>" +
	"<br/>" + 
	rename +
	"<br/>" +
        copy + 
        "<br/>" +
	save +
	"<br/>"+
	share +
	"<br/>"+
	deleteStrat +
	"</span>");
	}//else{
		//$(div_sn).html("<span style='font-size:14px;font-weight:bold' title='Name of this substrategy. To rename, click on the corresponding step name in the parent strategy'>" + name + "</span>" + "<span id='strategy_id_span' style='display: none;'>" + id + "</span>"); 
	//}
	$(div_sm).css({'z-index' : 90}); // DO NOT DELETE, needed for IE7
	return [div_sn, div_sm];
}

//REMOVE ALL OF THE SUBSTRATEGIES OF A GIVEN STRATEGY FROM THE DISPLAY
function removeStrategyDivs(stratId){
	strategy = getStrategyFromBackId(stratId);
	if(strategy != null && strategy.subStratOf != null){  //stratId.indexOf("_") > 0){
		var currentDiv = $("#Strategies div#diagram_" + strategy.frontId).remove();
		sub = getStrategyFromBackId(stratId.split("_")[0]);  //substring(0,stratId.indexOf("_")));
		//$("#Strategies div#diagram_" + sub.frontId).remove();
		subs = getSubStrategies(sub.frontId);
		for(i=0;i<subs.length;i++){
			$("#Strategies div#diagram_" + subs[i].frontId).remove();
		}
	}
}


// DISPLAY UTILITY FUNCTIONS

offset = function(modelstep){
	if(modelstep == null){
		return leftOffset + 123;
	}
	if((modelstep.isboolean || modelstep.isSpan) && (modelstep.prevStepType == "boolean" || modelstep.prevStepType == "span")){
		leftOffset += b2b;
	}else if((modelstep.isboolean || modelstep.isSpan) && modelstep.prevStepType == "transform"){
		leftOffset += b2t;
	}else if(modelstep.isTransform && (modelstep.prevStepType == "boolean" || modelstep.prevStepType == "span")){
		leftOffset += t2b;
	}else if(modelstep.isTransform && modelstep.prevStepType == "transform"){
		leftOffset += t2t;
	}else if(modelstep.isboolean || modelstep.isSpan){
		leftOffset += f2b;
	}else if(modelstep.isTransform){
		leftOffset += f2t;
	}
	return leftOffset;
}

function createInvalidDiv(){
	var inval = document.createElement('div');
	has_invalid = true;
	inval.setAttribute("class","invalidStep");
	var i = document.createElement('img');
	$(i).attr("src","wdk/images/InvalidStep.png").
	     attr("height","36").
		 attr("width","98");
	$(i).attr("onClick","reviseInvalidSteps(this)");
	$(inval).append(i);
	return inval;
}

function createInvalidText(){
	var t = document.createElement('div');
	$(t).attr("id","invalid-step-text").attr('class','simple');
	$.ajax({
		url:"wdk/jsp/InvalidText.html",
		dataType: "html",
		type:"get",
		async:false,
		success:function(data){
			$(t).html(data); 
		}
	});
	return t;
}

function closeInvalidText(ele){
	$(ele).parent().remove();
}

function reviseInvalidSteps(ele){
	var iv_id = $(ele).parent().attr("id").split("_");
	$("div#diagram_" + iv_id[0] + " div#step_" + iv_id[1] + "_sub h4 a#stepId_" + iv_id[1]).click();
	$("div#diagram_" + iv_id[0] + " div#invalid-step-text").remove();
}
function getSpanOperation(params) {
	var op = '';
	$(params).each(function() {
		if (this.name == 'span_operation') {
			op = this.internal;
		}
	});
	return op;
}
