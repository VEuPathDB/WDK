(function bootstrap(window) {
  'use strict';

  function assetsUrl(path) {
    return wdkConfig.assetsUrl + path;
  }

  // conditionally load some scripts
  yepnope([{
    test: !!window.JSON,
    nope: assetsUrl('/wdk/lib/json3.min.js')
  },{
    // IE 8 needs some canvas help
    test: Modernizr.canvas,
    nope: assetsUrl('/wdk/lib/excanvas.min.js')
  },{
    load: [
      '/wdk/lib/jquery.flot.min.js',
      '/wdk/lib/jquery.flot.categories.min.js',
      '/wdk/lib/jquery.flot.selection.min.js'
    ].map(assetsUrl)
  }]);

}(this));
