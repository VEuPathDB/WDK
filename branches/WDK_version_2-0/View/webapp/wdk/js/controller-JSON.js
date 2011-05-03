var strats = new Object();
var xmldoc = null;
var exportBaseURL;
var sidIndex = 0;
var recordType= new Array();   //stratid, recordType which is the type of the last step
var state = null;
var p_state = null;
var ajaxTimeout = 180000;
$(document).ready(function(){
	// Make the strategies window resizable
	$(".resizable-wrapper").resizable({handles: 's', minHeight: 150, stop: function(event, ui) {setCurrentTabCookie('strategyWindow',$(".resizable-wrapper").height())}});
	// tell jQuery not to cache ajax requests.
    // generic error handling of ajax calls
	$.ajaxSetup ({ 
		cache: false,
		timeout: ajaxTimeout,
		error: function(data, msg, e){
			if(msg == "timeout"){
				var c = confirm("This request has timed out.\nWould you like to try again? (This request will timeout after " +  ((this.timeout / 60000) + 1) +" minutes.)");
				if(c){
					this.timeout = this.timeout + 60000;
					$.ajax(this);
				}else{
					if(url.indexOf("showSummary.do") != -1){
						removeLoading();
					}else{
						initDisplay();
					}
				}
			}else if(data.readyState != 0 && data.status != 0) {  // not timeout, backend throws errors
				
				try {
					customShowError();
				}
				catch(e) {
					alert("An error occurred.");
				}
				if(url.indexOf("showSummary.do") != -1){
					removeLoading();
				}else{
					initDisplay();
				}
			}
		}
	});
	initStrategyPanels();
	var current = getCurrentTabCookie('application');
	if (!current || current == null)
		showPanel('strategy_results');
	else
		showPanel(current);
	initDisplay();
});

function initStrategyPanels() {
	// Fetch sample, new strat, and help tab contents
	// If no page is found for a given tab, remove
	// that tab from the page
	$.ajax({
		url: "wdkCustomization/jsp/strategies/samplesTab.jsp",
		type: "GET",
		dataType: "html",
		success: function(data) {
			$("#sample_strat").html(data);
				try {
					customSampleTab();
				}
				catch(e) {}
		},
		error: function() {
			$("#tab_sample_strat").parent("li").remove();
		}
	});
	$.ajax({
		url: "wdkCustomization/jsp/strategies/newTab.jsp",
		type: "GET",
		dataType: "html",
		success: function(data) {
			$("#strategy_new").html(data);
				try {
					customNewTab();
				}
				catch(e) {}
		},
		error: function() {
			$("#tab_strategy_new").parent("li").remove();
		}
	});
	$.ajax({
		url: "wdkCustomization/jsp/strategies/helpTab.jsp",
		type: "GET",
		dataType: "html",
		success: function(data) {
			$("#help").html(data);
				try {
					customHelpTab();
				}
				catch(e) {}
		},
		error: function() {
			$("#tab_help").parent("li").remove();
		},
		complete: function() {
			initHelp();
		}
	});
}

function initDisplay(){
	var url = "showStrategy.do";
	$.ajax({
		url: url,
		type: "POST",
		dataType: "json",
		data:"state=",
		beforeSend: function(){
			showLoading();
		},
		success: function(data){
			updateStrategies(data);
		}
	});
}

function highlightStep(str, stp, v, pagerOffset, ignoreFilters, action){
	if(!str || stp == null){
		NewResults(-1); // don't show result, remove anything that is there, and empty the result section
	}else{
		NewResults(str.frontId, stp.frontId, v, pagerOffset, ignoreFilters, action);
	}
}

function updateStrategies(data, ignoreFilters){	
	state = data.state;
	p_state = $.json.serialize(state);
	removeClosedStrategies();
	for(st in state){
          if(st == "count")
                $("#mysearch span").text('('+state[st]+')');
	  else if(st != "length"){
		var str = state[st].id;
		if(isLoaded(str)){
			if(getStrategyFromBackId(state[st].id).checksum != state[st].checksum){
				loadModel(data.strategies[state[st].checksum], st);
			}
	  	}else{
			loadModel(data.strategies[state[st].checksum], st);
		}
	  }
	}
	showStrategies(data.currentView, ignoreFilters, data.state.length);
}

