/**
 * Bootstrap the wdk app. This runs on DOMReady and initializes views, etc.
 */

(function bootstrap($) {
  'use strict';

  $(wdk.cookieTest);
  $(wdk.setUpDialogs);
  $(wdk.setUpPopups);
  $(wdk.load);

}(jQuery));
