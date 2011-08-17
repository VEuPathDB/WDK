/*
WDK Strategy System
resultsPage.js

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
        var step = $("div.selected");
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
    updateResultsPage($(table), url, false);
}

function updateAttrs(attrSelector, commandUrl) {
    var form = $(attrSelector);
    var selected = $("input:checked",form);
    var attributes = [];

    selected.each(function() {
        attributes.push(this.value);
    });
    var url = commandUrl + "&command=update&attribute=" + attributes.join("&attribute=");
    updateResultsPage(form, url, true);
}

function resetAttr(url, button) {
    if (confirm("Are you sure you want to reset the column configuration back to the default?")) {
        var url = url + "&command=reset";
    updateResultsPage($(button), url, true);
    }
}

function updateResultsPage(element, url, update) {
    if (element.parents("#strategy_results").length > 0) {
            GetResultsPage(url, update, true);
    }
    else {
        ChangeBasket(url + "&results_only=true");
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
    $("#" + currentDiv + " div.attributesList").hide();
    if (update){$("#" + currentDiv + " > div.Workspace").block();}
    $.ajax({
        url: url,
        dataType: "html",
        beforeSend: function(){
            if(strat != false) showLoading(strat.frontId);
        },
        success: function(data){
            if (update && ErrorHandler("Results", data, strat, null)) {
                ResultsToGrid(data, ignoreFilters, currentDiv);
                updateResultLabels(currentDiv, strat, step);
            }
            if(strat != false) removeLoading(strat.frontId);
        },
        error : function(data, msg, e){
              alert("ERROR \n "+ msg + "\n" + e
                                      + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
        }
    });
}

function updateResultLabels(currentDiv, strat, step) {
    if (currentDiv == 'strategy_results') {
        $("#" + currentDiv + "span#text_strategy_number").html(strat.JSON.name);
        $("#" + currentDiv + "span#text_step_number").html(step.frontId);
    }
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

    // convert results table to drag-and-drop flex grid
    createFlexigridFromTable($("#" + currentDiv + " .Results_Table"));

    // check the basket for the page if needed
    checkPageBasket();

    setDraggable($("#" + currentDiv + " > div.Workspace div.attributesList"), ".dragHandle");

    $("#" + currentDiv + " > div.Workspace").unblock();

	$("#" + currentDiv + " > div.Workspace").unblock();
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


function updatePageCount(element) {
    var advancedPaging = $(element).parent();
    var resultSize = parseInt($("input.resultSize",advancedPaging).val());
    var pageSize = $(".pageSize",advancedPaging).val();
    var pageCount = Math.ceil(resultSize / pageSize);
    if (pageCount * pageSize < resultSize) pageCount++;
    var span = $(".pageCount",advancedPaging);
    span.html(pageCount);
}

function gotoPage(element) {
    var advancedPaging = $(element).parent();
    var pageNumber = $(".pageNumber",advancedPaging).val();
    var pageSize = $(".pageSize",advancedPaging).val();

    var pageUrl = $(".pageUrl").val();
    
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

function openAttributeList(element){
    var button = $(element);

    var popup = button.next(".attributesList");    

    // Position the popup.
    var left = $(window).width()/2 - popup.width()/2 + $(window).scrollLeft();
    var top = $(window).scrollTop() - button.offset().top + 200;
    popup.css({'display' : 'block',
               'top' : top + 'px',
               'left' : left + 'px'});
}

function closeAttributeList(element){
    var button = $(element);
    
    var popup = button.parents(".attributesList");
    popup.hide();
}

function toggleAttributes(from) {
   var strList = $("#" + from + "-list").text();
   var list = strList.split(',');
   var state = $("#toggle-" + from).attr('checked');
   for(var i = 0; i < list.length; i++) {
      var name = list[i];
      if (name == '') continue;

      // look for the checkboxes with the attribute name, and toggle them
      var attribute = $(".Results_Pane .attributesList input#" + name);
      if (attribute.attr("disabled") == false)
         attribute.attr('checked', state);
   }
}


function openAttributePlugins(ele) {
    var plugins = $(ele).parents(".attribute-plugins").children(".plugins");
    // attach hover events on the plugin entry
    plugins.find(".plugin").hover(
        function() { $(this).addClass("highlight"); },
        function() { $(this).removeClass("highlight"); }
    );
    plugins.show();
}

function closeAttributePlugins(ele) {
    var plugins = $(ele).parents(".attribute-plugins").children(".plugins");
    plugins.hide();
}


function invokeAttributePlugin(ele, stepId, attributeName) {
        var pluginName = $(ele).attr("plugin");
        var title = $(ele).html();
        var plugins = $(ele).parents(".plugins");
	var url = "invokeAttributePlugin.do?step=" + stepId + "&attribute=" + attributeName + "&plugin=" + pluginName;	
	$.ajax({
		url: url,
		dataType: "html",
		beforeSend: function(){
                        plugins.hide();
			$("body").block();
		},
		success: function(data){
			// create a place holder for the result
			if ($("#attribute-plugin-result").length == 0)
				$("body").append("<div id=\"attribute-plugin-result\"> </div>");
			$("#attribute-plugin-result").html(data)
                            .dialog({ width : 750,
                                      maxHeight: 800,
                                      title : title
                                    });
                        $("body").unblock();
		},
                error: function() {
                        $("body").unblock();
                }
	});

}
