let req = require.context('./');

Object.defineProperty(exports, '__esModule', { value: true });

for (let key of req.keys()) {
  if (key === './index' || key.endsWith('.js')) continue;
  exports[key.slice(2)] = req(key);
}
