wdk.util.namespace("wdk.components.datasetParam", function(ns, $) {
  "use strict";

  var init = function(paramDiv, attrs) {
    // set up change event on subparams
    paramDiv.find("input.type").each(function() {
      var type = $(this);
      if (type.is('[disabled=disabled]')) {
        type.parents("tr.subparam").addClass("disabled")
                                   .find("input, textarea, select").attr("disabled", "disabled");
      } else { // not disabled, register change event
        type.change(function() {
          var subparam = $(this).val();
          changeSubparams(paramDiv, subparam);
        });
      }
    });

    // enable the default subparam
    var subparam = paramDiv.find("input.type:checked").val();
    changeSubparams(paramDiv, subparam);
  };


  function changeSubparams(paramDiv, enabledSubparam) {
    // interate through all the subparams, and only enable the given one, and disable the rest.
    paramDiv.find("tr.subparam").each(function() {
      var subparamDiv = $(this);
      if (!subparamDiv.hasClass("disabled")) {
        var subparam = subparamDiv.find("input.type").val();
        var td = subparamDiv.children("td:last");
        if (subparam == enabledSubparam) { // enable subparam
          td.removeClass("disabled")
             .find("input, textarea, select").removeAttr("disabled");
          // move the parsers next to the subparam
          if (subparam == 'data' || subparam == 'file') {
            subparamDiv.after(paramDiv.find(".parsers"));
          }
        } else if (!td.hasClass("disabled")) {
          td.addClass("disabled")
            .find("input, textarea, select").attr("disabled", "disabled");
        }
      }
    });

    // enable/disable parsers
    var parserDiv = paramDiv.find(".parsers");
    if (enabledSubparam == 'data' || enabledSubparam == 'file') {
      // enable parser
      if (parserDiv.hasClass("disabled")) {
        parserDiv.removeClass("disabled");
        parserDiv.find("input").removeAttr("disabled");
      }
    } else if (!parserDiv.hasClass("disabled")){
      parserDiv.addClass("disabled");
      parserDiv.find("input").attr("disabled", "disabled");
    }
  }

  ns.init = init;

});