function removeClosedStrategies(){
	for(s in strats){
		if(s.indexOf(".") == -1){
			var x = true;
			for(t in state){
				if(t != "length"){
					if(strats[s].checksum == state[t].checksum){
						x = false;
						if(t != s){
							strats[t] = strats[s];
							removeSubStrategies(s, t);
							delete strats[s];
							break;
						}
					}else if(strats[s].backId == state[t].id){
						x = false;
						removeSubStrategies(s, t);
						if(t != s){
							strats[t] = strats[s];
							break;
						}
					}
				}
			}
			if(x){
				removeSubStrategies(s);
				delete strats[s];
				update_hist = true; //set update flag for history if anything was closed.
			}
		}
	}
}

function removeSubStrategies(ord1, ord2){
	for(var f in strats){
		if(f.split(".").length > 1 && f.split(".")[0] == ord1){
			if(ord2 == undefined){
				delete strats[f];
			}else{
				var n_ord = f.split(".");
				n_ord[0] = ord2;
				n_ord = n_ord.join(".");
				strats[n_ord] = strats[f];
				delete strats[f];
			}
		}
	}
}

function showStrategies(view, ignoreFilters, besc){
	$("#tab_strategy_results font.subscriptCount").text("(" + besc + ")");
	var sC = 0;
	for(s in strats){
		if(s.indexOf(".") == -1)
			sC++;
	}
	var s2 = document.createElement('div');
	for(var t=1; t<=sC; t++){
		$(s2).prepend(strats[t].DIV);
		displayOpenSubStrategies(strats[t], s2);
	}
	$("#strategy_messages").hide();
	$("#strategy_results .resizable-wrapper:has(#Strategies)").show();
	$("#Strategies").html($(s2).html());
	var height = getCurrentTabCookie('strategyWindow');
	if (!height && $("#Strategies").parent().parent().height() > 330) {
		$("#Strategies").parent().parent().height(330);
	}
	else if (height) {
		height = parseInt(height);
		if ($("#strategy_results .resizable-wrapper:has(#Strategies)").height() > height) {
			$("#strategy_results .resizable-wrapper:has(#Strategies)").height(height);
		}
		else if ($("#Strategies").height() + 10 < $("#strategy_results .resizable-wrapper:has(#Strategies)").height()) {
			$("#strategy_results .resizable-wrapper:has(#Strategies)").height($("#Strategies").height() + 10);
		}
	}
	if(view.action != undefined) {
		if (view.action == "share" || view.action == "save") {
			var x = $("a#" + view.action + "_" + view.actionStrat);
			x.click();
		}			
	}
	if(view.strategy != undefined || view.step != undefined){
		var initStr = getStrategyFromBackId(view.strategy);
		var initStp = initStr.getStep(view.step, false);
		if(initStr == false || initStp == null){
			NewResults(-1);
		}else{
			var isVenn = (initStp.back_boolean_Id == view.step);
			var pagerOffset = view.pagerOffset;
			if(view.action != undefined && view.action.match("^basket")) {
				highlightStep(initStr, initStp, isVenn, pagerOffset, ignoreFilters, view.action);
			}
			else {
				highlightStep(initStr, initStp, isVenn, pagerOffset, ignoreFilters);
			}
		}
	}else{
		NewResults(-1);
	}
	if(sC == 0) showInstructions();
}

