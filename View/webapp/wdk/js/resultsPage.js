/*
WDK Strategy System
results.js

Provides functions to support results table
*/
function moveAttr(col_ix) {
	// Get name of target attribute & attribute to left (if any)
	// NOTE:  Have to convert these from frontId to backId!!!
	var headers = $("div.flexigrid tr.headerrow th");
	var attr = $(headers[col_ix]).attr("id");
	var left;
	if (col_ix > 0) left = $(headers[col_ix-1]).attr("id");
	// Figure out what step/strategy is currently displayed in results panel
	var step = $("div.selectedarrow");
        if (step.length == 0) step = $("div.selectedtransform");
	if (step.length == 0) step = $("div.selected");
	var stepfId = step.attr("id").split('_')[1];
	var stratfId = step.parent().attr("id").split('_')[1];
	var strat = getStrategy(stratfId);
	var step = getStep(stratfId, stepfId);
	// build url.
	var url = "processSummary.do?strategy=" + strat.backId + "&step=" + step.back_step_Id + "&command=arrange&attribute=" + attr + "&left=" + left;
	GetResultsPage(url, false, true);
}

function addAttr(url) {
    var attributeSelect = document.getElementById("addAttributes");
    var attributes = attributeSelect.value;
    
    if (attributes.length == 0) return;

    attributes = attributes.split(',').join("&attribute=");

    var url = url + "&command=add&attribute=" + attributes;
    GetResultsPage(url, true, true);
}


function resetAttr(url) {
    if (confirm("Are you sure you want to reset the column configuration back to the default?")) {
        var url = url + "&command=reset";
        GetResultsPage(url, true, true);
    }
}
		

function GetResultsPage(url, update, ignoreFilters){
	var s = parseUrlUtil("strategy", url);
	var st = parseUrlUtil("step", url);
	var strat = getStrategyFromBackId(s[0]);
	var step = strat.getStep(st[0], false);
	url = url + "&resultsOnly=true";
	if (update){$("#Workspace").block();}
	$.ajax({
		url: url,
		dataType: "html",
		beforeSend: function(){
			showLoading(strat.frontId);
		},
		success: function(data){
			if (update) {
				ResultsToGrid(data, ignoreFilters);
				$("span#text_strategy_number").html(strat.JSON.name);
				$("span#text_step_number").html(step.frontId);
				$("span#text_strategy_number").parent().show();
				try {
					customResultsPage();
				}
				catch(err) {
					//Do nothing;
				}
				$("#Workspace").unblock();
			}
			removeLoading(strat.frontId);
		},
		error : function(data, msg, e){
			  alert("ERROR \n "+ msg + "\n" + e
                                      + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
		}
	});
}

function ResultsToGrid(data, ignoreFilters) {
        // the html() doesn't work in IE 7/8 sometimes (but not always.
        // $("div#Workspace").html(data);
	var oldFilters;
	if (ignoreFilters) {
		oldFilters = $("#Workspace div.layout-detail div.filter-instance .link-url");
	}

        document.getElementById('Workspace').innerHTML = data;

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
	var attrSelect = $("#addAttributes");
	if (attrSelect.length > 0) { 
		attrSelect.multiSelect({selectAll: false, noneSelected: '--- Add Column ---'},
				function() {
					addAttr($("#addAttributes").attr('commandUrl'));
				});
	}

	// convert results table to drag-and-drop flex grid
	$("#Results_Table").flexigrid({height : 'auto',
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
