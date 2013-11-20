/**
 * This file contains functions used to communicate strategy operations between
 * the client and the server.
 *
 * The current contract between client and server is as follows:
 *  - Client sends a request to the server to perform a strategy operation.
 *  - Server fulfills request and returns a new state object
 *  - Client resolves differences between new state object and local state
 *    object.
 *  - Client calls updateStrategies function which uses the resolved local
 *    state object to determine if a strategy's UI should be redrawn. The
 *    strategy's checksum is used to determine this action: if the checksum
 *    for a strategy changes between requests, it is redrawn.
 *
 */

wdk.util.namespace("window.wdk.strategy.controller", function (ns, $) {
  "use strict";

  ns.state = null;
  ns.strats = {};
  ns.stateString = '';
  ns.sidIndex = 0;

  function init(element, attrs) {
    // Selects the last step of the first strategy
    wdk.step.init();

    // Make the strategies window resizable
    element.find(".resizable-wrapper").resizable({
      handles: 's',
      minHeight: 150,
      stop: function(event, ui) {
        wdk.stratTabCookie.setCurrentTabCookie('strategyWindow', $(".resizable-wrapper").height())
      }
    });

    // tell jQuery not to cache ajax requests.
    // generic error handling of ajax calls
    $.ajaxSetup ({
      cache: false,
      timeout: 1000 * 60 * 5, // was 180000 ms
      error: function(data, msg, e) {
        if (msg == "timeout") {
          var c = confirm("This request has timed out.\n" +
              "Would you like to try again? (This request will timeout after " +
              ((this.timeout / 60000) + 1) +" minutes.)");
          if (c) {
            this.timeout = this.timeout + 60000;
            $.ajax(this);
          } else {
            if (this.url.indexOf("showSummary.do") != -1) {
              wdk.util.removeLoading();
            } else {
              initDisplay();
            }
          }
        } else if (data.readyState != 0 && data.status != 0) {
          // not timeout, backend throws errors
          try {
            customShowError();
          } catch(e) {
            alert("controller.js: a backend error occurred.");
          }
          if (this.url.indexOf("showSummary.do") != -1) {
            wdk.util.removeLoading();
          } else {
            //initDisplay();
          }
        }
      }
    });

    // remove tabs that don't have an associated jsp
    initStrategyTabs();

    // init DYK and show strategy tab
    wdk.addStepPopup.showPanel(chooseStrategyTab(attrs.allCount, attrs.openCount));

    // get strategies json from server and draw strategies ui
    initDisplay();

    // strategyselect event fired when a step in a strategy is selected
    $("#Strategies").on("strategyselect", ".diagram", function(e, strategy) {
      // hide editable strategy names and reset trigger
      $(e.delegateTarget).find(".strategy-name.wdk-editable")
      .editable("hide");
      // .editable("option", "trigger", "click");

      if (strategy.Steps.length > 1 && !strategy.hasCustomName()) {
        // show and turn off trigger
        $(this).find(".strategy-name.wdk-editable")
        .editable("show");
        // .editable("option", "trigger", "manual")
        // .on("editablehide", function() {
        //   console.log(this);
        //   $(this).editable("option", "trigger", "click");
        // });;
      }
    });
  }

  /**
   * Returns the name of the tab to open based on all and open counts
   */
  function chooseStrategyTab(allCount, openCount) {
    var openTabName = 'strategy_results';
    var allTabName = 'search_history';
    var current = wdk.stratTabCookie.getCurrentTabCookie('application');

    if (!current || current == null) {
      // no cookie set
      return (openCount > 0 || allCount == 0 ? openTabName : allTabName);
    }
    else {
      // cookie set
      if (current == allTabName) {
        return (allCount > 0 ? allTabName : openTabName);
      }
      else if (current == openTabName) {
        return (openCount > 0 || allCount == 0 ? openTabName : allTabName);
      }
      else {
        return current;
      }
    }
  }

  /**
   * For each tab, if associated JSP exists, call custom function if defined.
   * Otherwise, remove the tab.
   *
   * TODO - we should make this a configuration option.
   * Then users can name the files whatever they want
   * and we don't have to perform unnecessary HTTP requests
   * if nothing is configured.
   */
  function initStrategyTabs() {
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
        wdk.dyk.initHelp();
      }
    });
  }

  /**
   * Get state info from server and call updateStrategies
   */
  function initDisplay(){
    $.ajax({
      url: "showStrategy.do",
      type: "POST",
      dataType: "json",
      data:"state=",
      beforeSend: function() {
        // it doesn't make sense for this to be in util
        wdk.util.showLoading();
      },
      success: function(data) {
        updateStrategies(data);
      }
    });
  }

  /**
   * Update the namespace-attached `strats` array
   *
   * The state is attached to the namespace. Then, strategies are removed based
   * on the state object. Then, strategies are loaded (see @loadModel) if the
   * strategy is not loaded, or the checksum of the strategy has changed.
   *
   * @param {Object} data Object retreived from server with state information
   * @param {Boolean} ignoreFilters If `true` filters will not be reloaded.
   *   Otherwise they will be reloaded.
   * @returns {jQuery.Deffered} Promise
   */
  function updateStrategies(data, ignoreFilters) {
    var deferred = $.Deferred();

    ns.state = data.state;
    ns.stateString = JSON.stringify(ns.state);
    removeClosedStrategies();
    for (var newOrdering in ns.state) {
      if (newOrdering == "count") {
        // it appears the span was removed, so this code does nothing.
        $("#mysearch span").text('My Strategies ('+ns.state[newOrdering]+')');
      } else if (newOrdering != "length") {
        var strategyId = ns.state[newOrdering].id;
        if (wdk.strategy.model.isLoaded(strategyId)) {
          if (wdk.strategy.model.getStrategyFromBackId(ns.state[newOrdering].id).checksum !=
              ns.state[newOrdering].checksum) {
            loadModel(data.strategies[ns.state[newOrdering].checksum], newOrdering);
          }
        } else {
          loadModel(data.strategies[ns.state[newOrdering].checksum], newOrdering);
        }
      }
    }
    showStrategies(data.currentView, ignoreFilters, data.state.length, deferred);
    return deferred.promise();
  }

  /**
   * Remove strategies from, and reorders, the namespace-attached `strats`
   *  array.
   */
  function removeClosedStrategies(){
    for (var currentOrder in ns.strats) {
      if (currentOrder.indexOf(".") == -1) {
        var removeTopStrategy = true;
        for (var newOrder in ns.state) {
          if (newOrder != "length") {
            if (ns.strats[currentOrder].checksum == ns.state[newOrder].checksum) {
              removeTopStrategy = false;
              if (newOrder != currentOrder) {
                ns.strats[newOrder] = ns.strats[currentOrder];
                removeSubStrategies(currentOrder, newOrder);
                delete ns.strats[currentOrder];
                break;
              }
            } else if(ns.strats[currentOrder].backId == ns.state[newOrder].id) {
              removeTopStrategy = false;
              removeSubStrategies(currentOrder, newOrder);
              if (newOrder != currentOrder) {
                ns.strats[newOrder] = ns.strats[currentOrder];
                break;
              }
            }
          }
        }
        if (removeTopStrategy) {
          removeSubStrategies(currentOrder);
          delete ns.strats[currentOrder];
          wdk.history.update_hist(true); //set update flag for history if anything was closed.
        }
      }
    }
  }

  /**
   * Remove substrategies from `strats`. If `newOrder` is defined, then the
   * substrategy's parent stategy order is updated. Otherwise, the substrategy
   * is removed.
   *
   * @param {Number} currentOrder The stategy's current order
   * @param {Number} newOrder The strategy's new order
   */
  function removeSubStrategies(currentOrder, newOrder){
    for (var order in ns.strats) {
      if (order.split(".").length > 1 && order.split(".")[0] == currentOrder) {
        if (newOrder == undefined) {
          delete ns.strats[order];
        } else {
          var n_ord = order.split(".");
          n_ord[0] = newOrder;
          n_ord = n_ord.join(".");
          ns.strats[n_ord] = ns.strats[order];
          delete ns.strats[order];
        }
      }
    }
  }

  /**
   * Insert stategy HTML into DOM.
   *
   * @param {Object} view Current strategy, step, and results offset retrieved
   *    from server.
   * @param {Boolean} ignoreFilters If `true`, don't reload filters; otherwise
   *    reload filters.
   * @param {Number} count The number of open strategies
   * @param {Object} jQuery.Deffered object used to allow promise chaining for
   *  updating strategies. This adds complexity and will probably removed in
   *  favor of event triggering.
   */
  function showStrategies(view, ignoreFilters, count, deferred){
    $("#tab_strategy_results font.subscriptCount").text("(" + count + ")");
    var sC = 0;
    for (var s in ns.strats) {
      if (s.indexOf(".") == -1) {
        sC++;
      }
    }
    var s2 = document.createElement('div');
    for (var t=1; t<=sC; t++) {
      $(s2).prepend(ns.strats[t].DIV);
      displayOpenSubStrategies(ns.strats[t], s2);
    }
    $("#strategy_messages").hide();
    $("#strategy_results .resizable-wrapper:has(#Strategies)").show();
    $("#Strategies").html($(s2).html());
    var height = wdk.stratTabCookie.getCurrentTabCookie('strategyWindow');
    var wrapper = $("#strategy_results .resizable-wrapper:has(#Strategies)");
    if (!height && $("#Strategies").parent().parent().height() > 330) {
      // unless otherwise specified, don't allow height > 330
      wrapper.height(330);
    } else if (height) {
      height = parseInt(height, 10);
      if (wrapper.resizable("option", "minHeight") <= height) {
        // shrink wrapper to specified height only if no less than minHeight
        wrapper.height(height);
      } else if ($("#Strategies").height() + 10 < wrapper.height()) {
        // shrink wrapper to fit Strategies
        wrapper.height($("#Strategies").height() + 10);
      }
    }
    if (view.action != undefined) {
      if (view.action == "share" || view.action == "save") {
        var x = $("a#" + view.action + "_" + view.actionStrat);
        x.click();
      }      
    }
    if (view.strategy != undefined || view.step != undefined) {
      var initStr = wdk.strategy.model.getStrategyFromBackId(view.strategy);
      var initStp = initStr.getStep(view.step, false);
      if (initStr == false || initStp == null) {
        NewResults(-1);
      } else {
        var isVenn = (initStp.back_boolean_Id == view.step);
        var pagerOffset = view.pagerOffset;
        if (view.action != undefined && view.action.match("^basket")) {
          NewResults(initStr.frontId, initStp.frontId, isVenn, pagerOffset,
              ignoreFilters, view.action, deferred);
        } else {
          NewResults(initStr.frontId, initStp.frontId, isVenn, pagerOffset,
              ignoreFilters, null, deferred);
        }
      }
    } else {
      NewResults(-1);
    }
    if (sC == 0) showInstructions();
    // add fancy tooltips
    wdk.tooltips.assignTooltips(".filterImg", 0);
    wdk.tooltips.assignTooltips(".step-elem", 0);
  }

  /**
   * Insert substrategies into DOM and add colored border arround them and
   * associated steps in parent strategies.
   *
   * @param {Object} strategy Parent strategy
   * @param {Object} div DOM node
   */
  function displayOpenSubStrategies(strategy, div) {
    //Colors for expanded substrategies
    var indent = 20;
    var colors = [
      {step:"#A00000", top:"#A00000", right:"#A00000", bottom:"#A00000", left:"#A00000"},
      {step:"#A0A000", top:"#A0A000", right:"#A0A000", bottom:"#A0A000", left:"#A0A000"},
      {step:"#A000A0", top:"#A000A0", right:"#A000A0", bottom:"#A000A0", left:"#A000A0"},
      {step:"#00A0A0", top:"#00A0A0", right:"#00A0A0", bottom:"#00A0A0", left:"#00A0A0"},
      {step:"#0000A0", top:"#0000A0", right:"#0000A0", bottom:"#0000A0", left:"#0000A0"}
    ];
    var sCount = 0;
    var subs;
    var j;
    for (j in strategy.subStratOrder) {
      sCount++;
    }
    for (j=1; j<=sCount; j++) {
      subs = wdk.strategy.model.getStrategy(strategy.subStratOrder[j]);
      subs.color = parseInt(strategy.getStep(wdk.strategy.model.getStrategy(strategy.subStratOrder[j]).backId.split("_")[1],false).frontId, 10) % colors.length;
      $(subs.DIV).addClass("sub_diagram").css({
        "margin-left": (subs.depth(null) * indent) + "px",
        "border-color": colors[subs.color].top+" "+colors[subs.color].right+" "+colors[subs.color].bottom+" "+colors[subs.color].left
      });
      $("div#diagram_" + strategy.frontId + " div#step_" + strategy.getStep(wdk.strategy.model.getStrategy(strategy.subStratOrder[j]).backId.split("_")[1],false).frontId + "_sub", div).css({"border-color":colors[subs.color].step});
      $("div#diagram_" + strategy.frontId, div).after(subs.DIV);
      if (wdk.strategy.model.getSubStrategies(strategy.subStratOrder[j]).length > 0) {
        displayOpenSubStrategies(wdk.strategy.model.getStrategy(strategy.subStratOrder[j]),d);
      }
    }
  }

  function showInstructions() {
    $("#strategy_messages").empty();
    //$("#strat-instructions").remove();
    //$("#strat-instructions-2").remove();
    //var instr = document.createElement('div');
    //var id = "strat-instructions";
    //if ($("#tab_strategy_new").length > 0) id = "strat-instructions-2"
    //$(instr).attr("id",id).html(getSimpleInstructionsHtml());
    var instr = getSimpleInstructionsHtml();
    $("#strategy_messages").append(instr);
    $("#strategy_results .resizable-wrapper:has(#Strategies)").hide();
    $("#strategy_messages").show();
  }

  /* FIXME: probably want to eventually fix this to use 'single-arrow' markup below to
   *   tell users what to do if there are no open strategies AND there is no 'new' tab. */
  function getSimpleInstructionsHtml() {
    // if 'new' tab doesn't exist, then don't display fancy instructions with arrows
    var openTabContents = '<div style="font-size:120%;line-height:1.2em;text-indent:10em;padding:0.5em">' +
        'You have no open strategies.  Please run a search to start a strategy.' +
        '<p style="text-indent:10em">To open an existing strategy, visit the ' +
        '<a href=\"javascript:wdk.addStepPopup.showPanel(\'search_history\')">\'All\' tab</a>.</p></div>';
    return openTabContents;
  }

  function getInstructionsHtml() {
    var arrow_image = "<img id='bs-arrow' alt='Arrow pointing to Browse Strategy Tab' src='" + wdk.assetsUrl('/wdk/images/lookUp2.png') + "' width='45px'/>"; 
    if ($("#tab_strategy_new").length > 0) {
      arrow_image = "<img id='ns-arrow' alt='Arrow pointing to New Search Button' src='" + wdk.assetsUrl('/wdk/images/lookUp.png') + "' width='45px'/>" + arrow_image;
    }
    
    arrow_image += getInstructionsText();
    return arrow_image;
  }

  function getInstructionsText() {
    var instr_text = "<p style='width: 85px; position: absolute; padding-top: 14px;'>Run a new search to start a strategy</p>";
    if ($("#tab_strategy_new").length > 0) {
      instr_text = "<p style='width: 85px; position: absolute; left: 12px; padding-top: 14px;'>Click '<a href=\"javascript:wdk.addStepPopup.showPanel('strategy_new')\">New</a>' to start a strategy</p>";
    }
    var instr_text2 = "<p style='width: 85px; position: absolute; right: 12px; padding-left: 1px;'>Or Click on '<a href=\"javascript:wdk.addStepPopup.showPanel('search_history')\">All</a>' to view your strategies.</p>";
    return instr_text + "<br>" + instr_text2;
  }

  /**
   * Instantiate Strategy object.
   *
   * @param {Object} json Strategy object retreived from server.
   * @param {Number} ord The order in which to display the strategy.
   */
  function loadModel(json, ord) {
    wdk.history.update_hist(true); //set update flag for history if anything was opened/changed.
    var strategy = json;
    var strat = null;
    if (!wdk.strategy.model.isLoaded(strategy.id)) {
      strat = new wdk.strategy.model.Strategy(ns.sidIndex, strategy.id, true);
      ns.sidIndex++;
    } else {
      strat = wdk.strategy.model.getStrategyFromBackId(strategy.id);
      strat.subStratOrder = {};
    }    
    if (strategy.importId != "") {
      strat.isDisplay = true;
      strat.checksum = ns.state[ord].checksum;
    } else {
      var prts = strat.backId.split("_");
      strat.subStratOf = wdk.strategy.model.getStrategyFromBackId(prts[0]).frontId;
      if (strategy.order > 0) {
        strat.isDisplay = true;
      }
    }
    strat.JSON = strategy;
    strat.isSaved = strategy.saved;
    strat.isPublic = strategy.isPublic;
    strat.isValid = strategy.isValid;
    strat.name = strategy.name;
    strat.description = strategy.description;
    strat.importId = strategy.importId;
    ns.strats[ord] = strat;
    strat.initSteps(strategy.steps, ord);
    strat.dataType = strategy.steps[strategy.steps.length].dataType;
    strat.displayType = strategy.steps[strategy.steps.length].displayType; //corresponds with record displayName in model e.g. Metabolic Pathway (singular always)
    strat.nonTransformLength = strategy.steps.nonTransformLength;
    strat.DIV = wdk.strategy.view.displayModel(strat);
    return strat.frontId;
  }

  function unloadStrategy(id) {
    for (var s in ns.strats) {
      s = parseInt(s, 10);
      if (ns.strats[s].frontId == id) {
        delete ns.strats[s];
        return;
      }
    }
  }

  /**
   * Display results for a particular step.
   *
   * Retrieve results from server as HTML and insert into workspace.
   *
   * @param {Number} f_strategyId Strategy order/frontID
   * @param {Number} f_stepId Step order/frontID
   * @param {Boolean} bool If Step is a Boolean Step set to `true`; else `false`
   * @param {Number} pagerOffset Results offset
   * @param {Boolean} ignoreFilters If `true` don't reload filters; else reload
   *    filters
   * @param {String} action Action to trigger once results are loaded.
   * @param {Object} deferred jQuery.Deffered object created in `updateStrategies`
   */
  function NewResults(f_strategyId, f_stepId, bool, pagerOffset, ignoreFilters,
      action, deferred) {

    if (f_strategyId == -1) {
      // don't show any results
      $("#strategy_results > div.Workspace").html("");
      wdk.addStepPopup.current_Front_Strategy_Id = null;
      return;
    }

    wdk.addStepPopup.current_Front_Strategy_Id = f_strategyId;

    var strategy = wdk.strategy.model.getStrategy(f_strategyId);
    var step = strategy.getStep(f_stepId, true);
    var url = "showSummary.do";
    var data = {
      strategy: strategy.backId,
      step: step.back_step_Id,
      resultsOnly: true,
      strategy_checksum: (strategy.checksum != null) ? strategy.checksum :
          wdk.strategy.model.getStrategy(strategy.subStratOf).checksum,
      bool: (bool) ? step.back_boolean_Id : null
    };

    if (!pagerOffset) {
      data.noskip = 1;
    } else { 
      data.pager = { offset: pagerOffset };
    }

    return $.ajax({
      url: url,
      dataType: "html",
      type: "post",
      data: data,
      beforeSend: function() {
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        var $Strategies = $("#Strategies");
        var oldSelectedStrategyId = $Strategies.find(".diagram").has(".selected").attr("id");
        var oldSelectedStepId = $Strategies.find(".selected").attr("id");

        step.isSelected = true;
        if (wdk.strategy.error.ErrorHandler("Results", data, strategy,
            $("#diagram_" + strategy.frontId + " step_" + step.frontId +
                "_sub div.crumb_details div.crumb_menu a.edit_step_link"))
        ) {
          // unselect previously selected step
          $Strategies.find(".selected").removeClass("selected");

          var init_view_strat = strategy.backId;
          var init_view_step;

          if (bool) {
            $("#Strategies div#diagram_" + strategy.frontId + " div[id='step_" +
                step.frontId + "']").addClass("selected");
            init_view_step = step.back_step_Id + ".v";
          } else {
            $("#Strategies div#diagram_" + strategy.frontId + " div[id='step_" +
                step.frontId + "_sub']").addClass("selected");
            init_view_step = step.back_step_Id;
          }

          // insert results HTML into DOM
          wdk.resultsPage.ResultsToGrid(data, ignoreFilters, $("#strategy_results .Workspace"));
          // update results pane title
          wdk.resultsPage.updateResultLabels($("#strategy_results .Workspace"), strategy, step);
          
          // remember user's action, if user is not logged in,
          // and tries to save, this place holds the previous
          // action the user was doing.
          var linkToClick = $("a#" + action);
          if (linkToClick.length > 0) {
            linkToClick.click();
          }

          // trigger custom events
          // in this case, select events
          var $selectedStrategy = $("#Strategies .diagram").has(".selected");
          var $selectedStep = $("#Strategies .diagram").find(".selected");

          if ($selectedStrategy.attr("id") !== oldSelectedStrategyId) {
            $selectedStrategy.trigger("strategyselect", [strategy])
          }

          if ($selectedStep.attr("id") !== oldSelectedStepId) {
            $selectedStep.trigger("stepselect", [step]);
          }

        }

        wdk.util.removeLoading(f_strategyId);
        wdk.basket.checkPageBasket();
        $.cookie("refresh_results", "false", { path : '/' });
      }
    }).then(function() {
      wdk.load();

      if (deferred) {
        deferred.resolve()
      }
    });
  }

  //===========================================================================
  //  =Actions
  //
  //  The following functions make xhr calls to the server. The server returns
  //  a state object that is used by the function updateStrategies which will
  //  update the local state (strats) and redraw the UI.
  //===========================================================================

  function RenameStep(ele, s, stp) {
    var new_name = $(ele).val();
    var step = wdk.strategy.model.getStep(s, stp);
    var url = "renameStep.do?strategy=" + wdk.strategy.model.getStrategy(s).backId +
        "&stepId=" + step.back_step_Id + "&customName=" +
        encodeURIComponent(new_name);
    $.ajax({
      url: url,
      dataType: "html",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        wdk.util.showLoading(s);
      },
      success: function(data) {
        data = eval("(" + data + ")");
        if (wdk.strategy.error.ErrorHandler("RenameStep", data, wdk.strategy.model.getStrategy(s), null)) {
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(s);
        }
      }
    });
  }

  // will be replaced by wizard
  function AddStepToStrategy(url, proto, stpId) {
    var strategy = wdk.strategy.model.getStrategyFromBackId(proto);
    var b_strategyId = strategy.backId;
    var f_strategyId = strategy.frontId;
    var cs = strategy.checksum;
    if (strategy.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strategy.subStratOf).checksum;
    }
    url = url + "&strategy_checksum="+cs;
    var d = wdk.util.parseInputs();
    $.ajax({
      url: url,
      type: "POST",
      dataType: "json",
      data: d + "&state=" + ns.stateString,
      beforeSend: function(){
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("AddStep", data, strategy, $("div#query_form"))) {
          if ($("div#query_form").css("display") == "none") {
            $("div#query_form").remove();
          }
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(f_strategyId);
        }
      }
    });
    wdk.step.isInsert = "";
    wdk.addStepPopup.closeAll(true);
  }

  function EditStep(url, proto, step_number){
    var ss = wdk.strategy.model.getStrategyFromBackId(proto);
    var sss = ss.getStep(step_number, false);
    var d = wdk.util.parseInputs();
    var cs = ss.checksum;
    if (ss.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(ss.subStratOf).checksum;
    }
    url = url+"&strategy_checksum="+cs;
    $.ajax({
      url: url,
      type: "POST",
      dataType:"json",
      data: d + "&state=" + ns.stateString,
      beforeSend: function(obj){
        wdk.addStepPopup.closeAll(true);
        wdk.util.showLoading(ss.frontId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("EditStep", data, ss, $("div#query_form"))) {
          $("div#query_form").remove();
          wdk.step.hideDetails();
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(ss.frontId);
        }
      }
    });
  }

  function DeleteStep(f_strategyId,f_stepId) {
    var strategy = wdk.strategy.model.getStrategy(f_strategyId);
    var step = strategy.getStep(f_stepId, true);
    var cs = strategy.checksum;
    var url;

    if (strategy.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strategy.subStratOf).checksum;
    }

    if (step.back_boolean_Id == "") {
      url = "deleteStep.do?strategy=" + strategy.backId + "&step=" +
          step.back_step_Id + "&strategy_checksum=" + cs;
    } else {
      url = "deleteStep.do?strategy=" + strategy.backId + "&step=" +
          step.back_boolean_Id + "&strategy_checksum=" + cs;
    }
      
    $.ajax({
      url: url,
      type: "post",
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function(obj) {
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("DeleteStep", data, strategy, null)) {
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(strategy.frontId);
        }  
      }
    });
  }

  function ExpandStep(e, f_strategyId, f_stepId, collapsedName, uncollapse) {
    var strategy = wdk.strategy.model.getStrategy(f_strategyId);
    var step = strategy.getStep(f_stepId, true);
    var cs = strategy.checksum;
    if (strategy.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strategy.subStratOf).checksum;
    }
    var url = "expandStep.do?strategy=" + strategy.backId + "&step=" +
        step.back_step_Id + "&collapsedName=" + collapsedName +
        "&strategy_checksum=" + cs;
    if (uncollapse) url += "&uncollapse=true";
    
    $.ajax({
      url: url,
      type: "post",
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("EditStep", data, strategy, $("div#query_form"))) {

          // kludge to force a redraw of top level strategy by dirtying the checksum
          var topStrategy = (strategy.subStratOf !== null) ?
              wdk.strategy.model.getStrategy(strategy.subStratOf) :
              strategy;
          topStrategy.checksum += '_';
          // endkludge

          updateStrategies(data);
        } else {
          wdk.util.removeLoading(f_strategyId);
        }
      }
    });
  }

  function openStrategy(stratId){
    var url = "showStrategy.do?strategy=" + stratId;
    var strat = wdk.strategy.model.getStrategyFromBackId(stratId);
    $.ajax({
      url: url,
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        $("body").block();
      },
      success: function(data) {
        $("body").unblock();
        if (wdk.strategy.error.ErrorHandler("Open", data, null, null)) {
          updateStrategies(data);
          if ($("#strategy_results").css('display') == 'none') {
            wdk.addStepPopup.showPanel('strategy_results');
          }
        }
      }
    });
  }

  function deleteStrategy(stratId, fromHist) {
    var url = "deleteStrategy.do?strategy=" + stratId;
    var stratName;
    var strat;
    var message = "If you shared a strategy, its URL stays valid even if you " +
        "delete the strategy.======== Are you sure you want to " +
        "delete the strategy '";

    if (fromHist) {
      stratName = $.trim($("div#text_" + stratId).text());
    } else {
      strat = wdk.strategy.model.getStrategyFromBackId(stratId);
      stratName = strat.name;

      if (strat.subStratOf != null) {
        var parent = wdk.strategy.model.getStrategy(strat.subStratOf);
        var cs = parent.checksum;
        url = "deleteStep.do?strategy=" + strat.backId + "&step=" +
            stratId.split('_')[1] + "&strategy_checksum=" + cs;
        message = "Are you sure you want to delete the substrategy '";
        stratName = strat.name + "' from the strategy '" + parent.name;
      }
    }
    message = message + stratName + "'?";
    var agree = confirm(message);
    if (agree) {
      $.ajax({
        url: url,
        dataType: "json",
        data: "state=" + ns.stateString,
        beforeSend: function() {
          if (!fromHist) wdk.util.showLoading(stratId);
        },
        success: function(data) {
          if (wdk.strategy.error.ErrorHandler("DeleteStrategy", data, null, null)) {
            updateStrategies(data);
            wdk.history.update_hist(true);
            if ($('#search_history').css('display') != 'none') {
              wdk.history.updateHistory();
            }
          }
        }
      });
    }
  }

  function closeStrategy(stratId, isBackId) {
    var strat = wdk.strategy.model.getStrategy(stratId);
    if (isBackId) {
      strat = wdk.strategy.model.getStrategyFromBackId(stratId);
      stratId = strat.frontId;
    }
    var cs = strat.checksum;
    if (strat.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strat.subStratOf).checksum;
    }
    var url = "closeStrategy.do?strategy=" + strat.backId +
        "&strategy_checksum=" + cs;
    $.ajax({
      url: url,
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        wdk.util.showLoading(stratId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("CloseStrategy", data, strat, null)) {

          // kludge to force a redraw of top level strategy by dirtying the checksum
          var topStrategy = (strat.subStratOf !== null) ?
              wdk.strategy.model.getStrategy(strat.subStratOf) :
              strat;
          topStrategy.checksum += '_';
          // endkludge

          updateStrategies(data);
          if ($('#search_history').css('display') != 'none') {
            wdk.history.update_hist(true);
            wdk.history.updateHistory();
          }
        }
      }
    });
  }

  // maybe deprecated??
  function hideStrat(id) {
    var strat = wdk.strategy.model.getStrategy(id);
    if (!strat) return;
    unloadStrategy(id);
    strat.isDisplay = false;
    for (var i=0;i<strat.Steps.length;i++) {
      if (strat.Steps[i].child_Strat_Id != null) {
        hideStrat(strat.Steps[i].child_Strat_Id);
      }
    }
    if ($("#diagram_" + id + " div.selected").length > 0) {
      NewResults(-1);
    }
    $("#diagram_" + id).hide("slow").remove();
    if ($("#Strategies div[id^='diagram']").length == 0) {
      showInstructions();
      NewResults(-1);
    }
  }

  function copyStrategy(stratId, fromHist) {
    var ss = wdk.strategy.model.getStrategyOBJ(stratId);
    var result = confirm("Do you want to make a copy of strategy '" +
        ss.name + "'?");
    if (result == false) return;
    var url = "copyStrategy.do?strategy=" + stratId + "&strategy_checksum=" +
        ss.checksum;
    $.ajax({  
      url: url,
      dataType: "json", 
      data: "state=" + ns.stateString,
      beforeSend: function() {
        if (!fromHist) {
          wdk.util.showLoading(ss.frontId);
        }
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("Copystrategy", data, ss, null)) {
          updateStrategies(data);
          if (fromHist) {
            wdk.history.update_hist(true);
            wdk.history.updateHistory();
          }
        }
      }
    });
  }

  function saveOrRenameStrategy(strategy, checkName, save, fromHist, form) {
    return $.ajax({
      url: "renameStrategy.do",
      type: "POST",
      dataType: "json",
      data: {
        strategy: strategy.backId,
        name: strategy.name,
        description: strategy.description,
        isPublic: strategy.isPublic,
        checkName: checkName,
        save: save,
        strategy_checksum: (strategy.subStratOf != null) ?
            wdk.strategy.model.getStrategy(strategy.subStratOf).checksum :
            strategy.checksum,
        showHistory: fromHist,
        state: ns.stateString
      },
      beforeSend: function() {
        if (!fromHist) {
          wdk.util.showLoading(strategy.frontId);
        }
      },
      success: function(data) {
        var type = save ? "SaveStrategy" : "RenameStrategy";
        if (wdk.strategy.error.ErrorHandler(type, data, strategy, form, strategy.name, fromHist)) {
          updateStrategies(data);
          if (fromHist) {
            wdk.history.update_hist(true);
            wdk.history.updateHistory();
          }
        }
        if(!fromHist) {
          wdk.util.removeLoading(strategy.frontId);
        }
      }
    });
  }

  function ChangeFilter(strategyId, stepId, url, filter) {
    var filterElt = filter;
    var b_strategyId = strategyId;
    var strategy = wdk.strategy.model.getStrategyFromBackId(b_strategyId); 
    var f_strategyId = strategy.frontId;
    if (strategy.subStratOf != null) {
      ns.strats.splice(wdk.strategy.model.findStrategy(f_strategyId));
    }
    var cs = strategy.checksum;
    if (strategy.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strategy.subStratOf).checksum;
    }
    url += "&strategy_checksum="+cs;
    $.ajax({
      url: url,
      type: "GET",
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        $("#strategy_results > div.Workspace").block();
        wdk.util.showLoading(f_strategyId);
        wdk.addStepPopup.disableAddStepButtons();
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("ChangeFilter", data, strategy, null)) {
          updateStrategies(data, true);
          $("div.layout-detail td div.filter-instance div.current")
              .removeClass('current');
          $(filterElt).parent('div').addClass('current');
          wdk.addStepPopup.enableAddStepButtons();
        }
      }
    });
  }


  function SetWeight(e, f_strategyId, f_stepId) {
    var strategy = wdk.strategy.model.getStrategy(f_strategyId);
    var step = strategy.getStep(f_stepId, true);
    var cs = strategy.checksum;
    var weight = $(e).siblings("input#weight").val();
    if (weight == undefined) {
      weight = $(e).siblings().find("input[name='weight']").val();
    }
    if(strategy.subStratOf != null) {
      cs = wdk.strategy.model.getStrategy(strategy.subStratOf).checksum;
    }
    var url = "processFilter.do?strategy=" + strategy.backId +
        "&revise=" + step.back_step_Id + "&weight=" + weight +
        "&strategy_checksum=" + cs;

    $.ajax({
      url: url,
      type: "post",
      dataType: "json",
      data: "state=" + ns.stateString,
      beforeSend: function() {
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("SetWeight", data, strategy, null)) {
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(f_strategyId);
        }
      }
    });
  }

  //===========================================================================
  //  =End Actions
  //===========================================================================

  // editable plugin event handler
  function updateStrategyName(event, widget) {
    var strategyId = widget.element.data("id");
    var strategy = wdk.strategy.model.getStrategyFromBackId(strategyId);

    strategy.name = widget.value;

    wdk.util.showLoading(strategy.frontId);

    strategy.update().success(function(data) {
      if (wdk.strategy.error.ErrorHandler("RenameStrategy", data, strategy, null, strategy.name, null)) {
        updateStrategies(data);
        $(".strategy-name[data-id='" + strategyId + "']").text(strategy.name);
      }
      wdk.util.removeLoading(strategy.frontId);
    });
  }

  // deprecated?  -  dmf
  function callSpanLogic() {
    var cstrt = wdk.strategy.model.getStrategy(wdk.addStepPopup.current_Front_Strategy_Id);
    var f_strategyId = cstrt.frontId;
    var b_strategyId = cstrt.backId;
    var d = wdk.util.parseInputs();
    var quesName = "";
    var outputType = "";
    $("#form_question input[name='value(span_output)']").each(function() {
      if (this.checked) outputType = $(this).val();
    });
    outputType = (outputType.indexOf("A") != -1) ? "a" : "b";
    outputType = $("#form_question input#type"+outputType.toUpperCase()).val();
    if (outputType == "GeneRecordClasses.GeneRecordClass") quesName = "SpanQuestions.GenesBySpanLogic";
    if (outputType == "OrfRecordClasses.OrfRecordClass") quesName = "SpanQuestions.OrfsBySpanLogic";
    if (outputType == "IsolateRecordClasses.IsolateRecordClass") quesName = "SpanQuestions.IsolatesBySpanLogic";
    if (outputType == "EstRecordClasses.EstRecordClass") quesName = "SpanQuestions.EstsBySpanLogic";
    if (outputType == "SnpRecordClasses.SnpRecordClass") quesName = "SpanQuestions.SnpsBySpanLogic";
    if (outputType == "AssemblyRecordClasses.AssemblyRecordClass") quesName = "SpanQuestions.AssemblyBySpanLogic";
    if (outputType == "SequenceRecordClasses.SequenceRecordClass") quesName = "SpanQuestions.SequenceBySpanLogic";
    if (outputType == "SageTagRecordClasses.SageTagRecordClass") quesName = "SpanQuestions.SageTagsBySpanLogic";
    if (outputType == "DynSpanRecordClasses.DynSpanRecordClass") quesName = "SpanQuestions.DynSpansBySpanLogic";
    if (outputType == "") return null;
    $.ajax({
      url: "processFilter.do?questionFullName=" + quesName + "&strategy=" +
          cstrt.backId + "&strategy_checksum=" + cstrt.checksum,
      data: d + "&state=" + ns.stateString,
      type: "post",
      dataType: "json",
      beforeSend: function(){
        wdk.util.showLoading(f_strategyId);
      },
      success: function(data) {
        if (wdk.strategy.error.ErrorHandler("AddStep", data, cstrt, $("div#query_form"))) {
          if ($("div#query_form").css("display") == "none") {
            $("div#query_form").remove();
          }
          updateStrategies(data);
        } else {
          wdk.util.removeLoading(f_strategyId);
        }
      }
    });
    wdk.addStepPopup.isSpan = false;
    wdk.step.isInsert = "";
    wdk.addStepPopup.closeAll(true);
  }

  ns.init = init;
  ns.AddStepToStrategy = AddStepToStrategy;
  ns.ChangeFilter = ChangeFilter;
  ns.DeleteStep = DeleteStep;
  ns.EditStep = EditStep;
  ns.ExpandStep = ExpandStep;
  ns.NewResults = NewResults;
  ns.RenameStep = RenameStep;
  ns.SetWeight = SetWeight;
  ns.closeStrategy = closeStrategy;
  ns.copyStrategy = copyStrategy;
  ns.deleteStrategy = deleteStrategy;
  ns.initDisplay = initDisplay;
  ns.loadModel = loadModel;
  ns.openStrategy = openStrategy;
  ns.saveOrRenameStrategy = saveOrRenameStrategy;
  //ns.setStrategyStatusCounts = setStrategyStatusCounts;
  ns.showStrategies = showStrategies;
  ns.updateStrategies = updateStrategies;
  ns.updateStrategyName = updateStrategyName;
});
