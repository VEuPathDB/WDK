/*

});
WDK Strategy System
resultsPage.js

Provides functions to support results table
*/

wdk.util.namespace("window.wdk.resultsPage", function(ns, $) {
  "use strict";

  function configureSummaryViews($element, $attrs) {
    // var currentTab = parseInt($element.children("ul").attr("currentTab"), 10);  
    var currentTab = 0;
    $element.tabs({
      active : currentTab,
      load: function(event, ui) {
        wdk.load();
        createFlexigridFromTable(ui.panel.find(".Results_Table"));
      }
    });
    
    // pretty up analysis tabs and create "new analysis" menu
    wdk.stepAnalysis.configureAnalysisViews($element);
    
    // var workspace = window.wdk.findActiveWorkspace(); 

    // $(".Summary_Views").each(function() {
    //   var summaryViews = $(this);
    //   // disable remembering current tab for now due to performance issue on
    //   // loading the genomic summary view. Always use the first tab.
    //   // var currentTab = parseInt(summaryViews.children("ul").attr("currentTab"), 10);  
    //   var currentTab = 0;
    //   summaryViews.tabs({

    //     active : currentTab,
    //     load: function(event, ui) {
    //       wdk.load();
    //       createFlexigridFromTable(ui.panel.find(".Results_Table"));
    //     }
    //   });
    // });
  }
  
  function moveAttr(col_ix, table) {
    // Get name of target attribute & attribute to left (if any)
    // NOTE:  Have to convert these from frontId to backId!!!
    var headers = $("tr.headerrow th", table);
    var attr = $(headers[col_ix]).attr("id");
    var left,
        strat,
        step;
    if (col_ix > 0) left = $(headers[col_ix-1]).attr("id");
    // Figure out what step/strategy is currently displayed in results panel
    if ($(table).parents("#strategy_results").length > 0) {
      step = $("div.selected");
      var stepfId = step.attr("id").split('_')[1];
      var stratfId = step.parents(".diagram").attr("id");
      stratfId = stratfId.substring(stratfId.indexOf('_') + 1);
      strat = wdk.strategy.model.getStrategy(stratfId).backId;
      step = wdk.strategy.model.getStep(stratfId, stepfId).back_step_Id;
    } else {
      step = $(table).attr('step');
    }
    // build url.
    var info = $("#Summary_Views");
    var view = $("#Summary_Views > ul > li.ui-tabs-active").attr("id");
    var url = info.attr("updateUrl") +
        "?strategy=" + strat + "&step=" + step + "&command=arrange" +
        "&attribute=" + attr + "&left=" + left + "&view=" + view;
    updateResultsPage($(table), url, false);
  }

  function updateAttrs(attrSelector, commandUrl) {
    var form = $(attrSelector).parents().parents("form");
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
      url += "&command=reset";
      updateResultsPage($(button), url, true);
    }
  }

  function updateResultsPage(element, url, update) {
    // determine whether to refresh strategy result or basket result
    var tab = $("#strategy_tabs > li#selected > a").attr("id");
    if (tab == "tab_strategy_results") {
      GetResultsPage(url, update, true);
    } else if (tab == "tab_basket") {
      wdk.basket.ChangeBasket(url + "&results_only=true");
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
    // var info = workspace("#Summary_Views");
    var info = wdk.findActiveWorkspace().find("#Summary_Views");;
    var url = info.attr("updateUrl");
    var strategyId = info.attr("strategy");
    var stepId = info.attr("step");
    var strategy = wdk.strategy.model.getStrategyFromBackId(strategyId);
    var view = $("#Summary_Views > ul > li.ui-tabs-active").attr("id");
    url += "?strategy=" + strategyId + "&strategy_checksum=" +
        strategy.checksum + "&step=" + stepId + "&view=" + view + "&" + command;
    GetResultsPage(url, true, true, true);
  }

  function GetResultsPage(url, update, ignoreFilters, resultOnly) {
    var s = wdk.util.parseUrlUtil("strategy", url);
    var st = wdk.util.parseUrlUtil("step", url);
    var strat = wdk.strategy.model.getStrategyFromBackId(s[0]);
    var currentDiv = window.wdk.findActiveView();
    var step = null;
    if (strat == false) {
      strat = new Object();
      strat.JSON = new Object();
      step = new Object();
      strat.JSON.name = "";
      step.frontId = "n/a";
    } else {
      step = strat.getStep(st[0], false);
    }
    url = url + "&resultsOnly=true";
    currentDiv.find("div.attributesList").hide();
    if (update) currentDiv.block();
    $.ajax({
      url: url,
      dataType: "html",
      beforeSend: function() {
        if(strat != false) wdk.util.showLoading(strat.frontId);
      },
      success: function(data) {
        if (update && wdk.strategy.error.ErrorHandler("Results", data, strat, null)) {
          if (resultOnly == undefined) {
            resultOnly = (url.indexOf('showResult.do') >= 0);
          }
          ResultsToGrid(data, ignoreFilters, currentDiv, resultOnly);
          updateResultLabels(currentDiv, strat, step);
        }
        if(strat != false) wdk.util.removeLoading(strat.frontId);
      },
      error : function(data, msg, e) {
        alert("ERROR \n "+ msg + "\n" + e + ". \n" +
            "Reloading this page might solve the problem. \n" +
            "Otherwise, please contact site support.");
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

    // invoke filters
    var wdkFilter = new wdk.filter.WdkFilter();
    
    if (ignoreFilters) {
      wdkFilter.addShowHide();
      wdkFilter.displayFilters();
      oldFilters.each(function() {
        var newFilter = document.getElementById(this.id);
        var count = $(this).text();

        // no need to update if nodes are the same
        // refs #15011
        if (this === newFilter) return;

        if (count == 0 || !/\d+/.test(count)) {
          $(newFilter).replaceWith(this);
        } else {
          $(newFilter).html(count);
        }
      });
    } else {
      //wdkFilter.initialize();
      // Using setTimeout allows the results HTML to be rendered first, and
      // thus the results ajax is fired before the filters ajax. This will make
      // getting results faster when there are lots of filters.
      setTimeout(wdkFilter.initialize.bind(wdkFilter), 0);
    }

    // convert results table to drag-and-drop flex grid
    createFlexigridFromTable(currentDiv.find(" .Results_Table"));  // moved to tab load success callback

    // check the basket for the page if needed
    wdk.basket.checkPageBasket();

    wdk.util.setDraggable(currentDiv.find("div.attributesList"), ".dragHandle");

    currentDiv.unblock();
  }

  function createFlexigridFromTable(table) {
    table.flexigrid({
      height : 'auto',
      showToggleBtn : false,
      useRp : false,
      singleSelect : true,
      onMoveColumn : moveAttr,
      nowrap : false,
      resizable : false
    });
  }

  function updatePageCount(element) {
    var advancedPaging = $(element).parent();
    var resultSize = parseInt($("input.resultSize",advancedPaging).val(), 10);
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

    var pageUrl = $(".pageUrl", advancedPaging).val();
    
    var pageOffset = (pageNumber - 1) * pageSize;
    var gotoPageUrl = pageUrl.replace(/\&pager\.offset=\d+/, "");
    gotoPageUrl = gotoPageUrl.replace(/\&pageSize=\d+/, "");
    gotoPageUrl += "&pager.offset=" + pageOffset;
    gotoPageUrl += "&pageSize=" + pageSize;
    $("div.advanced-paging").hide();
    GetResultsPage(gotoPageUrl, true, true);
  }

  function openAttributeList(element){
    openBlockingDialog("#" + getDialogId(element, "attributesList"));
  }

  function openAdvancedPaging(element){
    openBlockingDialog("#" + getDialogId(element, "advanced-paging"));
  }

  function closeAdvancedPaging(submitObj) {
    $(submitObj).parents(".advanced-paging").dialog("close");
  }

  function openBlockingDialog(selector) {
    $(selector).dialog({ modal: true });
    $(selector).dialog("open");
  }

  function getDialogId(element, dialogClass) {
    var list = $(element).next("." + dialogClass);
    if (list.length > 0) {
      var id = "dialog" + Math.floor(Math.random() * 1000000000);
      $(element).attr("dialog", id);
      list.attr("id", id).dialog({
        autoOpen: false,
        open: function(event, ui) {
          var $this = $(this);
          var dialogHeight = $this.height();
          var viewportHeight = $(window).height();
          $this.dialog("option", "height",
              dialogHeight > viewportHeight ? viewportHeight - 36 : "auto");
          $this.dialog("option", "width", list.outerWidth() + 20);
        }
      });
    }
    return $(element).attr("dialog");
  }

  function toggleAttributes(from) {
    var strList = $("#" + from + "-list").text();
    var list = strList.split(',');
    var state = $("#toggle-" + from).attr('checked');
    for (var i = 0; i < list.length; i++) {
      var name = list[i];
      if (name == '') continue;

      // look for the checkboxes with the attribute name, and toggle them
      var attribute = $(".Results_Div .attributesList input#" + name);
      if (attribute.attr("disabled") == false) {
        attribute.attr('checked', state);
      }
    }
  }

  function invokeAttributePlugin(ele, stepId, attributeName) {
    var pluginName = $(ele).attr("plugin");
    var title = $(ele).attr("plugintitle");
    var url = "invokeAttributePlugin.do?step=" + stepId +
        "&attribute=" + attributeName + "&plugin=" + pluginName;  
    $.ajax({
      url: url,
      dataType: "html",
      beforeSend: function() {
        $.blockUI();
      },
      success: function(data) {
        $.unblockUI();
        // create a place holder for the result
        if ($("#attribute-plugin-result").length == 0) {
          $("body").append("<div id=\"attribute-plugin-result\"> </div>");
        }
        $("#attribute-plugin-result")
            .html(data)
            .dialog({
              width : 825,
              maxHeight: 800,
              title : title,
              modal : true
            });
      }
    });
  }

  ns.GetResultsPage = GetResultsPage;
  ns.ResultsToGrid = ResultsToGrid;
  ns.closeAdvancedPaging = closeAdvancedPaging;
  ns.configureSummaryViews = configureSummaryViews;
  ns.createFlexigridFromTable = createFlexigridFromTable;
  ns.gotoPage = gotoPage;
  ns.invokeAttributePlugin = invokeAttributePlugin;
  ns.openAdvancedPaging = openAdvancedPaging;
  ns.openAttributeList = openAttributeList;
  ns.removeAttribute = removeAttribute;
  ns.resetAttr = resetAttr;
  ns.sortResult = sortResult;
  ns.updateAttrs = updateAttrs;
  ns.updatePageCount = updatePageCount;
  ns.updateResultLabels = updateResultLabels;
  
});
