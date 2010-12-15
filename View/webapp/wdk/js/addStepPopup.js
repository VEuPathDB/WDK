var _action = "";
var global_isAdd; 
var original_Query_Form_Text;
var original_Query_Form_CSS = new Object();
var current_Front_Strategy_Id = null;
var isSpan = false;
var pop_up_state = new Array();
// var stage = null;
function showExportLink(stratId){
 	closeModal();
 	var exportLink = $("div#export_link_div_" + stratId);
 	exportLink.show();
}

function showPanel(panel) {
	if(panel == 'strategy_results'){
		if($("div#Strategies").attr("newstrategy") == 'true')
			initDYK(true);
		else
			initDYK(false);
	}else 
		initDYK(false);
	
	$("#strategy_tabs li").each(function(){
		if($("a", this).length > 0){
			var hidePanel = $("a", this).attr("id").substring(4);
			$("#tab_" + hidePanel).parent().removeAttr("id");
			$("#" + hidePanel).css({'position':'absolute','left':'-1000em','width':'100%','display':'none'});
		}
	});
	$("#tab_" + panel).parent().attr("id", "selected");
	$("#" + panel).css({'position':'relative','left':'auto','display':'block'});
	if (panel == 'strategy_results') {
		if($.cookie("refresh_results") == "true"){
			var currentStep = $("#Strategies div.selected");
			if (currentStep.length == 0) currentStep = $("#Strategies div.selectedarrow");
			if (currentStep.length == 0) currentStep = $("#Strategies div.selectedtransform");
			var active_link = $("a.results_link", currentStep);
			if(active_link.length == 0) active_link = $(".resultCount a.operation", currentStep);
			active_link.click();
			$.cookie("refresh_results", "false", { path : '/' });
		}
		$("body > #query_form").show();
		$("body > .crumb_details").show();
	}
	else {
		if (panel == 'search_history') updateHistory();
		if (panel == 'basket') showBasket();
		$("body > #query_form").hide();
		$("body > .crumb_details").hide();
	}
	setCurrentTabCookie('application', panel);
}

function showSaveForm(stratId, save, share){
	closeModal();
	$("div.save_strat_div").addClass("hidden");
	var saveForm = $("div#save_strat_div_" + stratId);
	var stratName = getStrategyOBJ(stratId).name;
	$("input[type=text]", saveForm).attr("value", stratName);
       if (save){
         $("form", saveForm).attr("action", "javascript:saveOrRenameStrategy(" + stratId + ", true, true, false)");
         $("span.h3left", saveForm).text("Save As");
         $("input[type=submit]", saveForm).attr("value", "Save");
         if (share) {
		  $("form", saveForm).append("<input type='hidden' name='action' value='share'/>");
                  $("form", saveForm).append("<input type='hidden' name='actionStrat' value='" + stratId + "'/>");
		  $("span.h3left", saveForm).text("First you need to Save it!");
         }
       }
       else{
         $("form", saveForm).attr("action", "javascript:saveOrRenameStrategy(" + stratId + ", true, false, false)");
         $("span.h3left", saveForm).text("Rename");
         $("input[type=submit]", saveForm).attr("value", "Rename");
       }
	saveForm.show();
         $("input[name='name']", saveForm).focus().select();
}

function closeModal(){
	$("div.modal_div").hide();
}

function validateSaveForm(form){
	var strat = getStrategyFromBackId(form.strategy.value);
        if (form.name.value == ""){
                var message = "<h1>You must specify a name for saving!</h1><input type='button' value='OK' onclick='$(\"div#diagram_" + strat.frontId + "\").unblock();$(\"div#search_history\").unblock();'/>";
                $("div#diagram_" + strat.frontId).block({message: message});
                $("div#search_history").block({message: message});
                return false;
        } else if (form.name.value.length > 200) {
                var message = "<h1>The name you have entered is too long.  Please enter a name that is at most 200 characters.</h1><input type='button' value='OK' onclick='$(\"div#diagram_" + strat.frontId + "\").unblock();$(\"div#search_history\").unblock();'/>";
                $("div#diagram_" + strat.frontId).block({message: message});
                $("div#search_history").block({message: message});
		return false;
	}
        return true;
}
function validateAndCall(type, url, proto, rs){
	var valid = false;
	if($("div#query_form div.filter.operators").length == 0){
		valid = true;
	}else{
		if($(".filter.operators")){
			$(".filter.operators div#operations input[name='booleanExpression']").each(function(){
				if($(this)[0].checked) valid = true;
			});
		}
	}
	if(!valid){
		alert("Please select Intersect, Union or Minus operator.");
		return;
	}
	mapTypeAheads();
	window.scrollTo(0,0);
	if(type == 'add'){
		AddStepToStrategy(url, proto, rs);
	}else{
		EditStep(url, proto, rs);
	}
	return;
}

