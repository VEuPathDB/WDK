wdk.util.namespace("wdk.addStepPopup", function(ns, $) {
  "use strict";

  var _action = "";
  var global_isAdd; 
  var original_Query_Form_Text;
  var original_Query_Form_CSS = {};
  var buttonText = null;

  ns.isSpan = false;
  ns.current_Front_Strategy_Id = null;

  // var stage = null;

  /*
   * @deprecated Use wdk.history.showUpdateDialog()
   */
  function showExportLink(stratId) {
     closeModal();
     var exportLink = $("div#export_link_div_" + stratId);
     exportLink.draggable({
      handle: ".dragHandle",
      containment: "#strategy_results .scrollable-wrapper"
    }).show();
  }

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
        var currentStep = $("#Strategies div.selected");
        var active_link = $("a.results_link", currentStep);
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

  /**
   * @deprecated Use wdk.history.showUpdateDialog()
   */
  function showSaveForm(stratId, save, share) {
    closeModal();
    $("div.save_strat_div").addClass("hidden");
    var saveForm = $("div#save_strat_div_" + stratId);
    var stratName = wdk.strategy.model.getStrategyOBJ(stratId).name;
    $("input[type=text]", saveForm).attr("value", stratName);

    if (save) {
      $("form#save_strat_form", saveForm).attr("action", "javascript:wdk.strategy.controller.saveOrRenameStrategy(" + stratId + ", true, true, false)");
      $("span.h3left", saveForm).text("Save As");
      $("input[type=submit]", saveForm).attr("value", "Save");

      if (share) {
        $("form#save_strat_form", saveForm)
            .append("<input type='hidden' name='action' value='share'/>");
        $("form#save_strat_form", saveForm)
            .append("<input type='hidden' name='actionStrat' value='" +
                stratId + "'/>");
        $("span.h3left", saveForm).text("First you need to Save it!");
      }

      // we want save_warning, defined in view-JSON.js, to appear for save and share only
      $("form#save_strat_form i").css("display","block");

    } else {
      $("form#save_strat_form", saveForm)
          .attr("action", "javascript:saveOrRenameStrategy(" + stratId +
              ", true, false, false)");
      $("span.h3left", saveForm).text("Rename");
      $("input[type=submit]", saveForm).attr("value", "Rename");
      // we want save_warning, defined in view-JSON.js, to appear for save and share only
      $("form#save_strat_form i").css("display","none");
    }

    saveForm.show();
    $("input[name='name']", saveForm).focus().select();
  }

  /**
   * @deprecated Use jQueryUI dialogs
   */
  function closeModal() {
    $("div.modal_div").hide();
  }

  /**
   * @deprecated See wdk.history.showUpdateDialog()
   */
  function validateSaveForm(form) {
    var strat = wdk.strategy.model.getStrategyFromBackId(form.strategy.value);
    var message;
    if (form.name.value == "") {
      message = "<h1>You must specify a name for saving!</h1><input " +
          "type='button' value='OK' onclick='jQuery(\"div#diagram_" +
          strat.frontId + "\").unblock();jQuery(\"div#search_history\")" +
          ".unblock();'/>";
      $("div#diagram_" + strat.frontId).block({message: message});
      $("div#search_history").block({message: message});
      return false;

    } else if (form.name.value.length > 200) {
      message = "<h1>The name you have entered is too long.  " +
          "Please enter a name that is at most 200 characters.</h1>" +
          "<input type='button' value='OK' onclick='jQuery(\"div#diagram_" +
          strat.frontId + "\").unblock();jQuery(\"div#search_history\")" +
          ".unblock();'/>";
      $("div#diagram_" + strat.frontId).block({message: message});
      $("div#search_history").block({message: message});
      return false;
    }
    return true;
  }

  /**
   * Not sure what this does. It's called in queryList.tag
   */
  function formatFilterForm(params, data, edit, reviseStep, hideQuery, hideOp, isOrtholog) {
    //edit = 0 ::: adding a new step
    //edit = 1 ::: editing a current step
    var ps = document.createElement('div');
    var qf = document.createElement('div');
    var topMenu_script = null;
    qf.innerHTML = data;
    ps.innerHTML = params.substring(params.indexOf("<form"),params.indexOf("</form>") + 6);

    if ($("script#initScript", ps).length > 0) {
      topMenu_script = $("script#initScript", ps).text();
    }

    var operation = "";
    var stepn = 0;
    var insert = "";
    var proto = "";
    var currStrategy = wdk.strategy.model.getStrategy(ns.current_Front_Strategy_Id);
    var stratBackId = currStrategy.backId;
    var stp = null;
    var stepBackId = null;

    if (edit == 0) {
      insert = reviseStep;
      if (insert == "") {
        stp = currStrategy.getLastStep();
        stepBackId = (stp.back_boolean_Id == "") ? stp.back_step_Id :
            stp.back_boolean_id;
      } else {
        stp = currStrategy.getStep(insert,false);
        stepBackId = insert;
      }

    } else {
      var parts = reviseStep.split(":");
      proto = parts[0];
      reviseStep = parseInt(parts[1], 10);
      stp = currStrategy.getStep(reviseStep,false);
      stepBackId = reviseStep;
      // isSub = true; // doesn't appear to be used anywhere - dmf
      operation = parts[4];
    }

    var pro_url = "";

    if (edit == 0) {
      pro_url = "processFilter.do?strategy=" + stratBackId + "&insert=" +
          insert + "&ortholog=" + isOrtholog;

    } else{
      pro_url = "processFilter.do?strategy=" + stratBackId + "&revise=" +
          stepBackId;
    }

    var historyId = $("#history_id").val();
    var close_link;
    var back_link;
    
    if (edit == 0) {
      close_link = "<a class='close_window' " +
          "href='javascript:closeAll(false)'> " +
          "<img src='wdk/images/Close-X-box.png'/></a>";
      back_link = "<a id='back_to_selection' href='javascript:close()'>" + 
          "<img src='wdk/images/backbox.png'/></a>";

    } else {
      close_link = "<a class='close_window' " +
          "href='javascript:closeAll(false)'>" +
          "<img src='wdk/images/Close-X-box.png'/></a>";
    }

    var quesTitle = data.substring(data.indexOf("<h1>") + 4,
        data.indexOf("</h1>")).replace(/Identify \w+( \w+)* based on/,"");
    
    var quesForm = $("#form_question",qf);

    if (quesForm[0].tagName != "FORM") {
      var f = document.createElement('form');
      $(f).attr("id",$(quesForm).attr("id"));
      $(f).html($(quesForm).html());
      quesForm = $(f);
    }

    var quesDescription = $("#query-description-section",qf);//data;
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
    
    if (edit == 0) {
      if (insert == "" || (stp.isLast && isOrtholog)) {
        $(".filter.params", quesForm).prepend("<span class='form_subtitle'>" +
            "Add&nbsp;Step&nbsp;" + (parseInt(stp.frontId, 10)+1) + ": " +
            quesTitle + "</span></br>");    
      } else if (stp.frontId == 1 && !isOrtholog) {
        $(".filter.params", quesForm).prepend("<span class='form_subtitle'>" +
            "Insert&nbsp;Step&nbsp;Before&nbsp;" + (stp.frontId) + ": " +
            quesTitle + "</span></br>");
      } else if (isOrtholog) {
        $(".filter.params", quesForm).prepend("<span class='form_subtitle'>" +
            "Insert&nbsp;Step&nbsp;Between&nbsp;" + (stp.frontId) +
            "&nbsp;And&nbsp;" + (parseInt(stp.frontId, 10)+1) + ": " +
            quesTitle + "</span></br>");
      } else {
        $(".filter.params", quesForm).prepend("<span class='form_subtitle'>" +
            "Insert&nbsp;Step&nbsp;Between&nbsp;" +
            (parseInt(stp.frontId, 10)-1) + "&nbsp;And&nbsp;" + (stp.frontId) +
            ": " + quesTitle + "</span></br>");    
      }
    } else {
      $(".filter.params", quesForm).prepend("<span class='form_subtitle'>" +
          "Revise&nbsp;Step&nbsp;" + (stp.frontId) + ": " + quesTitle +
          "</span></br>");
    }

    // TODO - replace with templates (handlebars?)
    if (edit == 0) {
      if (insert == "") {
        $(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (stp.frontId) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck' valign='middle'><input type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td valign='middle'>&nbsp;" + (stp.frontId) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>UNION</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)+1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td></tr></table></div></div>");
      } else {
        $(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (parseInt(stp.frontId, 10)-1) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck' valign='middle'><input type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td valign='middle'>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>UNION</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "</td></tr></table></div></div>");
      }

    } else {
      if (stp.frontId != 1) {
        $(".filter.params", quesForm).after("<div class='filter operators'><span class='form_subtitle'>Combine with Step " + (parseInt(stp.frontId, 10)-1) + "</span><div id='operations'><table style='margin-left:auto; margin-right:auto;'><tr><td class='opcheck'><input id='INTERSECT' type='radio' name='booleanExpression' value='INTERSECT' /></td><td class='operation INTERSECT'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>INTERSECT</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input id='UNION' type='radio' name='booleanExpression' value='UNION'></td><td class='operation UNION'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>UNION</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input id='MINUS' type='radio' name='booleanExpression' value='NOT'></td><td class='operation MINUS'></td><td>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "&nbsp;<b>MINUS</b>&nbsp;" + (stp.frontId) + "</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class='opcheck'><input type='radio' name='booleanExpression' value='RMINUS'></td><td class='operation RMINUS'></td><td>&nbsp;" + (stp.frontId) + "&nbsp;<b>MINUS</b>&nbsp;" + (parseInt(stp.frontId, 10)-1) + "</td></tr></table></div></div>");
      } else {
        $(".filter.params", quesForm).after("<input type='hidden' name='booleanExpression' value='AND' />");
      }
    }

    var action;
    if (edit == 0) {
      action = "javascript:validateAndCall('add','" + pro_url + "', '" + stratBackId + "')";
    } else {
      action = "javascript:validateAndCall('edit', '" + pro_url + "', '" + stratBackId + "', "+ parseInt(reviseStep, 10) + ")";
    }

    var formtitle = "";

    if (edit == 0) {
      if (insert == "") {
        formtitle = "<h1 style='font-size:130%;position:relative;margin-top: 4px;'>Add&nbsp;Step</h1>";
      } else {
        formtitle = "<h1  style='font-size:130%;position:relative;margin-top: 4px;'>Insert&nbsp;Step</h1>";
      }
    } else {
      formtitle = "<h1  style='font-size:130%;position:relative;margin-top: 4px;'>Revise&nbsp;Step</h1>";
    }

    quesForm.attr("action",action);

    var header;
    if (edit == 0) {
      header = "<span class='dragHandle'>" + back_link + " " + formtitle + " " + close_link + "</span>";
    } else {
      header = "<span class='dragHandle'>" + formtitle + " " + close_link + "</span>";
    }
      
    $("#query_form").html(header);

    if (hideQuery) {
      $(".filter.params", quesForm).remove();
      $("input[name=questionFullName]", quesForm).remove();
      $(".filter.operators", quesForm).width('auto');
    } else {
      $("div.filter div.params", quesForm).html(ps.getElementsByTagName('form')[0].innerHTML);
    }

    if (hideOp) {
      $(".filter.operators", quesForm).remove();
      $(".filter.params", quesForm).after("<input type='hidden' name='booleanExpression' value='AND' />");
    }
    
    $("#query_form").append(quesForm);

    if (edit == 1) {
      $("#query_form div#operations input#" + operation).attr('checked','checked'); 
    }
    
    if (quesDescription.length > 0) {
      $("#query_form").append("<div style='padding:5px;margin:5px 15px 5px 15px;border-top:1px solid grey;border-bottom:1px solid grey'>" + quesDescription.html() + "</div>");
    }

    if (dataSources.length > 0) {
      $("#query_form").append("<div style='padding:5px;margin:5px 15px 5px 15px;border-top:1px solid grey;border-bottom:1px solid grey'>" + dataSources.html() + "</div>");
    }

    $("#query_form").append("<div class='bottom-close'><a href='javascript:closeAll(false)' class='close_window'>Close</a></div>");
    setDraggable($("#query_form"), ".dragHandle");
    $("#query_form").fadeIn("normal");

    if (topMenu_script != null) {
      var tms = topMenu_script.substring(topMenu_script.indexOf("{")+1,topMenu_script.indexOf("}"));
      eval(tms);
    }

    if (edit == 1) {
      wdk.parameterHandlers.init(true, true);
    } else {
      wdk.parameterHandlers.init(true);
    }
  }

  /**
   * called in formatFilterForm -- deprecated?
   */
  function validateAndCall(type, url, proto, rs){
    var valid = false;

    if ($("div#query_form div.filter.operators").length == 0) {
      valid = true;
    } else {
      if ($(".filter.operators")) {
        $(".filter.operators div#operations input[name='booleanExpression']").each(function(){
          if($(this)[0].checked) valid = true;
        });
      }
    }

    if (!valid) {
      alert("Please select Intersect, Union or Minus operator.");
      return;
    }

    wdk.parameterHandlers.mapTypeAheads();
    window.scrollTo(0,0);

    if (type == 'add') {
      wdk.strategy.controller.AddStepToStrategy(url, proto, rs);
    } else {
      wdk.strategy.controller.EditStep(url, proto, rs);
    }
    return;
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
        $("#query_form").css("z-index", 100);
        $("#query_form_overlay").css("z-index", 100).height($("body").height());
      },
      error: function() {
        alert("Error getting the needed information from the server \n" +
            "Please contact the system administrator");
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

  function callWizard(url, ele, id, sec, action, stratFrontId){
    // TODO - make this accssible via wdk.addStepPopup namespace
    // set isPopup flag, which will be used by param initialization process
    window.isPopup = true;

    //hide any open tooltips
    $(".qtip").qtip("hide");

    wdk.parameterHandlers.mapTypeAheads();

    if (stratFrontId == undefined) {
      stratFrontId = ns.current_Front_Strategy_Id; 
    }

    var strategy = wdk.strategy.model.getStrategy(stratFrontId);
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
          data: wdk.util.parseInputs() + "&state=" + wdk.strategy.controller.p_state,

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
        var d = "strategy="+strategy.backId;
        $.ajax({
          url: url,
          type: "get",
          dataType: "html",
          data: d,

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
            $("#query_form").css("z-index", 100);
            $("#query_form_overlay").css("z-index", 100).height($("body").height());

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

  /**
   * @deprecated
   */
  function openAddStrategy(strat_id) {
    original_Query_Form_Text = $("#query_form").html();
    OpenOperationBox(strat_id, (global_isAdd ? undefined : step_id));
    return false;
  }

  /**
   * @deprecated to close the old question form
   */
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

  function closeAll(hide, as) {
    if (hide) {
      $("#query_form").hide();
      $("#query_form_overlay").hide();
    } else {
      ns.isSpan = false;
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
      containment: "document"
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

  ns.showExportLink = showExportLink;
  ns.showPanel = showPanel;
  ns.showSaveForm = showSaveForm;
  ns.closeModal = closeModal;
  ns.validateSaveForm = validateSaveForm;
  ns.formatFilterForm = formatFilterForm;
  ns.validateAndCall = validateAndCall;
  ns.openFilter = openFilter;
  ns.openStage = openStage;
  ns.WizardLoading = WizardLoading;
  ns.callWizard = callWizard;
  ns.backStage = backStage;
  ns.setPopupContent = setPopupContent;
  ns.openAddStrategy = openAddStrategy;
  ns.close = close;
  ns.closeAll = closeAll;
  ns.enableAddStepButtons = enableAddStepButtons;
  ns.disableAddStepButtons = disableAddStepButtons;
  ns.setDraggable = setDraggable;
  ns.showNewSection = showNewSection;
  ns.changeButtonText = changeButtonText;
  ns.validateOperations = validateOperations;

});
