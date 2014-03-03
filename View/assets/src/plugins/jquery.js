!(function($) {
  // This is wrong:
  //   1. This gets called before tab data is loaded (as of jQuery UI 1.9?)
  //   2. This gets called for _all_ ajax requests, which is unecessary.
  $(document).ajaxSuccess(function(event, xhr, ajaxOptions) {
    xhr.done(wdk.load);
  });
}(jQuery));