function openFilter(dtype,strat_id,step_id,isAdd){
	global_isAdd = isAdd;
	if(openDetail != null) hideDetails();
	var isFirst = false;
	steps = getStrategy(strat_id).Steps;
	if(step_id == undefined){
		isFirst = true;
	}else{
		stp = getStrategy(strat_id).getStep(step_id,false)
		if(stp != null && stp.frontId == 1 && !isAdd) isFirst = true;
	}
	current_Front_Strategy_Id = strat_id;
	currStrat = getStrategy(strat_id);
	current_Front_Strategy_Id = strat_id;

        var currentStepId = stp.back_boolean_Id;
        if (currentStepId == '') currentStepId = stp.back_step_Id;
	var url = "wizard.do?strategy="+currStrat.backId+"&stage=list&step=" + currentStepId;

        // add insert flag
        var action = isAdd ? "add" : "insert";
        url += "&action=" + action;

	$.ajax({
		url: url,
		dataType: "html",
		beforeSend: function(){
			$("#query_form").remove();
			disableAddStepButtons();
		},
		success: function(data){
			dykClose();
			$("body").append(data);
			if(isAdd)
				$("#query_form h1#query_form_title").html("Add&nbsp;Step");
			else
				$("#query_form h1#query_form_title").html("Insert&nbsp;Step");
			setDraggable($("#query_form"), ".dragHandle");
		},
		error: function(){
			alert("Error getting the needed information from the server \n Please contact the system administrator");
			enableAddStepButtons();
		}
	});
}

var buttonText = null;
function WizardLoading(boo){
	if(boo){
		i = $("img#wizard-busy-image").clone();
        buttonText = $("div.filter-button").html();
		$("div.filter-button").html(i.show());
	} else {
        $("div.filter-button").html(buttonText);
        buttonText = null;
    }
}

function callWizard(url, ele, id, sec, action){
    var strategy = getStrategy(current_Front_Strategy_Id);
	$("div#errors").html("");
	switch (action){
			case "submit":
                                var stage = $(ele).find("#stage").val();
				url = url + "stage="+stage+"&strategy="+getStrategy(current_Front_Strategy_Id).backId;
				$(ele).attr("action", "javascript:void(0)");
				$.ajax({
					url: url,
					type: "get",
					dataType: "html",
					data: parseInputs()+"&state="+p_state,
					beforeSend: function(){
						WizardLoading(true);
					},
					success: function(data){
						if(data.indexOf("{") == 0){
							data = eval("("+data+")");
                            // before close, check if json is success or error, if error, display 
                            // it in the current qf_content
                            if (ErrorHandler("Wizard", data, strategy, $("#errors"))) {
                                closeAll();
                                updateStrategies(data);
                            } else {
                                WizardLoading(false);
                            }
						}else{
							WizardLoading(false);
							pop_up_state.push($("#qf_content").html());
							setPopupContent(data);
						}
					}	
				});
				break;
			case "next":
				d = "strategy="+getStrategy(current_Front_Strategy_Id).backId;
				$.ajax({
					url: url,
					type: "get",
					dataType: "html",
					data: d,
					success: function(data){
						if(data.indexOf("{") == 0){
							updateStrategies(data);
						}else{
							if($("#qf_content").length == 0){
								urlparts = url.split("/");
								$.ajax({
									async: false,
									url:"wdk/jsp/wizard/context.jsp",
									type:"get",
									success:function(data){
										$("body").append(data);
										setDraggable($("#query_form"), ".dragHandle");
									} 
								});
							}else{
								pop_up_state.push($("#qf_content").html());
							}
							setPopupContent(data);
							
							if(ele != undefined){
								showNewSection(ele,id,sec);
							}
						}
					}	
				});
				break;
			default:
				showNewSection(ele,id,sec);
				break;
	}
	
	return false;
}

