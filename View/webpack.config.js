var baseConfig = require('./base.webpack.config');

// shims for global style scripts
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
  { alias: 'lib/zynga-scroller/Animate',                path : __dirname + '/webapp/wdk/lib/zynga-scroller/Animate.js' },
  { alias: 'lib/zynga-scroller/Scroller',               path : __dirname + '/webapp/wdk/lib/zynga-scroller/Scroller.js' }
];

var alias = scripts.reduce(function(alias, script) {
  alias[script.alias + '$'] = script.path;
  return alias;
}, {});

var scriptLoaders = scripts.map(function(script) {
  return {
    test: script.path,
    loader: 'script-loader'
  };
});

// expose module exports as global vars
var exposeModules = [
  { module: 'lodash', expose: '_' },
  { module: 'react', expose: 'React' },
  { module: 'react-dom', expose: 'ReactDOM' },
  { module: 'react-router/es', expose: 'ReactRouter' },
  { module: 'react-addons-perf', expose: 'ReactPerf' }
];

var exposeLoaders = exposeModules.map(function(entry) {
  return {
    test: require.resolve(entry.module),
    loader: 'expose-loader?' + entry.expose
  };
});

module.exports = baseConfig.merge({
  entry: {
    'wdk-client': './webapp/wdk/js/client/index.js',
    'wdk': './webapp/wdk/js/index.js'
  },
  output: {
    path: './dist',
    filename: '[name].bundle.js',
    library: 'Wdk'
  },
  resolve: {
    alias: alias
  },
  externals: [
    { jquery: 'jQuery' }
  ],
  module: {
    rules: scriptLoaders.concat(exposeLoaders)
  },
  plugins: [
    new baseConfig.webpack.optimize.CommonsChunkPlugin({
      name: 'wdk-client'
    })
  ]
});
