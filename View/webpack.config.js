module.exports = {
  context: __dirname + '/webapp/wdk/js',
  entry: './app.js',
  output: {
    filename: 'dist/wdk/js/wdk.js'
  },
  module: {
    loaders: [
      // { test: /\.js$/, loader: '6to5-loader?modules=commonInterop', exclude: /node_modules/ }
      { test: /\.js$/, loader: 'jsx-loader?stripTypes,harmony', exclude: /node_modules/ }
    ]
  }
};
