module.exports = {
  cache: true,
  context: __dirname + '/webapp/wdk/js',
  entry: './app.js',
  output: {
    filename: 'dist/wdk/js/wdk.js'
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.js$/, loader: 'traceur?runtime' },
      { test: /^(?!.*(bower_components|node_modules))+.+\.js$/, loader: 'jsx-loader' },
    ]
  }
};
