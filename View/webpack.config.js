var webpack = require('webpack');
var node_env = process.env.NODE_ENV || 'production';
var outputPath = './dist/wdk/js';

module.exports = {
  entry: './webapp/wdk/js/app',
  output: {
    path: outputPath,
    filename: 'wdk.js'
  },
  bail: true,
  resolve: {
    alias: {
      // alias underscore to lodash, mainly for backbone
      underscore: 'lodash'
    },
    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".jsx"]
  },
  externals: [
    { jquery: 'jQuery' }
  ],
  module: {
    loaders: [
      { test: /\.tsx?$/, exclude: /node_modules/, loader: 'babel?cacheDirectory!ts' },
      { test: /\.jsx?$/, exclude: /node_modules/, loader: 'babel?cacheDirectory' },
      { test: /\.css$/,  loader: "style-loader!css-loader?sourceMap" },
      { test: /\.png$/,  exclude: /node_modules/, loader: "url-loader?limit=100000" },
      { test: /\.gif/,   exclude: /node_modules/, loader: "url-loader?limit=100000" },
      { test: /\.jpg$/,  exclude: /node_modules/, loader: "file-loader" }
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
          __DEV__: "true",
          "process.env": {
            NODE_ENV: JSON.stringify("development")
          }
        })
      ]

    : [ new webpack.optimize.UglifyJsPlugin({ mangle: false }),
        new webpack.optimize.OccurenceOrderPlugin(true),
        new webpack.DefinePlugin({
          __DEV__: "false",
          "process.env": {
            NODE_ENV: JSON.stringify("production")
          }
        })
      ]
};
