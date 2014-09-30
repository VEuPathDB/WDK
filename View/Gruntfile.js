module.exports = function(grunt) {

  var wdkFiles = require('./wdkFiles');
  var helpers = require('./tasks/helpers');

  grunt.initConfig({

    jshint: {
      options: {
        jshintrc: true,
      },
      wdk: wdkFiles.src
    },

    concat: {
      js: {
        src: helpers.filterByFlag('env', 'prod', wdkFiles.libs),
        dest: 'dist/wdk/js/wdk.libs.js'
      }
    },

    handlebars: {
      compile: {
        options: {
          namespace: 'wdk.templates',
          processName: function(filePath) {
            return filePath.replace(/^webapp\/wdk\/js\/templates\//, '');
          }
        },
        files: {
          'dist/wdk/js/wdk.templates.js': ['webapp/wdk/js/templates/**/*.handlebars']
        }
      }
    },

    uglify: {
      options: {
        mangle: {
          except: ['wdk']
        },
        compress: {
          drop_console: true
        },
        report: true,
        sourceMap: 'dist/wdk/js/wdk.js.map',
        sourceMappingURL: 'wdk.js.map',
        sourceMapPrefix: 3
      },
      wdk: {
        files: {
          'dist/wdk/js/wdk.js': [].concat('dist/wdk/js/wdk.templates.js', wdkFiles.src),
        }
      }
    },

    cssmin: {
      wdk: {
        src: ['webapp/wdk/css/wdk.css'],
        dest: 'dist/wdk/css/wdk.min.css',
      },
      libs: {
        src: ['webapp/wdk/css/wdk.libs.css'],
        dest: 'dist/wdk/css/wdk.libs.min.css',
      }
    },

    // copy: {
    //   webapp: {
    //     files: [{
    //       expand: true,
    //       cwd: 'webapp',
    //       src: 'wdk/**',
    //       dest: 'dist'
    //     }]
    //   },
    //   js: {
    //     files: [
    //       {
    //         expand: true,
    //         //cwd: 'js',
    //         src: ['src/**', 'lib/**'],
    //         dest: 'dist/wdk'
    //       }
    //     ]
    //   },
    //   css: {
    //     files: [
    //       {
    //         expand: true,
    //         cwd: 'css',
    //         src: ['**/*'],
    //         dest: 'dist/wdk/css'
    //       }
    //     ]
    //   },
    //   images: {
    //     files: [
    //       {
    //         expand: true,
    //         cwd: 'images',
    //         src: ['**/*'],
    //         dest: 'dist/wdk/images'
    //       }
    //     ]
    //   }
    // },

    clean: {
      wdk: [ 'dist' ]
    }

  });

  grunt.loadTasks('tasks');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-handlebars');

  grunt.registerTask('dist', ['jshint', 'clean', 'concat', 'handlebars', 'uglify', 'cssmin', 'debugScript:dist/wdk/js/wdk.debug.js']);

  grunt.registerTask('default', ['dist']);

};

