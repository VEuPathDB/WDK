'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _HeadingRow = require('../Ui/HeadingRow');

var _HeadingRow2 = _interopRequireDefault(_HeadingRow);

var _DataRowList = require('../Ui/DataRowList');

var _DataRowList2 = _interopRequireDefault(_DataRowList);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataTableClass = (0, _Utils.makeClassifier)('DataTable');

var DataTable = function (_React$PureComponent) {
  _inherits(DataTable, _React$PureComponent);

  function DataTable(props) {
    _classCallCheck(this, DataTable);

    var _this = _possibleConstructorReturn(this, (DataTable.__proto__ || Object.getPrototypeOf(DataTable)).call(this, props));

    _this.generateLayout = _this.generateLayout.bind(_this);
    _this.shouldUseStickyHeader = _this.shouldUseStickyHeader.bind(_this);
    return _this;
  }

  _createClass(DataTable, [{
    key: 'shouldUseStickyHeader',
    value: function shouldUseStickyHeader() {
      var _props = this.props,
          columns = _props.columns,
          options = _props.options;

      if (!options || !options.useStickyHeader) return false;
      var hasWidthProperty = function hasWidthProperty(_ref) {
        var width = _ref.width;
        return typeof width === 'string';
      };
      if (columns.every(hasWidthProperty)) return true;
      console.error('\n      "useStickyHeader" enabled but not all columns have explicit widths (required).\n      Use a CSS width (e.g. "250px" or "30%") as each column\'s .width property.\n    ');
      return false;
    }
  }, {
    key: 'generateLayout',
    value: function generateLayout() {
      var _props2 = this.props,
          rows = _props2.rows,
          filteredRows = _props2.filteredRows,
          options = _props2.options,
          columns = _props2.columns,
          actions = _props2.actions,
          uiState = _props2.uiState,
          eventHandlers = _props2.eventHandlers;

      var props = { rows: rows, filteredRows: filteredRows, options: options, columns: columns, actions: actions, uiState: uiState, eventHandlers: eventHandlers };

      var _ref2 = options ? options : {},
          tableBodyMaxHeight = _ref2.tableBodyMaxHeight;

      var tableBodyStyle = { maxHeight: tableBodyMaxHeight };
      var useStickyLayout = this.shouldUseStickyHeader();
      var cumulativeWidth = (0, _Utils.combineWidths)(columns.map(function (col) {
        return col.width;
      }));
      var widthLayer = !useStickyLayout ? null : {
        minWidth: cumulativeWidth,
        maxWidth: cumulativeWidth,
        width: cumulativeWidth
      };

      return useStickyLayout ? _react2.default.createElement(
        'div',
        { className: dataTableClass('Sticky') },
        _react2.default.createElement(
          'div',
          { className: dataTableClass('Header'), style: widthLayer },
          _react2.default.createElement(
            'table',
            { cellSpacing: 0, cellPadding: 0 },
            _react2.default.createElement(
              'thead',
              null,
              _react2.default.createElement(_HeadingRow2.default, props)
            )
          )
        ),
        _react2.default.createElement(
          'div',
          { className: dataTableClass('Body'), style: Object.assign(tableBodyStyle, widthLayer) },
          _react2.default.createElement(
            'table',
            { cellSpacing: 0, cellPadding: 0 },
            _react2.default.createElement(_DataRowList2.default, props)
          )
        )
      ) : _react2.default.createElement(
        'table',
        { cellSpacing: '0', cellPadding: '0' },
        _react2.default.createElement(
          'thead',
          null,
          _react2.default.createElement(_HeadingRow2.default, props)
        ),
        _react2.default.createElement(_DataRowList2.default, props)
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var Layout = this.generateLayout;

      return _react2.default.createElement(
        'div',
        { className: 'MesaComponent' },
        _react2.default.createElement(
          'div',
          { className: dataTableClass() },
          _react2.default.createElement(Layout, null)
        )
      );
    }
  }]);

  return DataTable;
}(_react2.default.PureComponent);

;

DataTable.propTypes = {
  rows: _propTypes2.default.array,
  columns: _propTypes2.default.array,
  options: _propTypes2.default.object,
  actions: _propTypes2.default.arrayOf(_propTypes2.default.shape({
    element: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.node, _propTypes2.default.element]),
    handler: _propTypes2.default.func,
    callback: _propTypes2.default.func
  })),
  uiState: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.objectOf(_propTypes2.default.func)
};

exports.default = DataTable;