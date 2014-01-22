module.exports = function(grunt) {

  var wdkFiles = require('./wdkFiles');

  grunt.initConfig({

    concat: {
      js: {
        // this was preventing creating of some global variables
        // options: {
        //   process: function(src, filepath) {
        //     // wrap files in an immediately invoked function wrapper
        //     // some libraries misbehave and add "use strict" pragma in global scope
        //     return '(function(){' + src + '}());';
        //   }
        // },
        src: wdkFiles.libs,
        dest: 'dist/wdk/wdk.libs.js'
      }
    },

    uglify: {
      options: {
        mangle: {
          except: ['wdk']
        },
        report: true,
        sourceMap: 'dist/wdk/wdk.js.map',
        sourceMappingURL: 'wdk.js.map',
        // sourceMapPrefix: 1
      },
      wdk: {
        files: {
          'dist/wdk/wdk.js': wdkFiles.src,
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
            dest: 'dist/wdk'
          }
        ]
      },
      css: {
        files: [
          {
            expand: true,
            cwd: 'css',
            src: ['**/*'],
            dest: 'dist/wdk/css'
          }
        ]
      },
      images: {
        files: [
          {
            expand: true,
            cwd: 'images',
            src: ['**/*'],
            dest: 'dist/wdk/images'
          }
        ]
      }
    },

    clean: {
      dist: 'dist'
    }

  });

  grunt.loadTasks('tasks');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');

  grunt.registerTask('dist', ['clean', 'concat', 'uglify', 'copy', 'debugScript']);

  grunt.registerTask('default', ['dist']);

};

