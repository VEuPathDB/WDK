var path = require('path');

module.exports = {
  entry: {
    app: ['./app/main.js']
  },

  output: {
    path: path.resolve(__dirname, 'build'),
    publicPath: '/assets/',
    filename: 'bundle.js'
  },

  resolve: {
    alias: {
      'wdk-client': path.resolve(__dirname, '../webapp/wdk/js/client')
    }
  },

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

  devtool: '#cheap-module-eval-source-map'
};