function displayOpenSubStrategies(s, d){
	//Colors for expanded substrategies
	var indent = 20;
	var colors = new Array();
	colors[0] = {step:"#A00000", top:"#A00000", right:"#A00000", bottom:"#A00000", left:"#A00000"};
	colors[1] = {step:"#A0A000", top:"#A0A000", right:"#A0A000", bottom:"#A0A000", left:"#A0A000"};
	colors[2] = {step:"#A000A0", top:"#A000A0", right:"#A000A0", bottom:"#A000A0", left:"#A000A0"};
	colors[3] = {step:"#00A0A0", top:"#00A0A0", right:"#00A0A0", bottom:"#00A0A0", left:"#00A0A0"};
	colors[4] = {step:"#0000A0", top:"#0000A0", right:"#0000A0", bottom:"#0000A0", left:"#0000A0"};
	var sCount = 0;
	for(j in s.subStratOrder)
		sCount++;
	for(var j=1;j<=sCount;j++){
		subs = getStrategy(s.subStratOrder[j]);
		subs.color = parseInt(s.getStep(getStrategy(s.subStratOrder[j]).backId.split("_")[1],false).frontId) % colors.length;
		$(subs.DIV).addClass("sub_diagram").css({"margin-left": (subs.depth(null) * indent) + "px",
												 "border-color": colors[subs.color].top+" "+colors[subs.color].right+" "+colors[subs.color].bottom+" "+colors[subs.color].left
												});
		$("div#diagram_" + s.frontId + " div#step_" + s.getStep(getStrategy(s.subStratOrder[j]).backId.split("_")[1],false).frontId + "_sub", d).css({"border-color":colors[subs.color].step});
		$("div#diagram_" + s.frontId, d).after(subs.DIV);
		if(getSubStrategies(s.subStratOrder[j]).length > 0){
			displayOpenSubStrategies(getStrategy(s.subStratOrder[j]),d);
		}
	}
}

function showInstructions(){
	$("#strat-instructions").remove();
	$("#strat-instructions-2").remove();
	var instr = document.createElement('div');
	var id = "strat-instructions";
	if ($("#tab_strategy_new").length > 0) id = "strat-instructions-2"
	$(instr).attr("id",id).html(getInstructionsHtml());
	$("#strategy_messages").append(instr);
	$("#strategy_results .resizable-wrapper:has(#Strategies)").hide();
	$("#strategy_messages").show();
}

function getInstructionsHtml() {
	var arrow_image = "<img id='bs-arrow' alt='Arrow pointing to Browse Strategy Tab' src='wdk/images/lookUp2.png' width='45px'/>"; 
	if ($("#tab_strategy_new").length > 0) {
		arrow_image = "<img id='ns-arrow' alt='Arrow pointing to New Search Button' src='wdk/images/lookUp.png' width='45px'/>" + arrow_image;
	}
	
	arrow_image += getInstructionsText();
	return arrow_image;
}

function getInstructionsText() {
	var instr_text = "<p style='width: 85px; position: absolute; padding-top: 14px;'>Run a new search to start a strategy</p>";
        if ($("#tab_strategy_new").length > 0) {
		instr_text = "<p style='width: 85px; position: absolute; left: 12px; padding-top: 14px;'>Click '<a href=\"javascript:showPanel('strategy_new')\">New</a>' to start a strategy</p>";
	}
	var instr_text2 = "<p style='width: 85px; position: absolute; right: 12px; padding-left: 1px;'>Or Click on '<a href=\"javascript:showPanel('search_history')\">All</a>' to view your strategies.</p>";
	return instr_text + "<br>" + instr_text2
}

function loadModel(json, ord){
	update_hist = true; //set update flag for history if anything was opened/changed.
	var strategy = json;
	var strat = null;
	if(!isLoaded(strategy.id)){
		var strat = new Strategy(sidIndex, strategy.id, true);
		sidIndex++;
	}else{
		var strat = getStrategyFromBackId(strategy.id);
		strat.subStratOrder = new Object();
	}		
	if(strategy.importId != ""){
		strat.isDisplay = true;
		strat.checksum = state[ord].checksum;
	}else{
		var prts = strat.backId.split("_");
		strat.subStratOf = getStrategyFromBackId(prts[0]).frontId;
		if(strategy.order > 0){
			strat.isDisplay = true;
		}
	}
	strat.JSON = strategy;
	strat.isSaved = strategy.saved;
	strat.name = strategy.name;
  	strat.importId = strategy.importId;
	var steps = strategy.steps;
	strats[ord] = strat;
	strat.initSteps(steps, ord);
	strat.dataType = strategy.steps[strategy.steps.length].dataType;
	strat.displayType = strategy.steps[strategy.steps.length].displayType;
	strat.nonTransformLength = strategy.steps.nonTransformLength;
	strat.DIV = displayModel(strat);
	return strat.frontId;
}

