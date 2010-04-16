/*
WDK Strategy System
results.js

Provides functions to support results table
*/
function moveAttr(col_ix, table) {
	// Get name of target attribute & attribute to left (if any)
	// NOTE:  Have to convert these from frontId to backId!!!
	var headers = $("tr.headerrow th", table);
	var attr = $(headers[col_ix]).attr("id");
	var left, strat, step;
	if (col_ix > 0) left = $(headers[col_ix-1]).attr("id");
	// Figure out what step/strategy is currently displayed in results panel
	if ($(table).parents("#strategy_results").length > 0) {
		var step = $("div.selectedarrow");
	        if (step.length == 0) step = $("div.selectedtransform");
		if (step.length == 0) step = $("div.selected");
		var stepfId = step.attr("id").split('_')[1];
		var stratfId = step.parent().parent().attr("id");
		stratfId = stratfId.substring(stratfId.indexOf('_') + 1);
		strat = getStrategy(stratfId).backId;
		step = getStep(stratfId, stepfId).back_step_Id;
	}
	else {
		step = $(table).attr('step');
	}
	// build url.
	var url = "processSummary.do?strategy=" + strat + "&step=" + step + "&command=arrange&attribute=" + attr + "&left=" + left;
	if ($(table).parents("#strategy_results").length > 0) {
		GetResultsPage(url, false, true);
	}
	else {
		ChangeBasket(url + "&results_only=true", true);
	}
}

function addAttr(attrSelector) {
	var attributes = attrSelector.val();

	if (attributes.length == 0) return;

	attributes = attributes.split(',').join("&attribute=");

	var url = attrSelector.attr('commandurl') + "&command=add&attribute=" + attributes;
	if (attrSelector.parents("div#strategy_results").length > 0) {
		GetResultsPage(url, true, true);
	}
	else {
		ChangeBasket(url + "&results_only=true");
	}
}


function resetAttr(url, button) {
    if (confirm("Are you sure you want to reset the column configuration back to the default?")) {
        var url = url + "&command=reset";
	if ($(button).parents("#strategy_results").length > 0) {
	        GetResultsPage(url, true, true);
	}
	else {
		ChangeBasket(url + "&results_only=true");
	}
    }
}


function GetResultsPage(url, update, ignoreFilters){
	var s = parseUrlUtil("strategy", url);
	var st = parseUrlUtil("step", url);
	var strat = getStrategyFromBackId(s[0]);
	var currentDiv = getCurrentBasketWrapper();
	var step = null;
	if(strat == false){
		strat = new Object();
		strat.JSON = new Object();
		step = new Object();
		strat.JSON.name = "";
		step.frontId = "n/a";
	}else
		step = strat.getStep(st[0], false);
	url = url + "&resultsOnly=true";
//	if (update){$("#strategy_results > div.Workspace").block();}
	if (update){$("#" + currentDiv + " > div.Workspace").block();}
	$.ajax({
		url: url,
		dataType: "html",
		beforeSend: function(){
			if(strat != false) showLoading(strat.frontId);
		},
		success: function(data){
			if (update && ErrorHandler("Results", data, strat, null)) {
				ResultsToGrid(data, ignoreFilters);
				$("span#text_strategy_number").html(strat.JSON.name);
				$("span#text_step_number").html(step.frontId);
				$("span#text_strategy_number").parent().show();
				$("#" + currentDiv + " > div.Workspace").unblock();
			}
			if(strat != false) removeLoading(strat.frontId);
		},
		error : function(data, msg, e){
			  alert("ERROR \n "+ msg + "\n" + e
                                      + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
		}
	});
}

function ResultsToGrid(data, ignoreFilters, div) {
	var oldFilters;
	var currentDiv = div;
	if(currentDiv == undefined) currentDiv = getCurrentBasketWrapper();
	if (ignoreFilters) {
		oldFilters = $("#strategy_results > div.Workspace div.layout-detail div.filter-instance .link-url");
	}

	$("#" + currentDiv + " > div.Workspace").html(data);

	try {
		customResultsPage();
	}
	catch(err) {
		//Do nothing;
	}
	
	// invoke filters
        var wdkFilter = new WdkFilter();
	
	if (ignoreFilters) {
		wdkFilter.addShowHide();
		wdkFilter.displayFilters();
		oldFilters.each(function() {
			var id = $(this).attr("id");
			var count = $(this).text();
			if (count == 0) {
				$("#" + id).replaceWith(this);
			} else {
				$("#" + id).html(count);
			}
		});
	}
	else {
        	wdkFilter.initialize();
	}

	// create multi select control for adding columns
	createMultiSelectAttributes($("#" + currentDiv + " #addAttributes"));

	// convert results table to drag-and-drop flex grid
	createFlexigridFromTable($("#" + currentDiv + " #Results_Table"));

	// check the basket for the page if needed
	checkPageBasket();
}

function createMultiSelectAttributes(attrSelect) {
	if (attrSelect.length > 0) { 
		attrSelect.multiSelect({selectAll: false,
				noneSelected: '--- Add Column ---',
				oneOrMoreSelected: '% selected: leave menu to submit'},
				function(selector) {
					addAttr(selector.prev("input#addAttributes"));
				});
	}
}

function createFlexigridFromTable(table) {
	table.flexigrid({height : 'auto',
			showToggleBtn : false,
			useRp : false,
			singleSelect : true,
			onMoveColumn : moveAttr,
                        nowrap : false,
			resizable : false});
}

function updatePageCount(pager_id) {
    var resultSize = parseInt($("input#resultSize").val());
    var psSelect = document.getElementById(pager_id + "_pageSize");
    var index = psSelect.selectedIndex;
    var pageSize = psSelect.options[index].value;
    var pageCount = Math.ceil(resultSize / pageSize);
    if (pageCount * pageSize < resultSize) pageCount++;
    var span = document.getElementById(pager_id + "_pageCount");
    span.innerHTML = pageCount;
}

function gotoPage(pager_id) {
    //alert("hello");
    var pageNumber = document.getElementById(pager_id + "_pageNumber").value;
    var psSelect = document.getElementById(pager_id + "_pageSize");
    var pageSize = psSelect.options[psSelect.selectedIndex].value;

    var pageUrl = document.getElementById("pageUrl").value;
    
    var pageOffset = (pageNumber - 1) * pageSize;
    var gotoPageUrl = pageUrl.replace(/\&pager\.offset=\d+/, "");
    gotoPageUrl = gotoPageUrl.replace(/\&pageSize=\d+/, "");
    gotoPageUrl += "&pager.offset=" + pageOffset;
    gotoPageUrl += "&pageSize=" + pageSize;
    $("div.advanced-paging").hide();
    GetResultsPage(gotoPageUrl, true, true);
}

function openAdvancedPaging(element){
    var button = $(element);
   
    var isOpen = (button.val() == "Advanced Paging");
    var panel = button.next(".advanced-paging");
    var offset = button.position();
    offset.left += button.width() + 20;
    offset.top -= 20;
	if(isOpen){
                    panel.css({"display" : "block",
                               "left": offset.left + "px",
                               "top": offset.top + "px",
			   "width": "290px", 
                               "z-index" : 500});
                    button.val("Cancel");
	}else{
                    panel.css({"display" : "none"});
                    button.val("Advanced Paging");
	}
}


