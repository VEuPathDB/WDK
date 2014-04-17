var wdkFiles = require('../wdkFiles');
var filterByFlag = require('./helpers').filterByFlag;

var glob = require('../node_modules/grunt/node_modules/glob');
var externalRegex = /^(https?:)?\/\//;

function expandGlob(files, pattern) {
  // var files = [];
  // if (externalRegex.test(pattern)) {
  //   files = files.concat(pattern);
  // } else {
  //   files = files.concat(glob.sync(pattern));
  // }
  // return files
  return externalRegex.test(pattern)
    ? files.concat(pattern)
    : files.concat(glob.sync(pattern));
}

module.exports = function(grunt) {
  grunt.registerTask('debugScript', 'Generate script tags for WDK files to load individually', function() {
    var scripts = [].concat(
      filterByFlag('env', 'dev', wdkFiles.libs).reduce(expandGlob, []),
      'wdk.templates.js',
      filterByFlag('env', 'dev', wdkFiles.src).reduce(expandGlob, [])
    );

    var scriptLoaderStr = scripts.map(function(script) {
      var line;
      if (externalRegex.test(script)) {
        line = 'document.writeln(\'<script src="' + script + '">\\x3c/script>\');\n';
      } else {
        line = 'document.writeln(\'<script src="\' + wdkConfig.assetsUrl + \'/wdk/' +
            script + '">\\x3c/script>\');\n';
      }
      return line;
    }).join('');

    grunt.file.write('dist/wdk/wdk.debug.js', scriptLoaderStr);

  });
};
