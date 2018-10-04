var LiveReloadPlugin = require('webpack-livereload-plugin');

module.exports = {
  entry:'./dist/app.js',
  watch: true,
  output: {
    path: __dirname + "/dist",
    filename: 'bundle.js'
  },
  optimization: {
    minimize: false
  },
  resolve: {
      extensions: [".ts", ".tsx", ".js", ".json"]
  },
  module: {
      rules: [
          { test: /\.tsx?$/, loader: "awesome-typescript-loader" },
          { enforce: "pre", test: /\.js$/, loader: "source-map-loader" }
      ]
  },
  externals: {
      "react": "React",
      "react-dom": "ReactDOM"
  },
  devServer: {
    contentBase: './'
  },
  plugins: [
    new LiveReloadPlugin()
  ]
};
