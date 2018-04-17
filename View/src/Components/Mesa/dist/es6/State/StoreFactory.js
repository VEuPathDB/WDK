'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _redux = require('redux');

var _Reducers = require('../State/Reducers');

var _Reducers2 = _interopRequireDefault(_Reducers);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var StoreFactory = {
  create: function create(base) {
    var reducer = (0, _Reducers2.default)(base);
    return (0, _redux.createStore)(reducer);
  }
};

exports.default = StoreFactory;