function backStage(){
	var h = pop_up_state.pop()
	if(h == undefined)
		closeAll();
	else {
		setPopupContent(h);
        }
}

function setPopupContent(data) {
    $("#qf_content").html(data);
    updateStepNumberReferences();
}

function updateStepNumberReferences() {
    var stratBackId = $("#strategyId").text();
    var stepBackId = $("#stepId").text();
    var step = getStepFromBackId(stratBackId, stepBackId);
    var stepNumber = parseInt(step.frontId);
    $("span.current_step_num").text(stepNumber);
    $("span.new_step_num").text((stepNumber + 1));
}

// deprecated
function openAddStrategy(strat_id){
	original_Query_Form_Text = $("#query_form").html();
	OpenOperationBox(strat_id, (global_isAdd ? undefined : step_id));
	return false;
}

// deprecated -- to close the old question form
function close(ele){
	cd = $("#query_form");
	$(cd).html(original_Query_Form_Text);
	$("#query_form").css("max-width",original_Query_Form_CSS.maxW);
	$("#query_form").css("min-width",original_Query_Form_CSS.minW);
	setDraggable($("#query_form"), ".dragHandle");
	
	$("#query_form #continue_button").click(function(){
		original_Query_Form_Text = $("#query_form").html();
		OpenOperationBox(strat_id, undefined);
		return false;
	});

	$("#query_form #continue_button_transforms").click(function(){
		original_Query_Form_Text = $("#query_form").html();
		getQueryForm($("#query_form select#transforms").val(),true);
	});
}

function closeAll(hide,as){
	if(hide){
		$("#query_form").hide();
	}else{
		isSpan = false;
		$("#query_form").remove();
		$(".original").remove();
		pop_up_state = new Array();
	}
	isInsert = "";
	enableAddStepButtons();
}

function enableAddStepButtons() {
	$("#Strategies div a#filter_link span").css({opacity: 1.0}).each(function() {
		var button = $(this).parent("a");
		var oldHref = button.attr("oldHref");
		if (oldHref) {
			button.attr("href",oldHref);
			button.removeAttr("oldHref");
		}
	});
}

function disableAddStepButtons() {
	$("#Strategies div a#filter_link span").css({opacity: 0.4}).each(function() {
		var button = $(this).parent("a");
		button.attr("oldHref",button.attr("href"));
		button.attr("href","javascript:void(0);");
	});
}

function setDraggable(e, handle){
	var rlimit = $("div#contentwrapper").width() - e.width() - 18;
	if(rlimit < 0) rlimit = 525;
	var blimit = $("body").height();
	$(e).draggable({
		handle: handle,
		containment: [0,0,rlimit,blimit]
	});
}

function showNewSection(ele,sectionName,sectionNumber){
	isSpan = (sectionName == 'span_logic' || sectionName.split("_")[0] == 'sl'); 
	var sec = document.createElement('td');
	var s = $("div#" + sectionName + ".original").clone();
	$(sec).html($(s)
		.addClass("qf_section")
		.removeClass("original")
		.css({
			"display":"block",
			"background-color":"#EEEEEE"
			}))
		.attr("id","section-"+sectionNumber);
	for(i=sectionNumber; i<=5; i++){
		$("td#section-"+i+" div.qf_section").html("");
	}
	$(ele).parent().find("li").css({
		"background":"",
		"font-weight":""
	});
	$(ele).css({
		"background-color":"#DDDDDD"
	});
	$("#query_form table#sections-layout td#section-" + (sectionNumber-1) + " div").css("background-color","#FFFFFF");
	$("#query_form table#sections-layout td#section-" + sectionNumber).replaceWith(sec);
}

function changeButtonText(ele){
	var val = "";
 	var stage = $(ele).attr("stage");
        $(ele).parents("form").find("#stage").val(stage);
	if($(ele).val() != "SPAN"){
		v = "Run Step";
	}else{
		v = "Continue....";
	}
	//$("form#form_question").attr("action",stage);
	$(".filter-button input[name='questionSubmit']").attr("value",v);
}
