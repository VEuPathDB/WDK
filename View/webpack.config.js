var path = require('path');
var baseConfig = require('./base.webpack.config');

// Shims for global style scripts
// These will expose global varables on the `window` object.
// For instance, `window.$`
var scripts = [
  { alias: 'lib/jquery',                                path : __dirname + '/webapp/wdk/lib/jquery.js' },
  { alias: 'lib/jquery-migrate',                        path : __dirname + '/webapp/wdk/lib/jquery-migrate-1.2.1.min.js' },
  { alias: 'lib/jquery-ui',                             path : __dirname + '/webapp/wdk/lib/jquery-ui.js' },
  { alias: 'lib/jquery-cookie',                         path : __dirname + '/webapp/wdk/lib/jquery.cookie.js' },
  { alias: 'lib/jquery-blockUI',                        path : __dirname + '/webapp/wdk/lib/jquery.blockUI.js' },
  { alias: 'lib/flexigrid',                             path : __dirname + '/webapp/wdk/lib/flexigrid.js' },
  { alias: 'lib/select2',                               path : __dirname + '/webapp/wdk/lib/select2.min.js' },
  { alias: 'lib/jquery-jstree',                         path : __dirname + '/webapp/wdk/lib/jstree/jquery.jstree.js' },
  { alias: 'lib/jquery-qtip',                           path : __dirname + '/webapp/wdk/lib/jquery.qtip.min.js' },
  { alias: 'lib/jquery-flot',                           path : __dirname + '/webapp/wdk/lib/flot/jquery.flot.min.js' },
  { alias: 'lib/jquery-flot-categories',                path : __dirname + '/webapp/wdk/lib/flot/jquery.flot.categories.min.js' },
  { alias: 'lib/jquery-flot-selection',                 path : __dirname + '/webapp/wdk/lib/flot/jquery.flot.selection.min.js' },
  { alias: 'lib/jquery-flot-time',                      path : __dirname + '/webapp/wdk/lib/flot/jquery.flot.time.min.js' },
  { alias: 'lib/jquery-datatables',                     path : __dirname + '/webapp/wdk/lib/datatables.min.js' },
  { alias: 'lib/jquery-datatables-natural-type-plugin', path : __dirname + '/webapp/wdk/lib/datatables-natural-type-plugin.js' },
  { alias: 'zynga-scroller',                            path : __dirname + '/lib/zynga-scroller/src' }
];

// Create webpack alias configuration object
var alias = scripts.reduce(function(alias, script) {
  alias[script.alias + '$'] = script.path;
  return alias;
}, { });

// Create webpack script-loader configuration object
var scriptLoaders = scripts.map(function(script) {
  return {
    test: script.path,
    loader: 'script-loader'
  };
});

// expose module exports as global vars
var exposeModules = [
  { module: 'flux',           expose : 'Flux' },
  { module: 'flux/utils',     expose : 'FluxUtils' },
  { module: 'history',        expose : 'HistoryJS' },
  { module: 'lodash',         expose : '_' },
  { module: 'natural-sort',   expose : 'NaturalSort' },
  { module: 'prop-types',     expose : 'ReactPropTypes' },
  { module: 'react',          expose : 'React' },
  { module: 'react-dom',      expose : 'ReactDOM' },
  { module: 'react-router',   expose : 'ReactRouter' },
  { module: 'rxjs',           expose : 'Rx' },
  { module: 'rxjs/operators', expose : 'RxOperators' },
];

var primaryConfig = {
  entry: {
    'wdk-client': exposeModules.map(function(entry) {
      return 'expose-loader?' + entry.expose + '!' + entry.module;
    }).concat([
      'whatwg-fetch',
      './webapp/wdk/css/wdk.css',
      './src/Core/Style/index.scss',
      './src/Core/index.ts'
    ]),
    'wdk': [
      './webapp/wdk/css/wdk.css',
      './src/Core/Style/index.scss',
      './webapp/wdk/js/index.js'
    ]
  },
  output: {
    library: 'Wdk'
  },
  resolve: {
    modules: [
      path.resolve(__dirname, 'lib'),
      path.resolve(__dirname, 'src'),
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
      minChunks: function(module){
        return module.context && module.context.includes("node_modules");
      }
    }),
  ]
};

module.exports = baseConfig.merge([ primaryConfig ]);
