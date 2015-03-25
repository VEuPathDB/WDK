/* jshint evil:true */
wdk.util.namespace("window.wdk.strategy.error", function (ns, $) {
  "use strict";

  var OOSMessage = "Your last request was sent from a page that is out " +
      "of date. Please reload the page and try again.";
  var SessionTimeOutMessage = "Your session may have timed out and all " +
      "your data has been lost.  By registering and logging in you can " +
      "prevent this from happening in the future.";

  function ErrorHandler(evt, data, strategy, qform, name, fromHist) {
    var type = null;

    $("form[name=questionForm] .paramter-errors").remove();

    if (evt == "Results") {
      if (data.substring(0,1) != "{") return true;

      // Presumabley, we only get here if data is out-of-sync
      data = JSON.parse(data);

      // Find the index of the strategy in `data.state` whose step results we are trying to view
      for (var index in data.state) {
        // If this is never true, then `index` will be the last strategy in `data.state`
        if (data.state[index].id == strategy.backId) {
          break;
        }
      }

      // Update client state with server state
      wdk.strategy.controller.state = data.state;

      // Reload the strategy model of the step we are trying to view
      wdk.strategy.controller.loadModel(data.strategies[data.state[index].checksum], index);

      // Finally, render the strategy panel again
      wdk.strategy.controller.showStrategies(data.currentView, false, 0);

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
        if (data.state.length === 0) {
          alert(SessionTimeOutMessage);
        } else {
          alert(OOSMessage);
        }
        wdk.strategy.view.removeStrategyDivs(strategy.backId);
        wdk.util.removeLoading(strategy.frontId);
        $("#diagram_" + strategy.frontId + " div.stepBox:last h6.resultCount:last a").click();
        wdk.step.isInsert = "";
      } else if(type == "dup-name-error") {
        if (evt == "SaveStrategy") {
          var publicAddOn = (data.isPublicDup === true) ? " <em>public</em>" : "";
          var dialogContent = "<div>A" + publicAddOn + " strategy already " +
               "exists with the name '" + name + "'.<br/> <br/>Are you sure " +
               "you want to overwrite it?</div>";
          $(dialogContent).dialog({
            resizable: false,
            modal: true,
            title: "Please Confirm...",
            buttons: {
              Ok: function () {
                wdk.strategy.controller.saveOrRenameStrategy(strategy, false, true, fromHist, qform);
                $(this).dialog("close");
              },
              Cancel: function () {
                $(this).dialog("close");
              }
            }
          });
        } else if(evt == "RenameStrategy") {
          var savedNotif = (strategy.isSaved ? "saved" : "unsaved");
          alert("An " + savedNotif + " strategy already exists with the name '" + name + ".'");
          if (strategy.isSaved) {
            $("input[name='name']",qform).attr("value", strategy.savedName);
          }
        }
      } else { // General Error Catcher
        alert(data.message);
        //TODO : Add a AJAX call to send an e-mail to Administrator with exception, stack trace and message
        wdk.strategy.controller.initDisplay(0);
      }
    }
  }

  ns.ErrorHandler = ErrorHandler;

});
