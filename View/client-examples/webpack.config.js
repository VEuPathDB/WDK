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
    },
    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },

  module: {
    loaders: [
      { test: /\.jsx?$/, exclude: /node_modules/, loader: 'babel' },
      { test: /\.css$/, loader: "style-loader!css-loader?sourceMap" }
    ]
  },

  node: {
    console: true,
    fs: 'empty'
  },

  devtool: '#cheap-module-eval-source-map'
};