function unloadStrategy(id){
	for(s in strats){
		s = parseInt(s);
		if(strats[s].frontId == id){
			delete strats[s];
			return;
		}
	}
}


function NewResults(f_strategyId, f_stepId, bool, pagerOffset, ignoreFilters, action){
	if(f_strategyId == -1){
		$("#strategy_results > div.Workspace").html("");
		current_Front_Strategy_Id = null;
		return;
	}
	current_Front_Strategy_Id = f_strategyId;
	var strategy = getStrategy(f_strategyId);
	var step = strategy.getStep(f_stepId,true);
	url = "showSummary.do";
	var d = new Object();
	d.strategy = strategy.backId;
	d.step = step.back_step_Id;
	d.resultsOnly = true;
	d.strategy_checksum = (strategy.checksum != null) ? strategy.checksum : getStrategy(strategy.subStratOf).checksum;
	if(bool){
		d.step = step.back_boolean_Id;
	}
	if (!pagerOffset) {
		d.noskip = 1;
	}else{ 
		d.pager = new Object();
		d.pager.offset = pagerOffset;    
	}
	$.ajax({
		url: url,
		dataType: "html",
		type: "post",
		data: d,
		beforeSend: function(){
			showLoading(f_strategyId);
		},
		success: function(data){
			step.isSelected = true;
			if(ErrorHandler("Results", data, strategy, $("#diagram_" + strategy.frontId + " step_" + step.frontId + "_sub div.crumb_details div.crumb_menu a.edit_step_link"))){
				$("#Strategies div").removeClass("selected");
				init_view_strat = strategy.backId
				if(bool){
					$("#Strategies div#diagram_" + strategy.frontId + " div[id='step_" + step.frontId + "']").addClass("selected");
					init_view_step = step.back_step_Id + ".v";
				}else{
					$("#Strategies div#diagram_" + strategy.frontId + " div[id='step_" + step.frontId + "_sub']").addClass("selected");
					init_view_step = step.back_step_Id;
				}
				ResultsToGrid(data, ignoreFilters, "strategy_results");
                                updateResultLabels("strategy_results", strat, step);
                                
                // remember user's action, if user is not logged in, and tries to save, this place 
                // holds the previous action the user was doing.
				var linkToClick = $("a#" + action);
				if (linkToClick.length > 0) {
					linkToClick.click();
				}
                        } 
	                removeLoading(f_strategyId);
			checkPageBasket();
			$.cookie("refresh_results", "false", { path : '/' });
		}
	});
}

function RenameStep(ele, s, stp){
	var new_name = $(ele).val();
	step = getStep(s, stp);
	var url = "renameStep.do?strategy=" + getStrategy(s).backId + "&stepId=" + step.back_step_Id + "&customName=" + escape(new_name);	
	$.ajax({
			url: url,
			dataType: "html",
			data: "state=" + p_state,
			beforeSend: function(){
				showLoading(s);
			},
			success: function(data){
				data = eval("(" + data + ")");
				if(ErrorHandler("RenameStep", data, getStrategy(s), null)){
					updateStrategies(data);
				}else{
					removeLoading(f_strategyId);
				}
			}
		});
}

// will be replaced by wizard
function AddStepToStrategy(url, proto, stpId){	
	var strategy = getStrategyFromBackId(proto);
	var b_strategyId = strategy.backId;
	var f_strategyId = strategy.frontId;
	var cs = strategy.checksum;
	if(strategy.subStratOf != null)
		cs = getStrategy(strategy.subStratOf).checksum;
	url = url + "&strategy_checksum="+cs;
	var d = parseInputs();
	$.ajax({
		url: url,
		type: "POST",
		dataType:"json",
		data: d + "&state=" + p_state,
		beforeSend: function(){
			showLoading(f_strategyId);
		},
		success: function(data){
			if(ErrorHandler("AddStep", data, strategy, $("div#query_form"))){
				if($("div#query_form").css("display") == "none") $("div#query_form").remove();
				updateStrategies(data);
			}else{
				removeLoading(f_strategyId);
			}
		}
	});
	isInsert = "";
	closeAll(true);
}

