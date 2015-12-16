var webpack = require('webpack');
var node_env = process.env.NODE_ENV || 'production';
var outputPath = './dist/wdk/js';

var commonsPlugin = new webpack.optimize.CommonsChunkPlugin({
  name: 'common',
  path: outputPath,
  filename: 'wdk.common.js'
});

module.exports = {
  entry: {
    'app': './webapp/wdk/js/app',
    'client': './webapp/wdk/js/client/main'
  },
  output: {
    path: outputPath,
    filename: 'wdk.[name].js',
    library: [ 'Wdk', '[name]' ],
    libraryTarget: 'umd'
  },
  bail: true,
  resolve: {
    alias: {
      // alias underscore to lodash, mainly for backbone
      underscore: 'lodash'
    },
    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },
  externals: [
    { jquery: 'jQuery' }
  ],
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx?$/, loader: 'babel-loader' },
      { test: /\.css$/, loader: "style-loader!css-loader?sourceMap" }
    ]
  },
  node: {
    console: true,
    fs: 'empty'
  },
  debug: node_env !== 'production',
  devtool: 'source-map',
  plugins: node_env !== 'production'
    ? [
        new webpack.DefinePlugin({
          __DEV__: "true"
        }),
        commonsPlugin
      ]

    : [ new webpack.optimize.UglifyJsPlugin({ mangle: false }),
        new webpack.optimize.OccurenceOrderPlugin(true),
        new webpack.DefinePlugin({
          __DEV__: "false",
          "process.env": {
            NODE_ENV: JSON.stringify("production")
          }
        }),
        commonsPlugin ]
};
