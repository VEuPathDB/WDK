'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Templates = require('../Templates');

var _Templates2 = _interopRequireDefault(_Templates);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataCellClass = (0, _Utils.makeClassifier)('DataCell');

var DataCell = function (_React$PureComponent) {
  _inherits(DataCell, _React$PureComponent);

  function DataCell(props) {
    _classCallCheck(this, DataCell);

    var _this = _possibleConstructorReturn(this, (DataCell.__proto__ || Object.getPrototypeOf(DataCell)).call(this, props));

    _this.renderContent = _this.renderContent.bind(_this);
    return _this;
  }

  _createClass(DataCell, [{
    key: 'renderContent',
    value: function renderContent() {
      var _props = this.props,
          row = _props.row,
          column = _props.column,
          rowIndex = _props.rowIndex,
          columnIndex = _props.columnIndex,
          inline = _props.inline;
      var key = column.key,
          getValue = column.getValue;

      var value = typeof getValue === 'function' ? getValue({ row: row, key: key }) : row[key];
      var cellProps = { key: key, value: value, row: row, column: column, rowIndex: rowIndex, columnIndex: columnIndex };

      if ('renderCell' in column) {
        return column.renderCell(cellProps);
      }

      if (!column.type) return _Templates2.default.textCell(cellProps);

      switch (column.type.toLowerCase()) {
        case 'link':
          return _Templates2.default.linkCell(cellProps);
        case 'number':
          return _Templates2.default.numberCell(cellProps);
        case 'html':
          return _Templates2.default[inline ? 'textCell' : 'htmlCell'](cellProps);
        case 'text':
        default:
          return _Templates2.default.textCell(cellProps);
      };
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          column = _props2.column,
          row = _props2.row,
          inline = _props2.inline;
      var style = column.style,
          width = column.width,
          className = column.className,
          key = column.key;


      var whiteSpace = !inline ? {} : {
        textOverflow: 'ellipsis',
        overflow: 'hidden',
        maxWidth: options.inlineMaxWidth ? options.inlineMaxWidth : '20vw',
        maxHeight: options.inlineMaxHeight ? options.inlineMaxHeight : '2em'
      };

      width = typeof width === 'number' ? width + 'px' : width;
      width = width ? { width: width, maxWidth: width, minWidth: width } : {};
      style = Object.assign({}, style, width, whiteSpace);
      className = dataCellClass() + (className ? ' ' + className : '');
      var children = this.renderContent();
      var props = { style: style, children: children, key: key, className: className };

      return column.hidden ? null : _react2.default.createElement('td', props);
    }
  }]);

  return DataCell;
}(_react2.default.PureComponent);

;

DataCell.propTypes = {
  column: _propTypes2.default.object,
  row: _propTypes2.default.object,
  inline: _propTypes2.default.bool
};

exports.default = DataCell;