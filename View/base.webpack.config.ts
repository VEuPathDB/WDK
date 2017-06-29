// Base webpack configuration that can be shared amongst packages.
//
// Provide the ability to bundle typescript and es-latest authored source code
// into es5 code. Also handles minification, when appropriate.
//
// Exports a function that can be called with more configuration options.
// Those options will be merged with `baseConfig`, and the result will be
// returned.

import * as webpack from 'webpack';
import * as webpackMerge from 'webpack-merge';
import * as ExtractTextPlugin from 'extract-text-webpack-plugin';

type Env = {
  production: true;
  development?: false;
} | {
  production?: false;
  development: true;
};

/**
 * Creates a configuration function that is used by webpack. Takes a
 * configuration object, or an array of configuration objects, and merges them
 * with the base configuration object provided by WDK.
 *
 * For details, see https://webpack.js.org/configuration/.
 *
 * @param {object|object[]} additionalConfig
 */
export function merge(...additionConfig: webpack.Configuration[]): webpack.Configuration {
  return function (env: Env = { development: true }) {
    console.log('webpack env', env);
    var isDevelopment = !env.production;
    if (!isDevelopment) console.log('optimizing web assets');
    return webpackMerge({
      bail: true,
      resolve: {
        extensions: [ ".js", ".jsx", ".ts", ".tsx" ]
      },
      module: {
        rules: [

          // handle typescript source. reads `tsconfig.json` in cwd
          {
            test: /\.tsx?$/,
            exclude: /node_modules/,
            use: [
              { loader: 'babel-loader', options: { cacheDirectory: true } },
              { loader: 'ts-loader' }
            ]
          },

          // handle es source. reads `.babelrc` in cwd
          {
            test: /\.jsx?$/,
            exclude: /node_modules/,
            use: [
              { loader: 'babel-loader', options: { cacheDirectory: true } }
            ]
          },

          {
            test: /\.css$/,
            use: ExtractTextPlugin.extract({
              use: {
                loader: 'css-loader',
                options: {
                  sourceMap: true,
                  minimize: !isDevelopment
                }
              },
              fallback: 'style-loader'
            })
          },

          // inlines images as base64
          {
            test: /\.(gif|png)$/,
            exclude: /node_modules/,
            loader: 'url-loader',
            options: { limit: 100000 }
          },

          {
            test: /\.jpg$/,
            exclude: /node_modules/,
            loader: 'file-loader'
          }
        ]
      },
      node: {
        console: true,
        fs: 'empty'
      },
      devtool: 'source-map',
      plugins: [
        new webpack.LoaderOptionsPlugin({ debug: isDevelopment }),
        new webpack.DefinePlugin({
          __DEV__: JSON.stringify(isDevelopment),
          "process.env": {
            NODE_ENV: JSON.stringify(isDevelopment ? "development" : "production")
          }
        }),
        isDevelopment ? noop : new webpack.optimize.UglifyJsPlugin({ sourceMap: true }),
        new ExtractTextPlugin('[name].bundle.css')
      ]
    }, ...additionConfig);
  }
}

// expose webpack in case consumers want to add more plugins
export { webpack };

/** no nothing */
function noop(){}
