// =============================================================================
// The js related to the display on question page
// $(document).ready(initializeQuestion);

wdk.util.namespace("wdk", function(ns) {
  "use strict";

  var question = {
    init: function(el) {
      new wdk.views.core.QuestionView({ el: el });
      wdk.onloadQuestion();
    }
  };

  ns.question = question;

});