function callSpanLogic(){
	var cstrt = getStrategy(current_Front_Strategy_Id);
	var f_strategyId = cstrt.frontId;
	var b_strategyId = cstrt.backId;
	var d = parseInputs();
	var quesName = "";
	var outputType = "";
	$("#form_question input[name='value(span_output)']").each(function(){
		if(this.checked) outputType = $(this).val();
	});
	outputType = (outputType.indexOf("A") != -1) ? "a" : "b";
	outputType = $("#form_question input#type"+outputType.toUpperCase()).val();
	if(outputType == "GeneRecordClasses.GeneRecordClass") quesName = "SpanQuestions.GenesBySpanLogic";
	if(outputType == "OrfRecordClasses.OrfRecordClass") quesName = "SpanQuestions.OrfsBySpanLogic";
	if(outputType == "IsolateRecordClasses.IsolateRecordClass") quesName = "SpanQuestions.IsolatesBySpanLogic";
	if(outputType == "EstRecordClasses.EstRecordClass") quesName = "SpanQuestions.EstsBySpanLogic";
	if(outputType == "SnpRecordClasses.SnpRecordClass") quesName = "SpanQuestions.SnpsBySpanLogic";
	if(outputType == "AssemblyRecordClasses.AssemblyRecordClass") quesName = "SpanQuestions.AssemblyBySpanLogic";
	if(outputType == "SequenceRecordClasses.SequenceRecordClass") quesName = "SpanQuestions.SequenceBySpanLogic";
	if(outputType == "SageTagRecordClasses.SageTagRecordClass") quesName = "SpanQuestions.SageTagsBySpanLogic";
	if(outputType == "DynSpanRecordClasses.DynSpanRecordClass") quesName = "SpanQuestions.DynSpansBySpanLogic";
	if(outputType == "") return null;
	$.ajax({
		url:"processFilter.do?questionFullName="+quesName+"&strategy="+cstrt.backId+"&strategy_checksum="+cstrt.checksum,
		data: d+"&state="+p_state,
		type: "post",
		dataType: "json",
		beforeSend: function(){
			showLoading(f_strategyId);
		},
		success: function(data){
			if(ErrorHandler("AddStep", data, cstrt, $("div#query_form"))){
				if($("div#query_form").css("display") == "none") $("div#query_form").remove();
				updateStrategies(data);
			}else{
				removeLoading(f_strategyId);
			}
		}
	});
	isSpan = false;
	isInsert = "";
	closeAll(true);
}

function EditStep(url, proto, step_number){
	var ss = getStrategyFromBackId(proto);
	var sss = ss.getStep(step_number, false);
	var d = parseInputs();
	var cs = ss.checksum;
	if(ss.subStratOf != null)
		cs = getStrategy(ss.subStratOf).checksum;
	url = url+"&strategy_checksum="+cs;
		$.ajax({
		url: url,
		type: "POST",
		dataType:"json",
		data: d + "&state=" + p_state,
		beforeSend: function(obj){
			    closeAll(true);
				showLoading(ss.frontId);
			},
		success: function(data){
			if(ErrorHandler("EditStep", data, ss, $("div#query_form"))){
				$("div#query_form").remove();
				hideDetails();
				updateStrategies(data);
			}else{
				removeLoading(ss.frontId);
			}
		}
	});
}



function DeleteStep(f_strategyId,f_stepId){
	var strategy = getStrategy(f_strategyId);
	var step = strategy.getStep(f_stepId, true);
	var cs = strategy.checksum;
	if(strategy.subStratOf != null)
		cs = getStrategy(strategy.subStratOf).checksum;
	if (step.back_boolean_Id == "")
		url = "deleteStep.do?strategy=" + strategy.backId + "&step=" + step.back_step_Id+"&strategy_checksum="+cs;
	else
		url = "deleteStep.do?strategy=" + strategy.backId + "&step=" + step.back_boolean_Id+"&strategy_checksum="+cs;
		
	$.ajax({
		url: url,
		type: "post",
		dataType:"json",
		data:"state=" + p_state,
		beforeSend: function(obj){
				showLoading(f_strategyId);
			},
		success: function(data){
				if(ErrorHandler("DeleteStep", data, strategy, null)){
					updateStrategies(data);
				}else{
					removeLoading(strategy.frontId);
				}	
			}
	});
}

