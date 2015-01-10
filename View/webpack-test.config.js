module.exports = {
  context: __dirname + '/test',
  resolve: {
    alias: {
      'wdk': __dirname + '/webapp/wdk/js'
    }
  },
  entry: './tests.js',
  output: {
    filename: 'test/dist/tests.js',
    devtoolModuleFilenameTemplate: "wdk:///[resource-path]"
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.js$/, loader: 'traceur?runtime' },
      { test: /^(?!.*(bower_components|node_modules))+.+\.js$/, loader: 'jsx-loader' }
    ]
  }
};
