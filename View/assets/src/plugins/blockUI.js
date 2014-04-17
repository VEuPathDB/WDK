!(function($) {
  // Override BlockUI CSS and message
  $.blockUI.defaults.overlayCSS.opacity = 0.2;
  $.blockUI.defaults.message = '<span class="h2center">Please wait...</span>';
  // allow rules in external CSS to be effective
  $.blockUI.defaults.css = {};
}(jQuery));
