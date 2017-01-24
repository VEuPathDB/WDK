let re = /\.(js|jsx|ts|tsx)$/

// FIXME Remove this, which will require changing how component overrides are
// implemented.  Hardcoding removale of ./Table for now
let req = require.context('./', false, /^(?!\.\/Table\.?)/)

Object.defineProperty(exports, '__esModule', { value: true });

for (let key of req.keys()) {
  if (key === './index' || re.test(key)) continue;
  exports[key.slice(2)] = req(key).default;
}