function ExpandStep(e, f_strategyId, f_stepId, collapsedName){
	var strategy = getStrategy(f_strategyId);
	var step = strategy.getStep(f_stepId, true);
	var cs = strategy.checksum;
	if(strategy.subStratOf != null)
		cs = getStrategy(strategy.subStratOf).checksum;
	url = "expandStep.do?strategy=" + strategy.backId + "&step=" + step.back_step_Id + "&collapsedName=" + collapsedName+"&strategy_checksum="+cs;
	$.ajax({
		url: url,
		type: "post",
		dataType: "json",
		data: "state=" + p_state,
		beforeSend: function(){
			showLoading(f_strategyId);
		},
		success: function(data){
			if(ErrorHandler("EditStep", data, strategy, $("div#query_form"))){
				updateStrategies(data);
			}else{
				removeLoading(f_strategyId);
			}
		}
	});
}

function openStrategy(stratId){
	var url = "showStrategy.do?strategy=" + stratId;
	strat = getStrategyFromBackId(stratId);
	$.ajax({
		url: url,
		dataType:"json",
		data:"state=" + p_state,
		beforeSend: function(){
			$("body").block();
		},
		success: function(data){
			$("body").unblock();
			if(ErrorHandler("Open", data, null, null)){
				updateStrategies(data);
				if ($("#strategy_results").css('display') == 'none') showPanel('strategy_results');
			}
		}
	});
}

function deleteStrategy(stratId, fromHist){
	var url = "deleteStrategy.do?strategy=" + stratId;
	var stratName;
	var message = "Are you sure you want to delete the strategy '";
	if (fromHist) stratName = $.trim($("div#text_" + stratId).text());
	else {
		strat = getStrategyFromBackId(stratId);
		stratName = strat.name;
		if (strat.subStratOf != null) {
			var parent = getStrategy(strat.subStratOf);
			var cs = parent.checksum;
			url = "deleteStep.do?strategy="+strat.backId+"&step="+stratId.split('_')[1]+"&strategy_checksum="+cs;
			message = "Are you sure you want to delete the substrategy '";
			stratName = strat.name + "' from the strategy '" + parent.name;
		}
	}
	message = message + stratName + "'?";
	var agree = confirm(message);
	if (agree){
	$.ajax({
		url: url,
		dataType: "json",
		data:"state=" + p_state,
		beforeSend: function(){
			if (!fromHist) showLoading(stratId);
		},
		success: function(data){
			if (ErrorHandler("DeleteStrategy", data, null, null)){
				updateStrategies(data);
				updateHist = true;
				if ($('#search_history').css('display') != 'none'){
					updateHistory();
				}
			}
		}
	});
	}
}

function closeStrategy(stratId, isBackId){
	var strat = getStrategy(stratId);
	if (isBackId) {
		strat = getStrategyFromBackId(stratId);
		stratId = strat.frontId;
	}
	var cs = strat.checksum;
	if(strat.subStratOf != null){
		cs = getStrategy(strat.subStratOf).checksum;
	}
	var url = "closeStrategy.do?strategy=" + strat.backId+"&strategy_checksum="+cs;
	$.ajax({
		url: url,
		dataType:"json",
		data:"state=" + p_state,
		beforeSend: function(){
			showLoading(stratId);
		},
		success: function(data){
			if(ErrorHandler("CloseStrategy", data, strat, null)){
				updateStrategies(data);
				if ($('#search_history').css('display') != 'none') {
					update_hist = true;
					updateHistory();
				}
			}
		}
	});
}

// maybe deprecated??
function hideStrat(id){
	var strat = getStrategy(id);
	if(!strat) return;
	unloadStrategy(id);
	strat.isDisplay = false;
	for(var i=0;i<strat.Steps.length;i++){
		if(strat.Steps[i].child_Strat_Id != null){
			hideStrat(strat.Steps[i].child_Strat_Id);
		}
	}
	if($("#diagram_" + id + " div.selected").length > 0){
		NewResults(-1);
	}
	$("#diagram_" + id).hide("slow").remove();
	if($("#Strategies div[id^='diagram']").length == 0){
		showInstructions();
		NewResults(-1);
	}
}

