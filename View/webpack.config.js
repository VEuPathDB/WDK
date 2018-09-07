var path = require('path');
var baseConfig = require('../../WDKClient/Build/base.webpack.config');

var scriptRoot = path.join(__dirname, '../../WDKClient/Dependencies/lib');
var depPath = path.join.bind(null, scriptRoot);


var aliases = {
  'wdk-client': path.join(__dirname, '../../WDKClient/Client/src/Core'),
}

// Shims for global style scripts
// These will expose global varables on the `window` object.
// For instance, `window.$`
// TODO Migrate to npm/yarn packages
var scripts = [
  { alias: 'lib/jquery',                                 path: depPath('jquery.js') },
  { alias: 'lib/jquery-migrate',                         path: depPath('jquery-migrate-1.2.1.js') },
  { alias: 'lib/jquery-ui',                              path: depPath('jquery-ui.js') },
  { alias: 'lib/flexigrid',                              path: depPath('flexigrid.js') },
  { alias: 'lib/jquery-blockUI',                         path: depPath('jquery.blockUI.js') },
  { alias: 'lib/jquery-cookie',                          path: depPath('jquery.cookie.js') },
  { alias: 'lib/jquery-datatables',                      path: depPath('datatables.js') },
  { alias: 'lib/jquery-datatables-natural-type-plugin',  path: depPath('datatables-natural-type-plugin.js') },
  { alias: 'lib/jquery-flot',                            path: depPath('flot/jquery.flot.js') },
  { alias: 'lib/jquery-flot-categories',                 path: depPath('flot/jquery.flot.categories.js') },
  { alias: 'lib/jquery-flot-selection',                  path: depPath('flot/jquery.flot.selection.js') },
  { alias: 'lib/jquery-flot-time',                       path: depPath('flot/jquery.flot.time.js') },
  { alias: 'lib/jquery-jstree',                          path: depPath('jstree/jquery.jstree.js') },
  { alias: 'lib/jquery-qtip',                            path: depPath('jquery.qtip.min.js') },
  { alias: 'lib/select2',                                path: depPath('select2.min.js') },
];

// polyfills
var polyfills = [
  'babel-polyfill',
  'custom-event-polyfill',
  'whatwg-fetch'
];

// expose module exports as global vars
var exposeModules = [
  { module: 'flux',              expose: 'Flux' },
  { module: 'flux/utils',        expose: 'FluxUtils' },
  { module: 'history',           expose: 'HistoryJS' },
  { module: 'lodash',            expose: '_' },
  { module: 'lodash/fp',         expose: '_fp' },
  { module: 'natural-sort',      expose: 'NaturalSort' },
  { module: 'prop-types',        expose: 'ReactPropTypes' },
  { module: 'react',             expose: 'React' },
  { module: 'react-dom',         expose: 'ReactDOM' },
  { module: 'react-redux',       expose: 'ReactRedux' },
  { module: 'react-router',      expose: 'ReactRouter' },
  { module: 'redux',             expose: 'Redux' },
  { module: 'redux-observable',  expose: 'ReduxObservable' },
  { module: 'rxjs',              expose: 'Rx' },
  { module: 'rxjs/operators',    expose: 'RxOperators' },
];


// Create config
// -------------

// Create webpack alias configuration object
var alias = scripts.reduce(function(alias, script) {
  alias[script.alias + '$'] = script.path;
  return alias;
}, aliases);

// Create webpack script-loader configuration object
var scriptLoaders = scripts.map(function(script) {
  return {
    test: script.path,
    loader: 'script-loader'
  };
});

var exposeEntries = exposeModules.map(function(entry) {
  return 'expose-loader?' + entry.expose + '!' + entry.module;
});

var primaryConfig = {
  entry: {
    'wdk-client': [].concat(
      polyfills,
      exposeEntries,
      'lib/jquery',
      'lib/jquery-migrate',
      'lib/jquery-ui',
      'lib/jquery-qtip',
      './webapp/wdk/css/wdk.css',
      'wdk-client/Style/index.scss',
      'wdk-client'
    ),

    'wdk': [].concat(
      './webapp/wdk/css/wdk.css',
      'wdk-client/Style/index.scss',
      './webapp/wdk/js/index.js'
    )
  },
  output: {
    library: 'Wdk'
  },
  resolve: {
    modules: [
      // path.resolve(__dirname, 'lib'),
      path.resolve(__dirname, '../../WDKClient/Client/node_modules'),
      path.resolve(__dirname, 'node_modules')
    ],
    alias: alias
  },
  externals: [
    { jquery: 'jQuery' }
  ],
  module: {
    rules: [ ].concat(scriptLoaders),
  },
  plugins: [
    new baseConfig.webpack.optimize.CommonsChunkPlugin({
      name: 'wdk-client'
    }),
    new baseConfig.webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      minChunks: function(module) {
        return (
          module.context && (
            module.context.includes('node_modules') ||
            module.context.includes(scriptRoot)
          )
        );
      }
    }),
  ]
};

module.exports = baseConfig.merge([ primaryConfig ]);
