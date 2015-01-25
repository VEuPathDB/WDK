module.exports = {
  bail: true,
  resolve: {
    alias: {
      'wdk': __dirname + '/webapp/wdk/js'
    },

    // adding .jsx; the rest are defaults (this overwrites, so we're including them)
    extensions: ["", ".webpack.js", ".web.js", ".js", ".jsx"]
  },
  output: {
    devtoolModuleFilenameTemplate: "wdk:///[resource-path]"
  },
  module: {
    loaders: [
      { test: /^(?!.*(bower_components|node_modules))+.+\.jsx?$/, loader: '6to5-loader' }
    ]
  }
};
