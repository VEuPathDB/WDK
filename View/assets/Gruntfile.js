module.exports = function(grunt) {

  var wdkFiles = require('./wdkFiles'),
      path = require('path');

  grunt.initConfig({

    concat: {
      js: {
        options: {
          process: function(src, filepath) {
            // wrap files in an immediately invoked function wrapper
            // some libraries misbehave and add "use strict" pragma in global scope
            return '(function(){' + src + '}());';
          }
        },
        src: wdkFiles.libs,
        dest: 'dist/wdk.libs.js'
      }
    },

    uglify: {
      options: {
        mangle: {
          except: ['wdk']
        },
        report: true,
        sourceMap: 'dist/wdk.js.map',
        sourceMappingURL: 'wdk.js.map',
        // sourceMapPrefix: 1
      },
      wdk: {
        files: {
          'dist/wdk.js': wdkFiles.src,
        }
      }
    },

    copy: {
      js: {
        files: [
          {
            expand: true,
            //cwd: 'js',
            src: ['src/**', 'lib/**'],
            dest: 'dist'
          }
        ]
      },
      css: {
        files: [
          {
            expand: true,
            cwd: 'css',
            src: ['**/*'],
            dest: 'dist/css'
          }
        ]
      },
      images: {
        files: [
          {
            expand: true,
            cwd: 'images',
            src: ['**/*'],
            dest: 'dist/images'
          }
        ]
      }
    },

    clean: {
      dist: 'dist'
    }

  });

  grunt.registerTask('scriptTags', 'Generate script tags for WDK files to load individually', function() {
    var glob = require('./node_modules/grunt/node_modules/glob'),
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
    grunt.file.write('dist/wdkScriptLoader.js', scriptLoaderStr);
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');

  grunt.registerTask('dist', ['clean', 'concat', 'uglify', 'copy', 'scriptTags']);

  grunt.registerTask('default', ['dist']);

};

