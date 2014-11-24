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
    'webapp/wdk/js/core/console.js',
    'webapp/wdk/js/core/namespace.js',
    'webapp/wdk/js/core/fn.js',
    'webapp/wdk/js/core/c_properties.js',
    'webapp/wdk/js/core/base_class.js',
    'webapp/wdk/js/core/runloop.js',
    'webapp/wdk/js/core/container.js',
    'webapp/wdk/js/core/application.js',
    'webapp/wdk/js/core/common.js',
    'webapp/wdk/js/core/event.js',
    'webapp/wdk/js/core/util.js',

    'webapp/wdk/js/user.js',
    'webapp/wdk/js/models/filter/field.js',
    'webapp/wdk/js/models/filter/filter.js',
    'webapp/wdk/js/models/filter/filter_service.js',

    'webapp/wdk/js/plugins/**/*.js',

    'webapp/wdk/js/components/**/*.js',

    'webapp/wdk/js/views/core/view.js',
    'webapp/wdk/js/views/core/**/*.js',

    // filter views
    'webapp/wdk/js/views/filter/field_list_view.js',
    'webapp/wdk/js/views/filter/range_filter_view.js',
    'webapp/wdk/js/views/filter/membership_filter_view.js',
    'webapp/wdk/js/views/filter/field_detail_view.js',
    'webapp/wdk/js/views/filter/filter_fields_view.js',
    'webapp/wdk/js/views/filter/results_view.js',
    'webapp/wdk/js/views/filter/filter_item_view.js',
    'webapp/wdk/js/views/filter/filter_items_view.js',

    'webapp/wdk/js/views/strategy/**/*.js',

    'webapp/wdk/js/controllers/**/*.js',

    'webapp/wdk/js/app.js'

  ],

  libs: [
    'webapp/wdk/lib/es5-shim.min.js',
    'webapp/wdk/lib/rsvp.min.js',
    'webapp/wdk/lib/jquery.js',
    'ENV:DEV!webapp/wdk/lib/jquery-migrate-1.2.1.js',
    'ENV:PROD!webapp/wdk/lib/jquery-migrate-1.2.1.min.js',
    //'webapp/wdk/lib/underscore-min.js',
    'webapp/wdk/lib/lodash.compat.min.js',
    'webapp/wdk/lib/backbone-min.js',
    'webapp/wdk/lib/jquery-ui.js',

    'webapp/wdk/lib/jquery.cookie.js',
    'webapp/wdk/lib/jquery.blockUI.js',
    'webapp/wdk/lib/jquery.qtip.min.js',

    // question pages
    // 'webapp/wdk/lib/handlebars.js',
    'webapp/wdk/lib/handlebars.runtime.min-v1.3.0.js',
    'webapp/wdk/lib/flexigrid.js',
    'webapp/wdk/lib/select2.min.js',
    'webapp/wdk/lib/jquery.flot.min.js',
    'webapp/wdk/lib/jquery.flot.categories.min.js',
    'webapp/wdk/lib/jquery.flot.selection.min.js',
    'webapp/wdk/lib/jquery.dataTables.min.js',
    'webapp/wdk/lib/dataTables.colVis.min.js',
    'webapp/wdk/lib/jstree/jquery.jstree.js',
    'webapp/wdk/lib/spin.min.js'
  ]

};
