var _action = "";
var global_isAdd; 
var original_Query_Form_Text;
var original_Query_Form_CSS = new Object();
var current_Front_Strategy_Id = null;
var isSpan = false;
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
  $(".strategy-description.qtip").qtip("hide");
}

function showSaveForm(stratId, save, share){
	closeModal();
	$("div.save_strat_div").addClass("hidden");
	var saveForm = $("div#save_strat_div_" + stratId);
	var stratName = getStrategyOBJ(stratId).name;
	$("input[type=text]", saveForm).attr("value", stratName);
       if (save){
         $("form#save_strat_form", saveForm).attr("action", "javascript:saveOrRenameStrategy(" + stratId + ", true, true, false)");
         $("span.h3left", saveForm).text("Save As");
         $("input[type=submit]", saveForm).attr("value", "Save");
         if (share) {
		  $("form#save_strat_form", saveForm).append("<input type='hidden' name='action' value='share'/>");
                  $("form#save_strat_form", saveForm).append("<input type='hidden' name='actionStrat' value='" + stratId + "'/>");
		  $("span.h3left", saveForm).text("First you need to Save it!");
         }
	// we want save_warning, defined in view-JSON.js, to appear for save and share only
	 $("form#save_strat_form i").css("display","block");
       }
       else{
         $("form#save_strat_form", saveForm).attr("action", "javascript:saveOrRenameStrategy(" + stratId + ", true, false, false)");
         $("span.h3left", saveForm).text("Rename");
         $("input[type=submit]", saveForm).attr("value", "Rename");
	// we want save_warning, defined in view-JSON.js, to appear for save and share only
	 $("form#save_strat_form i").css("display","none");
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

function formatFilterForm(params, data, edit, reviseStep, hideQuery, hideOp, isOrtholog){
	//edit = 0 ::: adding a new step
	//edit = 1 ::: editing a current step
	var ps = document.createElement('div');
	var qf = document.createElement('div');
	var topMenu_script = null;
	qf.innerHTML = data;
	ps.innerHTML = params.substring(params.indexOf("<form"),params.indexOf("</form>") + 6);
	if($("script#initScript", ps).length > 0)
		topMenu_script = $("script#initScript", ps).text();
	var operation = "";
	var stepn = 0;
	var insert = "";
	var proto = "";
	var currStrategy = getStrategy(current_Front_Strategy_Id);
	var stratBackId = currStrategy.backId;
	var stp = null;
	var stepBackId = null;
	if(edit == 0){
		insert = reviseStep;
		if (insert == ""){
			stp = currStrategy.getLastStep();
			stepBackId = (stp.back_boolean_Id == "") ? stp.back_step_Id : stp.back_boolean_id;
		}else{
			stp = currStrategy.getStep(insert,false);
			stepBackId = insert;
		}
	}else{
		var parts = reviseStep.split(":");
		proto = parts[0];
		reviseStep = parseInt(parts[1], 10);
		stp = currStrategy.getStep(reviseStep,false);
		stepBackId = reviseStep;
		isSub = true;
		operation = parts[4];
	}
	var pro_url = "";
	if(edit == 0)
		pro_url = "processFilter.do?strategy=" + stratBackId + "&insert=" +insert + "&ortholog=" + isOrtholog;
	else{
		pro_url = "processFilter.do?strategy=" + stratBackId + "&revise=" + stepBackId;
	}
	var historyId = $("#history_id").val();
	
	if(edit == 0){
		var close_link = "<a class='close_window' href='javascript:closeAll(false)'><img src='wdk/images/Close-X-box.png'/></a>";
		var back_link = "<a id='back_to_selection' href='javascript:close()'><img src='wdk/images/backbox.png'/></a>";
	}else
		var close_link = "<a class='close_window' href='javascript:closeAll(false)'><img src='wdk/images/Close-X-box.png'/></a>";

	var quesTitle = data.substring(data.indexOf("<h1>") + 4,data.indexOf("</h1>")).replace(/Identify \w+( \w+)* based on/,"");
	
	var quesForm = $("#form_question",qf);
	if(quesForm[0].tagName != "FORM"){
		var f = document.createElement('form');
		$(f).attr("id",$(quesForm).attr("id"));
		$(f).html($(quesForm).html());
		quesForm = $(f);
	}
	var quesDescription = $("#query-description-section",qf);//data);
	var dataSources = $("#attributions-section",qf);
	$("input[value='Get Answer']",quesForm).val("Run Step");
	$("input[value='Run Step']",quesForm).attr("id","executeStepButton");
	$(".params", quesForm).wrap("<div class='filter params'></div>");
	$(".params", quesForm).attr("style", "margin-top:15px;");

        // hide the file upload box
        quesForm.find(".dataset-file").each(function() {
            $(this).css("display", "none");
        });
	
	// Bring in the advanced params, if exist, and remove styling
	var advanced = $("#advancedParams_link",quesForm);
	advanced = advanced.parent();
	advanced.remove();
	advanced.attr("style", "");
	$(".filter.params", quesForm).append(advanced);
	
	if(edit == 0){
		if(insert == "" || (stp.isLast && isOrtholog)){
			$(".filter.params", quesForm).prepend("<span class='form_subtitle'>Add&nbsp;Step&nbsp;" + (parseInt(stp.frontId, 10)+1) + ": " + quesTitle + "</span></br>");		
		}else if (stp.frontId == 1 && !isOrtholog){
			$(".filter.params", quesForm).prepend("<span class='form_subtitle'>Insert&nbsp;Step&nbsp;Before&nbsp;" + (stp.frontId) + ": " + quesTitle + "</span></br>");
		}else if (isOrtholog){
			$(".filter.params", quesForm).prepend("<span class='form_subtitle'>Insert&nbsp;Step&nbsp;Between&nbsp;" + (stp.frontId) + "&nbsp;And&nbsp;" + (parseInt(stp.frontId, 10)+1) + ": " + quesTitle + "</span></br>");		
		}else{
			$(".filter.params", quesForm).prepend("<span class='form_subtitle'>Insert&nbsp;Step&nbsp;Between&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;And&nbsp;" + (stp.frontId) + ": " + quesTitle + "</span></br>");		
		}
	}else{
		$(".filter.params", quesForm).prepend("<span class='form_subtitle'>Revise&nbsp;Step&nbsp;" + (stp.frontId) + ": " + quesTitle + "</span></br>");
	}
	if(edit == 0){
		if(insert == ""){
			$(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (stp.frontId) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck' valign='middle'><input type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td valign='middle'>&nbsp;" + (stp.frontId) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>UNION</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td></tr></table></div></div>");
		}else{
			$(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (parseInt(stp.frontId, 10)-1) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck' valign='middle'><input type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td valign='middle'>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>UNION</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "</td></tr></table></div></div>");
		}
	} else {
		if(stp.frontId != 1){
			$(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (parseInt(stp.frontId, 10)-1) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck'><input id='INTERSECT' type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input id='UNION' type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>UNION</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input id='MINUS' type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "</td></tr></table></div></div>");
		}else{
			$(".filter.params", quesForm).after("<input type='hidden' name='booleanExpression' value='AND' />");
		}
	}
	if(edit == 0)	
		var action = "javascript:validateAndCall('add','" + pro_url + "', '" + stratBackId + "')";
	else
		var action = "javascript:validateAndCall('edit', '" + pro_url + "', '" + stratBackId + "', "+ parseInt(reviseStep, 10) + ")";
	var formtitle = "";
	if(edit == 0){
		if(insert == "")
			formtitle = "<h1 style='font-size:130%;position:relative;margin-top: 4px;'>Add&nbsp;Step</h1>";
		else
			formtitle = "<h1  style='font-size:130%;position:relative;margin-top: 4px;'>Insert&nbsp;Step</h1>";
	}else{
		formtitle = "<h1  style='font-size:130%;position:relative;margin-top: 4px;'>Revise&nbsp;Step</h1>";
	}
	quesForm.attr("action",action);
	if(edit == 0)
		var header = "<span class='dragHandle'>" + back_link + " " + formtitle + " " + close_link + "</span>";
	else
		var header = "<span class='dragHandle'>" + formtitle + " " + close_link + "</span>";
		
	$("#query_form").html(header);
	if (hideQuery){
	        $(".filter.params", quesForm).remove();
	        $("input[name=questionFullName]", quesForm).remove();
	        $(".filter.operators", quesForm).width('auto');
	}else{
		$("div.filter div.params", quesForm).html(ps.getElementsByTagName('form')[0].innerHTML);
	}
	if (hideOp){
		$(".filter.operators", quesForm).remove();
		$(".filter.params", quesForm).after("<input type='hidden' name='booleanExpression' value='AND' />");
	}
	
	$("#query_form").append(quesForm);

	if(edit == 1)
		$("#query_form div#operations input#" + operation).attr('checked','checked'); 
	
	if(quesDescription.length > 0)
		$("#query_form").append("<div style='padding:5px;margin:5px 15px 5px 15px;border-top:1px solid grey;border-bottom:1px solid grey'>" + quesDescription.html() + "</div>");

	if(dataSources.length > 0)
		$("#query_form").append("<div style='padding:5px;margin:5px 15px 5px 15px;border-top:1px solid grey;border-bottom:1px solid grey'>" + dataSources.html() + "</div>");

	$("#query_form").append("<div class='bottom-close'><a href='javascript:closeAll(false)' class='close_window'>Close</a></div>");
	setDraggable($("#query_form"), ".dragHandle");
	$("#query_form").fadeIn("normal");
	if(topMenu_script != null){
		var tms = topMenu_script.substring(topMenu_script.indexOf("{")+1,topMenu_script.indexOf("}"));
		eval(tms);
	}
	if(edit == 1)
		initParamHandlers(true, true);
	else
		initParamHandlers(true);
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

// the dtype is never used, ignored.
function openFilter(dtype,strat_id,step_id,isAdd){
	// open the default stage
	openStage(strat_id, step_id, isAdd, '');
}

function openStage(strat_id,step_id,isAdd, stage){
	global_isAdd = isAdd;
	if(openDetail != null) hideDetails();
	$("#strategy_results div.attributesList").hide();
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
	
	var url = "wizard.do?strategy="+currStrat.backId + "&step=" + currentStepId;
	if (stage != '') yurl += "&stage=" + stage;

    // add insert flag
    var action = isAdd ? "add" : "insert";
    url += "&action=" + action;

	$.ajax({
		url: url,
		dataType: "html",
		beforeSend: function(){
			$("#query_form").remove();
			$("#query_form_overlay").remove();
			disableAddStepButtons();
		},
		success: function(data){
      hideDetails();
			dykClose();
			$("body").append(data);
			if(isAdd)
				$("#query_form h1#query_form_title").html("Add&nbsp;Step");
			else
				$("#query_form h1#query_form_title").html("Insert&nbsp;Step");
			setDraggable($("#query_form"), ".dragHandle");
      $("#query_form").css("z-index", 9001);
      $("#query_form_overlay").css("z-index", 9000).height($("body").height());
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

function callWizard(url, ele, id, sec, action, stratFrontId){
  // set isPopup flag, which will be used by param initialization process
  window.isPopup = true;
  mapTypeAheads();
  if (stratFrontId == undefined) stratFrontId = current_Front_Strategy_Id; 
  var strategy = getStrategy(stratFrontId);
  $("div#errors").html("");
  switch (action) {
    case "submit":
      var stage = $(ele).find("#stage").val();
      url = url + "stage="+stage+"&strategy="+strategy.backId;
      $(ele).attr("action", "javascript:void(0)");
      $.ajax({
        url: url,
        type: "post",
        dataType: "html",
        data: parseInputs()+"&state="+p_state,
        beforeSend: function(){
          $(".crumb_details").block( {message: "Loading..."} );
          WizardLoading(true);
        },
        success: function(data){
          hideDetails();
          $(".crumb_details").unblock();
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
          } else {
            WizardLoading(false);
            $("#qf_content").children().wrapAll('<div class="stage" />');
            $("#qf_content > div.stage").appendTo("#stage-stack");
            setPopupContent(data);
          }
          $("#query_form").css("z-index", 9001);
          $("#query_form_overlay").css("z-index", 9000).height($("body").height());
        }
      });
      break;
    case "next":
      d = "strategy="+strategy.backId;
      $.ajax({
        url: url,
        type: "get",
        dataType: "html",
        data: d,
        beforeSend: function(jqXHR, data) {
          $(".crumb_details").block( {message: "Loading..."} );
        },
        success: function(data){
          hideDetails();
          $(".crumb_details").unblock();
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
              $("#qf_content").children().wrapAll('<div class="stage" />');
              $("#qf_content > .stage").appendTo("#stage-stack");
            }
            setPopupContent(data);

            if(ele != undefined){
              showNewSection(ele,id,sec);
            }
          }
          $("#query_form").css("z-index", 9001);
          $("#query_form_overlay").css("z-index", 9000).height($("body").height());
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
    var lastStage = $("#stage-stack > .stage:last").detach();
    if(lastStage.length == 0)
        closeAll();
    else {
        $("#qf_content").html("");
        $("#qf_content").append(lastStage);
        $("#qf_content > .stage").children().unwrap();
    }
}

function setPopupContent(data) {
    $("#qf_content").html(data);
    // updateStepNumberReferences();
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
		$("#query_form_overlay").hide();
	}else{
		isSpan = false;
		$("#query_form").remove();
		$("#query_form_overlay").remove();
		$(".original").remove();
		$("#stage-stack").html("");
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
