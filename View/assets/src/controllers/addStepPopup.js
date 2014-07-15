wdk.util.namespace("wdk.addStepPopup", function(ns, $) {
  "use strict";

  var _action = "";
  var global_isAdd; 
  var original_Query_Form_Text;
  var original_Query_Form_CSS = {};
  var buttonText = null;

  ns.current_Front_Strategy_Id = null;

  // make panel object that implements show
  function showPanel(panel) {

    if (panel == 'strategy_results') {
      if ($("div#Strategies").attr("newstrategy") == 'true') {
        wdk.dyk.initDYK(true);
      } else {
        wdk.dyk.initDYK(false);
      }

    } else {
      wdk.dyk.initDYK(false);
    }
    
    $("#strategy_tabs li").each(function() {
      if ($("a", this).length > 0) {
        var hidePanel = $("a", this).attr("id").substring(4);
        $("#tab_" + hidePanel).parent().removeAttr("id");
        $("#" + hidePanel).css({
          position: 'absolute',
          left: '-1000em',
          width: '100%',
          display: 'none'
        });
      }
    });

    $("#tab_" + panel).parent().attr("id", "selected");
    $("#" + panel).css({'position':'relative','left':'auto','display':'block'});

    if (panel == 'strategy_results') {
      if ($.cookie("refresh_results") == "true") {
        // reload current tab
        var currentStep = $("#Strategies div.selected");
        var active_link = $(".results_link", currentStep);
        if (active_link.length == 0) {
          active_link = $(".resultCount a.operation", currentStep);
        }
        active_link.click();
        $.cookie("refresh_results", "false", { path : '/' });
      }

      $("body > #query_form").show();
      $("body > .crumb_details").show();

    } else {
      if (panel == 'search_history') wdk.history.updateHistory();
      if (panel == 'basket') wdk.basket.showBasket();
      if (panel == 'public_strat') wdk.publicStrats.showPublicStrats();
      $("body > #query_form").hide();
      $("body > .crumb_details").hide();
    }
    wdk.stratTabCookie.setCurrentTabCookie('application', panel);
    $(".strategy-description.qtip").qtip("hide");
  }

  // the dtype is never used, ignored.
  /**
   * opens initial addstep popup list
   */
  function openFilter(dtype,strat_id,step_id,isAdd) {
    // open the default stage
    openStage(strat_id, step_id, isAdd, '');
  }

  /**
   * opens initial addstep popup list
   */
  function openStage(strat_id,step_id,isAdd, stage) {
    var stp;
    global_isAdd = isAdd;

    if (wdk.step.openDetail != null) wdk.step.hideDetails();

    $("#strategy_results div.attributesList").hide();
    var isFirst = false;
    var steps = wdk.strategy.model.getStrategy(strat_id).Steps;

    if (step_id == undefined) {
      isFirst = true;
    } else {
      stp = wdk.strategy.model.getStrategy(strat_id).getStep(step_id,false)
      if(stp != null && stp.frontId == 1 && !isAdd) isFirst = true;
    }

    ns.current_Front_Strategy_Id = strat_id;
    var currStrat = wdk.strategy.model.getStrategy(strat_id);
    ns.current_Front_Strategy_Id = strat_id;

    var currentStepId = stp.back_boolean_Id;

    if (currentStepId == '') {
      currentStepId = stp.back_step_Id;
    }
    
    var url = "wizard.do?strategy="+currStrat.backId + "&step=" + currentStepId;

    if (stage != '') {
      url += "&stage=" + stage;
    }

    // add insert flag
    var action = isAdd ? "add" : "insert";
    url += "&action=" + action;

    $.ajax({
      url: url,
      dataType: "html",
      beforeSend: function() {
        $("#query_form").remove();
        $("#query_form_overlay").remove();
        disableAddStepButtons();
      },
      success: function(data) {
        wdk.step.hideDetails();
        wdk.dyk.dykClose();
        $("body").append(data);

        if (isAdd) {
          $("#query_form h1#query_form_title").html("Add&nbsp;Step");
        } else {
          $("#query_form h1#query_form_title").html("Insert&nbsp;Step");
        }
        setDraggable($("#query_form"), ".dragHandle");

        $("#query_form").css({
          // keep popup in viewport
          top: Math.max($(window).scrollTop() + 50,
                        $("#strategy_results").offset().top),
          left: ($(window).width() - $("#query_form").width()) / 2,
          zIndex: 100
        });

        $("#query_form_overlay").css("z-index", 100).height($("body").height());
      },
      error: function() {
        alert("Sorry. There has been an error getting the information from the server. \n" +
            "Please reload your page.\n" +
            "If this does not fix your problem, please contact us (link on top right) with a description.");
        enableAddStepButtons();
      }
    });
  }

  /**
   * display loading gif on form
   */
  function WizardLoading(boo) {
    if (boo) {
      var i = $("img#wizard-busy-image").clone();
      buttonText = $("div.filter-button").html();
      $("div.filter-button").html(i.show());
    } else {
      $("div.filter-button").html(buttonText);
      buttonText = null;
    }
  }

  // TODO Remove inline references to these functions. As it is now,
  // it is not possible to cancel an inline 'onsubmit' handler without
  // hacking it into a more conventional event handler.
  function callWizard(url, ele, id, sec, action, stratFrontId){
    // sometimes url can be null...
    var urlBase = url && url.split(/\?/)[0];
    var params = url && url.split(/\?/)[1];

    //hide any open tooltips
    $(".qtip").qtip("hide");

    // Not sure we need this
    // wdk.parameterHandlers.mapTypeAheads();

    if (stratFrontId == undefined) {
      stratFrontId = ns.current_Front_Strategy_Id; 
    }

    var strategy = wdk.strategy.model.getStrategy(stratFrontId);
    $("div#errors").html("");

    switch (action) {

      case "submit":
        var stage = $(ele).find("#stage").val();
        params = params + "stage="+stage+"&strategy="+strategy.backId;
        $(ele).attr("action", "javascript:void(0)");
        $.ajax({
          url: urlBase,
          type: "POST",
          dataType: "html",
          data: params + '&' + wdk.util.parseInputs() + "&state=" + wdk.strategy.controller.stateString,

          beforeSend: function() {
            //$(".crumb_details").block( {message: "Loading..."} );
            WizardLoading(true);
          },

          success: function(data) {
            wdk.step.hideDetails();
            $(".crumb_details").unblock();

            if (data.indexOf("{") == 0) {
              data = (0, eval)("("+data+")"); // use indirect eval
              // before close, check if json is success or error, if error, display 
              // it in the current qf_content
              if (wdk.strategy.error.ErrorHandler("Wizard", data, strategy,
                  $("#errors"))) {
                closeAll();

                wdk.strategy.controller.updateStrategies(data);

              } else {
                WizardLoading(false);
              }
            } else {
              WizardLoading(false);
              $("#qf_content").children().wrapAll('<div class="stage" />');
              $("#qf_content > div.stage").appendTo("#stage-stack");
              setPopupContent(data);
            }

            $("#query_form").css("z-index", 100);
            $("#query_form_overlay").css("z-index", 100).height($("body").height());
          }
        });
        break;

      case "next":
        params = params + "&strategy="+strategy.backId;
        $.ajax({
          url: urlBase,
          type: "POST",
          dataType: "html",
          data: params,

          beforeSend: function(jqXHR, data) {
            $("#query_form").block({
              message: "Loading...",
              overlayCSS: {
                marginTop: "25px"
              }
            });
          },

          success: function(data) {
            wdk.step.hideDetails();
            $("#query_form").unblock();

            if (data.indexOf("{") == 0) {
              wdk.strategy.controller.updateStrategies(data);
            } else {
              if ($("#qf_content").length == 0) {
                var urlparts = url.split("/");
                $.ajax({
                  async: false,
                  url:"wdk/jsp/wizard/context.jsp",
                  type:"get",

                  success: function(data) {
                    $("body").append(data);
                    setDraggable($("#query_form"), ".dragHandle");
                  }
                });
              } else {
                $("#qf_content").children().wrapAll('<div class="stage" />');
                $("#qf_content > .stage").appendTo("#stage-stack");
              }
              setPopupContent(data);

              if (ele != undefined) {
                showNewSection(ele,id,sec);
              }
            }

            $("#query_form").css({
              zIndex: 100,
              top: Math.max($(window).scrollTop() + 50,
                            $("#strategy_results").offset().top),
              left: ($(window).width() - $("#query_form").width()) / 2
            });
            $("#query_form_overlay").css("z-index", 100).height($("body").height());

            // inline-submit should be called last
            // attach submit handler to check that a boolean/span operation is selected
            var $form = $("#query_form").find("#form_question");
            $form.data("inline-submit", $form.get(0).onsubmit);
            $form.get(0).onsubmit = null;
            $form.submit(validateOperations);
          }
        });
        break;

      default:
        showNewSection(ele,id,sec);
        break;

    }
    return false;
  }

  function backStage() {
    var lastStage = $("#stage-stack > .stage:last").detach();
    if (lastStage.length == 0) {
      closeAll();
    } else {
      $("#qf_content").html("");
      $("#qf_content").append(lastStage);
      $("#qf_content > .stage").children().unwrap();
    }
  }

  function setPopupContent(data) {
    $("#qf_content").html(data);
    // updateStepNumberReferences();
  }

  function closeAll(hide, as) {
    if (hide) {
      $("#query_form").hide();
      $("#query_form_overlay").hide();
    } else {
      // ns.isSpan = false;
      $("#query_form").remove();
      $("#query_form_overlay").remove();
      $(".original").remove();
      $("#stage-stack").html("");
    }

    wdk.step.isInsert = "";
    enableAddStepButtons();
  }

  function enableAddStepButtons() {
    $("#Strategies div a#filter_link span").each(function() {
      var button = $(this).parent("a");
      var oldHref = button.attr("oldHref");

      if (oldHref) {
        button.attr("href", oldHref);
        button.removeAttr("oldHref");
      }

    }).css({opacity: 1.0});
  }

  function disableAddStepButtons() {
    $("#Strategies div a#filter_link span").each(function() {
      var button = $(this).parent("a");
      button.attr("oldHref",button.attr("href"));
      button.attr("href","javascript:void(0);");
    }).css({opacity: 0.4});
  }

  function setDraggable(e, handle) {
    var rlimit,
        tlimit,
        blimit;
    rlimit = $("div#contentwrapper").width() - e.width() - 18;
    if (rlimit < 0) rlimit = 525;
    blimit = $("body").height();
    tlimit = $("div#contentwrapper").offset().top;
    $(e).draggable({
      handle: handle,
      // containment: [0, tlimit, rlimit, blimit]
      containment: "document",
      scroll: false
    });
  }

  function showNewSection(ele,sectionName,sectionNumber) {
    ns.isSpan = (sectionName == 'span_logic' ||
        (sectionName !== null && sectionName.split("_")[0] == 'sl'));
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

    for (var i = sectionNumber; i <= 5; i++) {
      $("td#section-" + i + " div.qf_section").html("");
    }

    $(ele).parent().find("li").css({
      "background":"",
      "font-weight":""
    });

    $(ele).css({
      "background-color":"#DDDDDD"
    });

    $("#query_form table#sections-layout td#section-" + (sectionNumber-1) +
        " div").css("background-color","#FFFFFF");
    $("#query_form table#sections-layout td#section-" + sectionNumber)
        .replaceWith(sec);
  }

  function changeButtonText(ele) {
    var v = "";
    var stage = $(ele).attr("stage");
    $(ele).parents("form").find("#stage").val(stage);

    if ($(ele).val() != "SPAN") {
      v = "Run Step";
    } else {
      v = "Continue....";
    }

    //$("form#form_question").attr("action",stage);
    $(".filter-button input[name='questionSubmit']").attr("value",v);
  }

  /**
   * Don't allow form to submit unless an operation is selected
   */
  function validateOperations(e) {
    var $this = $(this);
    var bools = $this.find("input[name='boolean']");
    var inlineSubmit = $this.data("inline-submit");
    e.preventDefault();
    e.stopPropagation();
    if (bools.length) {
      var boolChecked = bools.toArray().reduce(function(memo, input) {
        return memo || input.checked; 
      }, false);
      if (!boolChecked) {
        if ($this.find(".wdk-error").length === 0) {
          $("<div>Please choose an operation below</div>")
            .addClass("wdk-error")
            .css("text-align", "center")
            .insertBefore($this.find("#operations"));
          return;
        }
      }
    }
    if (inlineSubmit instanceof Function) {
      inlineSubmit.call(this, e);
    }
  }

  ns.showPanel = showPanel;
  ns.openFilter = openFilter;
  ns.openStage = openStage;
  ns.WizardLoading = WizardLoading;
  ns.callWizard = callWizard;
  ns.backStage = backStage;
  ns.setPopupContent = setPopupContent;
  ns.closeAll = closeAll;
  ns.enableAddStepButtons = enableAddStepButtons;
  ns.disableAddStepButtons = disableAddStepButtons;
  ns.setDraggable = setDraggable;
  ns.showNewSection = showNewSection;
  ns.changeButtonText = changeButtonText;
  ns.validateOperations = validateOperations;

});
