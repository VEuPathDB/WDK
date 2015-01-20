module.exports = {
  context: __dirname + '/test',
  resolve: {
    alias: {
      'wdk': __dirname + '/webapp/wdk/js'
    },

    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },
  entry: './tests.js',
  output: {
    filename: 'test/dist/tests.js',
    devtoolModuleFilenameTemplate: "wdk:///[resource-path]"
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx?$/, loader: 'traceur?runtime' },
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx$/, loader: 'jsx-loader' }
    ]
  }
};
