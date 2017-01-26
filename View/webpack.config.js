var webpack = require('webpack');
var node_env = process.env.NODE_ENV || 'production';
var outputPath = './dist';

if (node_env === 'production') console.log('optimizing web assets');

// shims for global style scripts
var globals = [
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
  { alias: 'lib/zynga-scroller/Scroller',               path : __dirname + '/webapp/wdk/lib/zynga-scroller/Scroller.js' },
];

var alias = globals.reduce(function(alias, global) {
  alias[global.alias + '$'] = global.path;
  return alias;
}, {});

var globalLoaders = globals.map(function(global) {
  return {
    test: global.path,
    loader: 'script-loader'
  };
});

module.exports = {
  entry: {
    'wdk-client': './webapp/wdk/js/client/index.js',
    'wdk': './webapp/wdk/js/index.js'
  },
  output: {
    path: outputPath,
    filename: '[name].bundle.js',
    library: 'Wdk'
  },
  bail: true,
  resolve: {
    extensions: [".ts", ".tsx", ".js", ".jsx"],
    alias: alias
  },
  externals: [
    { jquery: 'jQuery' }
  ],
  module: {
    rules: globalLoaders.concat([
      // expose libs as properties on `window` object in the browser
      makeExposeRule('lodash', '_'),
      makeExposeRule('react', 'React'),
      makeExposeRule('react-dom', 'ReactDOM'),
      makeExposeRule('react-router/es', 'ReactRouter'),
      makeExposeRule('react-addons-perf', 'ReactPerf'),
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              cacheDirectory: true
            }
          },
          {
            loader: 'ts-loader'
          }
        ]
      },
      {
        test: /\.jsx?$/,
        exclude: [/node_modules/, /wdk\/lib/],
        loader: 'babel-loader',
        options: {
          cacheDirectory: true
        }
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader',
            options: {
              sourceMap: true
            }
          }
        ]
      },
      {
        test: /\.(gif|png)$/,
        exclude: /node_modules/,
        loader: 'url-loader',
        options: {
          limit: 100000
        }
      },
      {
        test: /\.jpg$/,
        exclude: /node_modules/,
        loader: 'file-loader'
      }
    ])
  },
  node: {
    console: true,
    fs: 'empty'
  },
  devtool: 'source-map',
  plugins: node_env !== 'production'
    ? [
        new webpack.LoaderOptionsPlugin({
          debug: true
        }),
        new webpack.DefinePlugin({
          __DEV__: "true",
          "process.env": {
            NODE_ENV: JSON.stringify("development")
          }
        }),
        new webpack.optimize.CommonsChunkPlugin({
          name: 'wdk-client'
        })
      ]

    : [
        new webpack.LoaderOptionsPlugin({
          debug: false
        }),
        new webpack.optimize.UglifyJsPlugin({
          sourceMap: true
        }),
        new webpack.DefinePlugin({
          __DEV__: "false",
          "process.env": {
            NODE_ENV: JSON.stringify("production")
          }
        }),
        new webpack.optimize.CommonsChunkPlugin({
          name: 'wdk-client'
        })
      ]
};

function makeExposeRule(moduleIdent, globalIdent) {
  return {
    test: require.resolve(moduleIdent),
    loader: 'expose-loader?' + globalIdent
  }
}
