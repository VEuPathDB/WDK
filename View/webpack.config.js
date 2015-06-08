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
    'flux': './webapp/wdk/js/flux'
  },
  output: {
    path: outputPath,
    filename: 'wdk.[name].js',
    library: [ 'Wdk', '[name]' ],
    libraryTarget: 'umd'
  },
  bail: true,
  resolve: {
    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx?$/, loader: 'babel-loader' },
    ]
  },
  node: {
    console: true
  },
  debug: node_env !== 'production',
  devtool: 'source-map',
  plugins: node_env !== 'production'
    ? [ commonsPlugin ]
    : [ new webpack.optimize.UglifyJsPlugin({ mangle: false }),
        new webpack.optimize.OccurenceOrderPlugin(true),
        new webpack.DefinePlugin({
          "process.env": {
            NODE_ENV: JSON.stringify("production")
          }
        }),
        commonsPlugin ]
};
