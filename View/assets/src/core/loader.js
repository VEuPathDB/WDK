(function bootstrap(window) {
  'use strict';

  function assetsUrl(path) {
    return wdkConfig.assetsUrl + path;
  }

  // conditionally load some scripts
  Modernizr.load([
    {
      test: !!window.JSON,
      nope: assetsUrl('/wdk/lib/json3.min.js')//,
      //complete: $.bind(window, wdk.load)
    },
    {
      // IE 8 needs some canvas help
      test: Modernizr.canvas,
      nope: assetsUrl('/wdk/lib/excanvas.min.js'),
      both: [
        '/wdk/lib/jquery.flot-0.8.1.min.js',
        '/wdk/lib/jquery.flot.categories-0.8.1.min.js'
      ].map(assetsUrl)
    }
  ]);

}(this));
