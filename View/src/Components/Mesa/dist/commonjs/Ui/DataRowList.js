'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _DataRow = require('../Ui/DataRow');

var _DataRow2 = _interopRequireDefault(_DataRow);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataRowListClass = (0, _Utils.makeClassifier)('DataRowList');

var DataRowList = function (_React$Component) {
  _inherits(DataRowList, _React$Component);

  function DataRowList(props) {
    _classCallCheck(this, DataRowList);

    return _possibleConstructorReturn(this, (DataRowList.__proto__ || Object.getPrototypeOf(DataRowList)).call(this, props));
  }

  _createClass(DataRowList, [{
    key: 'render',
    value: function render() {
      var props = this.props;
      var rows = props.rows;


      return _react2.default.createElement(
        'tbody',
        { className: dataRowListClass() },
        rows.map(function (row, rowIndex) {
          return _react2.default.createElement(_DataRow2.default, _extends({
            row: row,
            key: rowIndex,
            rowIndex: rowIndex
          }, props));
        })
      );
    }
  }]);

  return DataRowList;
}(_react2.default.Component);

;

exports.default = DataRowList;