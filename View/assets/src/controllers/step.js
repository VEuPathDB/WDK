wdk.util.namespace("window.wdk.step", function(ns, $) {
  "use strict";

  ns.openDetail = null;
  ns.isInsert = "";

  function init() {
    $("div.diagram:first div.stepBox:last h6.resultCount:last a").click();
  }

  function showDetails(det) {
    ns.openDetail = $(det).parent().find("div.crumb_details");
  /*
    var parent = openDetail.parent(); // for boolean details, parent is step div
    if (!parent.is("div")) parent = parent.parent(); // for non-boolean details, grandparent is step div
    var diagram = parent.parent();
    var dId = $(diagram).attr("id").substring(8);
    dId = parseInt(dId, 10);
  */
    var disp = ns.openDetail.attr("disp");
    $("body > div.crumb_details").each(function() {
      $(this).remove();  
    });
    $("div#Strategies").find("div.crumb_details").each(function() {
      $(this).attr("disp","0");
    });
    $("a.crumb_name img").attr("src",wdk.assetsUrl("/wdk/images/plus.gif"));
    if (disp == "0") {
      ns.openDetail.attr("disp","1");
      var det2 = ns.openDetail.clone();
      det2.addClass("jqDnR");
      det2.find(".crumb_menu").addClass("dragHandle");
      wdk.util.setDraggable(det2, ".dragHandle");
      var l = 361;
      var t = 145;
      det2.css({
        left: l + "px",
        top: t + "px",
        display: "block",
        position: "absolute"
      });
      det2.appendTo("body");
      wdk.util.initShowHide(det2);
      var op = $(".question_name .operation", det2);
      if (op.length > 0) {
        var opstring = op.removeClass("operation").attr('class');
        op.addClass("operation");
        $("input[value='" + opstring + "']", det2).attr('checked','checked');
      }
      if ($(det).hasClass('crumb_name')) $(det).children("img").attr("src",wdk.assetsUrl("/wdk/images/minus.gif"));
    } else {
      ns.openDetail.attr("disp","0");
      if ($(det).hasClass('crumb_name')) $(det).children("img").attr("src",wdk.assetsUrl("/wdk/images/plus.gif"));
    }
  }

  function hideDetails(det) {
    
    if (ns.openDetail != null) ns.openDetail.attr("disp","0");
    ns.openDetail = null;
    
    $("body > div.crumb_details").each(function() {
      $(this).remove();
    });
    $("a.crumb_name img").attr("src",wdk.assetsUrl("/wdk/images/plus.gif"));
  }

  function Edit_Step(ele, questionName, url, hideQuery, hideOp, assignedWeight) {
    wdk.addStepPopup.closeAll(false);
    var revisestep = $(ele).attr("id");
    var parts = revisestep.split("|");
    var strat = wdk.strategy.model.getStrategy(parts[0]);
    wdk.addStepPopup.current_Front_Strategy_Id = parts[0];

    // the input id is the back id of the step that is current revised.
    var inputId = parseInt(parts[1], 10);
    var step = strat.getStep(inputId);
    var operation = parts[2];

    // the revise id is the back id of the bottom node step.
    var reviseId = inputId;

    url = "wizard.do?action=revise&questionFullName=" + questionName + url;
    if (step.isboolean) {
      // revise id is the boolean step id
      reviseId = step.back_boolean_Id;
      url += "&stage=question&booleanExpression="+ operation;
    } else if (step.isTransform) {
      // revise id is the same as input id
      url += "&stage=transform";
    } else if (step.isSpan) {
      // revise id is the span step id
      reviseId = step.back_boolean_Id;
      var stage = (inputId == reviseId) ? "revise_span" : "question";
      url += "&stage=" + stage;
    } else {
      // revise the the first step, the revise id is the same as input id
      url += "&stage=question";
    }
    // assign the revise id and operation
    url += "&step=" + reviseId + "&operation=" + operation;

    if ($("#qf_content").length == 0) {
      if (assignedWeight) {
        url += "&weight=" + assignedWeight;
      }
    }

    // set isEdit flag, it will be used by the param initialization process
    window.isEdit = true;

    wdk.addStepPopup.callWizard(url,ele,null,null,'next')
    $(this).parent().parent().hide();
  }

  function Insert_Step(ele, dt) {
    var sNumber = $(ele).attr("id");
    sNumber = sNumber.split("|");
    ns.isInsert = sNumber[1];
    wdk.addStepPopup.current_Front_Strategy_Id = sNumber[0];
    wdk.addStepPopup.openFilter(dt,sNumber[0],sNumber[1],false);
  }

  function Rename_Step(ele, strat, stpId) {
    var a_link = $("#diagram_" + strat + " div#step_" + stpId + "_sub " +
        "a#stepId_" + stpId, $(ele).parent().parent().parent());
    var old_name = $(a_link).parent().find("#fullStepName").text();
    var input = document.createElement('input');

    $(input).attr("id","new_name_box").attr("value",old_name).blur(function() {
      wdk.strategy.controller.RenameStep(this,strat,stpId)
    }).focus(function() {
      this.select();
    }).keypress(function(event) {
      wdk.util.checkEnter(this, event);
    }).attr("size","10");

    $("#diagram_" + strat + " div#step_" + stpId + "_sub a#stepId_" + stpId,
        $(ele).parent().parent().parent()).replaceWith(input);
    $("#new_name_box").focus();
  }


  function Expand_Step(ele, url) {
    $(ele).parent().parent().hide();
    wdk.strategy.controller.ExpandStep(url);
  }

  ns.init = init;
  ns.showDetails = showDetails;
  ns.hideDetails = hideDetails;
  ns.Edit_Step = Edit_Step;
  ns.Insert_Step = Insert_Step;
  ns.Rename_Step = Rename_Step;
  ns.Expand_Step = Expand_Step;

});
