// Create a dynamic module using webpack specific functionality, and CommonJS
// (i.e., Node.js) module syntax. See http://webpack.github.io/docs/context.html#require-context
//
// The end result is that this module will export every module in the
// components directory. This will allow us to expose these modules
// like `Wdk.client.component.Answer`.
let contexts = {
  ActionCreators: require.context('./actioncreators'),
  Components: require.context('./components'),
  Stores: require.context('./stores')
};

for (let modName in contexts) {
  let req = contexts[modName];
  let mod = exports[modName] = {};
  // Each key is the name of a file in the "components" directory.
  // E.g., "./Answer"
  for (let key of req.keys()) {
    // remove leading "./" from key
    let name = key.slice(2);
    // Assign the value of the module to a named export.
    // This is using CommonJS module syntax which allows dynamically named exports.
    mod[name] = req(key);
  }
}
