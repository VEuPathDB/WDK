var webpack = require('webpack');
var node_env = process.env.NODE_ENV || 'development';

module.exports = {
  cache: true,
  resolve: {
    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx?$/, loader: '6to5-loader' },
    ]
  },
  debug: node_env !== 'production',
  devtool: node_env === 'production' ? 'source-map' : 'inline-source-map',
  plugins: node_env !== 'production' ? null : [
    new webpack.optimize.UglifyJsPlugin(),
    new webpack.optimize.OccurenceOrderPlugin(true)
  ]
};
