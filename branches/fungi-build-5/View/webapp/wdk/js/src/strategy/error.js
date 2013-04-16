wdk.util.namespace("window.wdk.strategy.error", function (ns, $) {
  "use strict";

  var OOSMessage = "Sorry, there was a synchronization problem.\n" +
      "We have updated the page with the latest infromation we have.\n" +
      "Please re-do your last action.";
  var SessionTimeOutMessage = "Your session may have timed out and all " +
      "of your data has been lost.  By registering and logging in you can " +
      "prevent this from happening in the future.";

  function ErrorHandler(evt, data, strategy, qform, name, fromHist) {
    var type = null;

    if ($("form#form_question table.parameter-errors").length > 0) {
      $("form#form_question table.parameter-errors").remove();
    }

    if (evt == "Results") {
      if (data.substring(0,1) != "{") return true;

      data = eval("(" + data + ")");

      for (var v in data.state) {
        if (data.state[v].id == strategy.backId) {
          break;
        }
      }

      wdk.strategy.controller.loadModel(data.strategies[data.state[v].checksum], v);
      var x = {
        strategy: undefined,
        step: undefined
      };
      wdk.strategy.controller.showStrategies(x, false, 0);
      return;
    }

    if (data.type == "success") {
      return true;
    } else {
      type = data.type;
      if (type == "param-error") { //Error is in the parameter list
        var table = document.createElement('table');
        $(table).addClass("parameter-errors").html("<tr><td colspan=2>"+data.message+"</td></tr>");
        var params = data.params;
        for (var p in params) {
          if (p != "length") {
            var tr = document.createElement('tr');
            var tdPrompt = document.createElement('td');
            var tdMessage = document.createElement('td');
            $(tdPrompt).html(p + ": ");
            $(tdMessage).html(params[p]);
            $(tr).append(tdPrompt).append(tdMessage);
            $(table).append(tr);
          }
        }
        $(qform).prepend(table);
        $(qform).show();
      } else if(type == "out-of-sync") { //Checksum sent did not match the back-end checksum
        if (data.state.length == 0) {
          alert(SessionTimeOutMessage);
        } else {
          alert(OOSMessage);
        }
        wdk.strategy.view.removeStrategyDivs(strategy.backId);
        var f_strategyId = wdk.strategy.controller.updateStrategies(data, evt, strategy);
        wdk.util.removeLoading(strategy.frontId);
        $("#diagram_" + strategy.frontId + " div.stepBox:last h6.resultCount:last a").click();
        wdk.step.isInsert = "";
      } else if(type == "dup-name-error") {
        if (evt == "SaveStrategy") {
          var overwrite = confirm("A strategy already exists with the name '" +
              name + ".' Do you want to overwrite the existing strategy?");
          if (overwrite) {
            wdk.strategy.controller.saveOrRenameStrategy(strategy, false, true,
                fromHist, qform);
          }
        } else if(evt == "RenameStrategy") {
          alert("An unsaved strategy already exists with the name '" + name +
              ".'");
          if (strategy.isSaved) {
            $("input[name='name']",qform).attr("value", strategy.savedName);
          }
        }
      } else { //Gerenal Error Catcher
        alert(data.message);
        //TODO : Add a AJAX call to send an e-mail to Admininstrator with exception, stacktrace and message
        wdk.strategy.controller.initDisplay(0);
      }
    }
  }

  function ValidateView(strategies){
    var failed = [];

    for (var str in wdk.strategy.controller.strats) {
      var strat = wdk.strategy.controller.strats[str];
      if (strat.checksum != strategies[strat.backId]) {
        failed.push(strat.frontId);
      }
    }

    if (failed.length == 0) {
      return true;
    } else {
      return failed;
    }
  }

  ns.ErrorHandler = ErrorHandler;

});