function copyStrategy(stratId, fromHist){
        var ss = getStrategyOBJ(stratId);
        var result = confirm("Do you want to make a copy of strategy '" + ss.name + "'?");
        if (result == false) return;
        var url="copyStrategy.do?strategy=" + stratId + "&strategy_checksum="+ss.checksum;
        $.ajax({        
                url: url,
                dataType: "json", 
                data:"state=" + p_state,
				beforeSend: function(){
					if(!fromHist)
						showLoading(ss.frontId);
				},
                success: function(data){
					if(ErrorHandler("Copystrategy", data, ss, null)){
						updateStrategies(data);
						if (fromHist) {
							update_hist = true;
							updateHistory();
						}
					}
                }
        });     
}

function saveOrRenameStrategy(stratId, checkName, save, fromHist){
	var strat = getStrategyOBJ(stratId);
	var form = $("#save_strat_div_" + stratId);
	if (fromHist) form = $(".viewed-popup-box form");
	var name = $("input[name='name']",form).attr("value");
	var strategy = $("input[name='strategy']",form).attr("value");
	var action = $("input[name='action']",form).val();
	var actionStrat = $("input[name='actionStrat']",form).val();
	var url="renameStrategy.do?strategy=";
	var cs = strat.checksum;
	if(strat.subStratOf != null)
		cs = getStrategy(strat.subStratOf).checksum;
	url = url + strategy + "&name=" + escape(name) + "&checkName=" + checkName+"&save=" + save + "&action=" + action + "&actionStrat=" + actionStrat + "&strategy_checksum="+cs;
	if (fromHist) url = url + "&showHistory=true";
	$.ajax({
		url: url,
		dataType: "json",
		data:"state=" + p_state,
		beforeSend: function(){
			if(!fromHist)
				showLoading(strat.frontId);
		},
		success: function(data){
					var type = save ? "SaveStrategy" : "RenameStrategy";
					if(ErrorHandler(type, data, strat, form, name, fromHist)){
							updateStrategies(data);
							if (fromHist) {
								update_hist = true;
								updateHistory();
							}
					}
					if(!fromHist)
						removeLoading(strat.frontId);
		}
	});
}

function ChangeFilter(strategyId, stepId, url, filter) {
	var filterElt = filter;
        b_strategyId = strategyId;
        strategy = getStrategyFromBackId(b_strategyId); 
        f_strategyId = strategy.frontId;
        if(strategy.subStratOf != null){
                strats.splice(findStrategy(f_strategyId));
        }
		var cs = strategy.checksum;
		if(strategy.subStratOf != null)
			cs = getStrategy(strategy.subStratOf).checksum;
        url += "&strategy_checksum="+cs;
        $.ajax({
                url: url,
                type: "GET",
                dataType:"json",
				data:"state=" + p_state,
                beforeSend: function(){
			$("#strategy_results > div.Workspace").block();
                        showLoading(f_strategyId);
                },
                success: function(data){
                        if(ErrorHandler("ChangeFilter", data, strategy, null)){
                        	updateStrategies(data, true);
							$("div.layout-detail td div.filter-instance div.current").removeClass('current');
							$(filterElt).parent('div').addClass('current');
						}
                }
        });
}


function SetWeight(e, f_strategyId, f_stepId){
        var strategy = getStrategy(f_strategyId);
        var step = strategy.getStep(f_stepId, true);
        var cs = strategy.checksum;
        var weight = $(e).siblings("input#weight").val();
		if(weight == undefined)
			weight = $(e).siblings().find("input[name='weight']").val();
        if(strategy.subStratOf != null)
                cs = getStrategy(strategy.subStratOf).checksum;
        var url = "processFilter.do?strategy=" + strategy.backId + "&revise=" + step.back_step_Id + "&weight=" + weight + "&strategy_checksum="+cs;
        $.ajax({
                url: url,
                type: "post",
                dataType: "json",
                data: "state=" + p_state,
                beforeSend: function(){
                        showLoading(f_strategyId);
                },
                success: function(data){
                        if(ErrorHandler("SetWeight", data, strategy, null)){
                                updateStrategies(data);
                        }else{
                                removeLoading(f_strategyId);
                        }
                }
        });
}
