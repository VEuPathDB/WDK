// Base webpack configuration that can be shared amongst packages.
//
// Provide the ability to bundle typescript and es-latest authored source code
// into es5 code. Also handles minification, when appropriate.
//
// Exports a function that can be called with more configuration options.
// Those options will be merged with `baseConfig`, and the result will be
// returned.

var webpack = require('webpack');
var webpackMerge = require('webpack-merge');
var isDevelopment = (process.env.NODE_ENV || 'production') !== 'production';

if (!isDevelopment) console.log('optimizing web assets');

exports.config = {
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

      // handle css source.
      {
        test: /\.css$/,
        use: [
          { loader: 'style-loader' },
          { loader: 'css-loader', options: { sourceMap: true } }
        ]
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
    isDevelopment ? noop : new webpack.optimize.UglifyJsPlugin({ sourceMap: true })
  ]
};

// partially apply webpackMerge with base config
exports.merge = webpackMerge.bind(null, exports.config);

// expose webpack in case consumers want to add more plugins
exports.webpack = webpack;

/** no nothing */
function noop(){}
