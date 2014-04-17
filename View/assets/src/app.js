!(function($) {
  'use strict';

  // Start the application. The ready callback is invoked
  // once the DOM has finished rendering.
  var app = wdk.application = wdk.app = wdk.core.Application.create({

    ready: function() {
      wdk.cookieTest();
      wdk.setUpDialogs();
      wdk.setUpPopups();
      wdk.load();
    }

  });

  // Sugar to register custom question views.
  // wdk.views.QuestionView delegates to these views
  wdk.questionView = function registerQuestionView() {
    var name = 'question:' + arguments[0];
    var rest = [].slice.call(arguments, 1);
    var args = [].concat(name, rest);
    app.registerView.apply(app, args);
    return wdk;
  };

}(jQuery));
