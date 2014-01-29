/**
 * List files to be bunled here.
 *
 * Files under the src key will be combined to wdk.js.
 * Files under the libs key will be combined to wdk.libs.js.
 *
 * A file can be flagged for a specific environment, such as dev or env
 * by prepending the flag name and flag value to the filename, e.g.:
 *
 *     ...
 *     'ENV:DEV!jquery.js',
 *     'ENV:PROD!jquery.min.js',
 *     ...
 *
 * At this time, 'env' is the only flag, and 'dev' and 'prod' are the
 * only acceptable values. Flags are case-insensitve, so the following
 * will also work, which may improve readability:
 *
 *     ...
 *     'env:dev!jquery.js',
 *     'env:prod!jquery.min.js',
 *     ...
 *
 */
module.exports = {

  src: [
    'src/core/console.js',
    'src/core/namespace.js',
    'src/core/c_properties.js',
    'src/core/base_object.js',
    'src/core/runloop.js',
    'src/core/container.js',
    'src/core/application.js',
    'src/core/common.js',
    'src/core/bootstrap.js',
    'src/core/event.js',
    'src/core/util.js',

    'src/user.js',
    'src/components/**/*.js',
    'src/controllers/**/*.js'
  ],

  libs: [
    'lib/es5-shim.min.js',
    'lib/modernizr.js',
    'lib/jquery-1.11.0.min.js',
    'ENV:DEV!lib/jquery-migrate-1.2.1.js',
    'ENV:PROD!lib/jquery-migrate-1.2.1.min.js',
    'lib/jquery-ui.js',

    // load polyfills
    'src/core/loader.js',

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

    // these will get loaded in bootstrap.js by Modernizr.load
    // 'lib/jquery.flot-0.8.1.min.js',
    // 'lib/jquery.flot.categories-0.8.1.min.js'

  ]

};
