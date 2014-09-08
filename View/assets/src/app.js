/*global RSVP */
(function($) {
  'use strict';

  // Don't allow RSVP to "swallow" errors.
  // This will cause inspectors to break
  // here, from which one can look up the
  // call chain.
  RSVP.on('error', function(err) {
    console.assert(false, err);
  });

  // Start the application. The ready callback is invoked
  // once the DOM has finished rendering.
  var app = wdk.application = wdk.app = wdk.core.Application.create({

    ready: function wdkReady() {
      wdk.cookieTest();
      wdk.setUpDialogs();
      wdk.setUpPopups();

      // call wdk.load trigger DOM-based functions
      setInterval(wdk.load, 200);
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

  // Global event handlers
  // need to call draw on dataTables that are children of a tab panel
  $(document).on('tabsactivate', function() {
    $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
  });

  $(window).on('resize', _.throttle(function() {
    $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
  }, 100));

}(jQuery));
