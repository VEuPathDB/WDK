!(function($) {
  'use strict';

  // Start the application. The ready callback is invoked
  // once the DOM has finished rendering.
  var app = wdk.core.Application.create({

    ready: function() {
      wdk.cookieTest();
      wdk.setUpDialogs();
      wdk.setUpPopups();
      wdk.load();
    }

  });

}(jQuery));
