module.exports = {

  src: [
    'src/core/console.js',
    'src/core/bootstrap.js',
    'src/core/namespace.js',
    'src/core/event.js',
    'src/core/common.js',
    'src/core/util.js',

    'src/user.js',
    'src/components/**/*.js',
    'src/controllers/**/*.js',
  ],

  libs: [
    'lib/es5-shim.min.js',
    'lib/jquery.js',
    'lib/jquery-ui.js',
    'lib/jquery.cookie.js',
    'lib/jquery.blockUI.js',
    'lib/qtip2/jquery.qtip.js',

    // use JSON.parse and JSON.stringify - polyfilled for older browsers by es5-shim.min.js
    // 'lib/json.js',

    // question pages
    'lib/handlebars.js',
    'lib/flexigrid.js',
    'lib/chosen.jquery.min.js',
    'lib/jquery.dataTables.min.js',
    'lib/jstree/jquery.jstree.js',
    'lib/jquery.flot-0.8.1.min.js',
    'lib/jquery.flot.categories-0.8.1.min.js'
  ]

};
