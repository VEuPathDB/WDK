var wdkFiles = require('../wdkFiles');

module.exports = function(grunt) {
  grunt.registerTask('debugScript', 'Generate script tags for WDK files to load individually', function() {
    var glob = require('../node_modules/grunt/node_modules/glob'),
        scripts = [],
        scriptLoaderStr = '';

    [].concat(wdkFiles.libs, wdkFiles.src).forEach(function(pattern) {
      var theseScripts = glob.sync(pattern);
      theseScripts = (theseScripts instanceof Array) ? theseScripts : [theseScripts];
      scripts = scripts.concat(theseScripts);
    });

    scriptLoaderStr = scripts.map(function(script) {
      return 'document.writeln(\'<scr\' + \'ipt src="\' + wdkConfig.assetsUrl + \'/wdk/' +
          script + '"></scr\' + \'ipt>\');';
    }).join('');

    grunt.file.write('dist/wdk.debug.js', scriptLoaderStr);

  });
};
