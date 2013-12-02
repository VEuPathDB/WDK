var wdkFiles = require('../wdkFiles');
var glob = require('../node_modules/grunt/node_modules/glob');
var externalRegex = /^(https?:)?\/\//;

module.exports = function(grunt) {
  grunt.registerTask('debugScript', 'Generate script tags for WDK files to load individually', function() {
    var scripts = [];

    [].concat(wdkFiles.libs, wdkFiles.src).forEach(function(pattern) {
      if (externalRegex.test(pattern)) {
        scripts = scripts.concat(pattern);
      } else {
        var theseScripts = glob.sync(pattern);
        theseScripts = (theseScripts instanceof Array) ? theseScripts : [theseScripts];
        scripts = scripts.concat(theseScripts);
      }
    });

    var scriptLoaderStr = scripts.map(function(script) {
      var line;
      if (externalRegex.test(script)) {
        line = 'document.writeln(\'<scr\' + \'ipt src="' + script + '"></scr\' + \'ipt>\');\n';
      } else {
        line = 'document.writeln(\'<scr\' + \'ipt src="\' + wdkConfig.assetsUrl + \'/wdk/' +
            script + '"></scr\' + \'ipt>\');\n';
      }
      return line;
    }).join('');

    grunt.file.write('dist/wdk.debug.js', scriptLoaderStr);

  });
};
