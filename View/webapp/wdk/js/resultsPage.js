/*
WDK Strategy System
resultsPage.js

Provides functions to support results table
*/

function configureSummaryViews(ele) {
    var workspace = window.wdk.findActiveWorkspace(); 
    var summaryViews = workspace.find("#Summary_Views");
    var currentTab = parseInt(summaryViews.children("ul").attr("currentTab"));    
    workspace.find("#Summary_Views").tabs({
        selected : currentTab,
        cache: true,
        ajaxOptions: {
            error: function( xhr, status, index, anchor ) {
               // alert( "Couldn't load this tab. Please try again later." + status );
            }
        }
    });
}

function summaryViewTabSelected(event, ui) {
/*
            var currentTab = getCurrentBasketTab();

            var currentDiv = getCurrentBasketRegion();
            currentDiv.prepend(jQuery("#basket-control-panel #basket-control").clone());

            // store the selection cookie
            var currentId = currentTab.attr("id");
            setCurrentTabCookie('basket', currentId);
            var control = jQuery("#basket-menu #basket-control");
            if (currentDiv.find("table").length > 0) {
                control.find("input#empty-basket-button").attr("disabled",false);
                control.find("input#make-strategy-from-basket-button").attr("disabled",false);
                control.find("input#export-basket-button").attr("disabled",false);
                // create multi select control for adding columns
                checkPageBasket();
                createFlexigridFromTable(jQuery("#basket-menu #Results_Table"));
                try {
                    customBasketPage();
                } catch(err) {
                    //Do nothing
                }
            } else {
                control.find("input#empty-basket-button").attr("disabled",true);
                control.find("input#make-strategy-from-basket-button").attr("disabled",true);
                control.find("input#export-basket-button").attr("disabled",true);
            }
*/
}



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
        var stratfId = step.parents(".diagram").attr("id");
        stratfId = stratfId.substring(stratfId.indexOf('_') + 1);
        strat = getStrategy(stratfId).backId;
        step = getStep(stratfId, stepfId).back_step_Id;
    }
    else {
        step = $(table).attr('step');
    }
    // build url.
    var info = $("#Summary_Views");
    var url = info.attr("updateUrl");
    url += "?strategy=" + strat + "&step=" + step + "&command=arrange&attribute=" + attr + "&left=" + left;
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
    
    // close the dialog
    form.parents(".attributesList").dialog("close");

    updateResultsPage(form, url, true);
}

function resetAttr(url, button) {
    if (confirm("Are you sure you want to reset the column configuration back to the default?")) {
        var url = url + "&command=reset";
    updateResultsPage($(button), url, true);
    }
}

function updateResultsPage(element, url, update) {
    // determine whether to refresh strategy result or basket result
    var tab = $("#strategy_tabs > li#selected > a").attr("id");
    if (tab == "tab_strategy_results") {
        GetResultsPage(url, update, true);
    } else if (tab == "tab_basket") {
        ChangeBasket(url + "&results_only=true");
    }
}

function sortResult(attribute, order) {
    var command = "command=sort&attribute=" + attribute + "&sortOrder=" + order;
    updateSummary(command);
}

function removeAttribute(attribute) {
    var command = "command=remove&attribute=" + attribute;
    updateSummary(command);
}

function updateSummary(command) {
    var info = $("#Summary_Views");
    var url = info.attr("updateUrl");
    var strategyId = info.attr("strategy");
    var stepId = info.attr("step");
    var strategy = getStrategyFromBackId(strategyId);
    var view = $("#Summary_Views > ul > li.ui-tabs-selected").attr("id");
    url += "?strategy=" + strategyId + "&strategy_checksum=" + strategy.checksum + "&step=" 
        + stepId + "&view=" + view + "&" + command;

    GetResultsPage(url, true, true, true);
}

function GetResultsPage(url, update, ignoreFilters, resultOnly){
    var s = parseUrlUtil("strategy", url);
    var st = parseUrlUtil("step", url);
    var strat = getStrategyFromBackId(s[0]);
    var currentDiv = window.wdk.findActiveView();
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
    currentDiv.find("div.attributesList").hide();
    if (update){currentDiv.block();}
    $.ajax({
        url: url,
        dataType: "html",
        beforeSend: function(){
            if(strat != false) showLoading(strat.frontId);
        },
        success: function(data){
            if (update && ErrorHandler("Results", data, strat, null)) {
                if (resultOnly == undefined)
                    resultOnly = (url.indexOf('showResult.do') >= 0);
               
                ResultsToGrid(data, ignoreFilters, currentDiv, resultOnly);
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
    if (currentDiv.hasClass('Workspace')) {
        currentDiv.find("span#text_strategy_number").html(strat.JSON.name);
        currentDiv.find("span#text_step_number").html(step.frontId);
    }
}

function ResultsToGrid(data, ignoreFilters, div, resultOnly) {
    var oldFilters;
    var currentDiv = div;
    if (currentDiv == undefined) currentDiv = window.wdk.findActiveView();
    if (ignoreFilters) {
        oldFilters = $("#strategy_results > div.Workspace div.layout-detail div.filter-instance .link-url");
    }

    currentDiv.html(data);

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
    createFlexigridFromTable(currentDiv.find(" .Results_Table"));

    // check the basket for the page if needed
    checkPageBasket();

    setDraggable(currentDiv.find("div.attributesList"), ".dragHandle");

    currentDiv.unblock();
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
	$("#" + getDialogId(element, "advanced-paging")).dialog("open");
}

function closeAdvancedPaging(submitObj) {
	$(submitObj).parents(".advanced-paging").dialog("close");
}

function openAttributeList(element){
    $("#" + getDialogId(element, "attributesList")).dialog("open");
}

function getDialogId(element, dialogClass) {
	var list = $(element).next("." + dialogClass);
    if (list.length > 0) {
        var id = "dialog" + Math.floor(Math.random() * 1000000000);
        $(element).attr("dialog", id);
        list.attr("id", id).dialog({
            autoOpen: false
        });
    }
    return $(element).attr("dialog");
}

function toggleAttributes(from) {
   var strList = $("#" + from + "-list").text();
   var list = strList.split(',');
   var state = $("#toggle-" + from).attr('checked');
   for(var i = 0; i < list.length; i++) {
      var name = list[i];
      if (name == '') continue;

      // look for the checkboxes with the attribute name, and toggle them
      var attribute = $(".Results_Div .attributesList input#" + name);
      if (attribute.attr("disabled") == false)
         attribute.attr('checked', state);
   }
}

/* This code should be removed as soon as we get approval for the alternate
 * solution.  If you find this code later than 4/2012, please delete.
function assignAttributePluginTip(selector) {
    $(selector)
      .qtip({
    	  position: {
	        my: 'top-right',
	        at: 'bottom-center'
	      },
          show: {
              solo: true,
              event: 'click'
          },
          hide: {
              event: 'click'
          },
          events: {
              show: setHideTipEvents
          }
      })
      .removeData('qtip') // clears data from target so second tooltip can be applied
      .qtip({
    	  position: {
  	        my: 'top-right',
  	        at: 'bottom-center'
  	      },
          content: '<h4>Analyze/Graph the contents of this column</h4>',
          show: { solo: true }
      });
}
*/

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
            $("#attribute-plugin-result")
                .html(data)
                .dialog({ width : 825,
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
