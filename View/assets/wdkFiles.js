module.exports = {

  src: [
    'src/core/console.js',
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
    'lib/json.js',

    // question pages
    'lib/handlebars.js',
    'lib/chosen.jquery.min.js',
    'lib/jquery.dataTables-1.9.0.min.js',
    'lib/jstree/jquery.jstree.js',
    'lib/jquery.flot-0.8.1.min.js',
    'lib/jquery.flot.categories-0.8.1.min.js'
  ]

};
