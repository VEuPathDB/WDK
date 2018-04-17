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
var hasWidthProperty = function hasWidthProperty(_ref) {
  var width = _ref.width;
  return typeof width === 'string';
};

var DataTable = function (_React$Component) {
  _inherits(DataTable, _React$Component);

  function DataTable(props) {
    _classCallCheck(this, DataTable);

    var _this = _possibleConstructorReturn(this, (DataTable.__proto__ || Object.getPrototypeOf(DataTable)).call(this, props));

    _this.widthCache = {};
    _this.state = { dynamicWidths: null };
    _this.renderPlainTable = _this.renderPlainTable.bind(_this);
    _this.renderStickyTable = _this.renderStickyTable.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.getInnerCellWidth = _this.getInnerCellWidth.bind(_this);
    _this.shouldUseStickyHeader = _this.shouldUseStickyHeader.bind(_this);
    _this.handleTableBodyScroll = _this.handleTableBodyScroll.bind(_this);
    _this.componentWillReceiveProps = _this.componentWillReceiveProps.bind(_this);
    return _this;
  }

  _createClass(DataTable, [{
    key: 'shouldUseStickyHeader',
    value: function shouldUseStickyHeader() {
      var _props = this.props,
          columns = _props.columns,
          options = _props.options;

      if (!options || !options.useStickyHeader) return false;
      if (!options.tableBodyMaxHeight) return console.error('\n      "useStickyHeader" option enabled but no maxHeight for the table is set.\n      Use a css height as the "tableBodyMaxHeight" option to use this setting.\n    ');
      return true;
    }
  }, {
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.setDynamicWidths();
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var _this2 = this;

      if (newProps && newProps.columns && newProps.columns !== this.props.columns) this.setState({ dynamicWidths: null }, function () {
        return _this2.setDynamicWidths();
      });
    }
  }, {
    key: 'setDynamicWidths',
    value: function setDynamicWidths() {
      var columns = this.props.columns;
      var headingTable = this.headingTable,
          contentTable = this.contentTable,
          getInnerCellWidth = this.getInnerCellWidth;

      if (!headingTable || !contentTable) return;
      var headingCells = Array.from(headingTable.getElementsByTagName('th'));
      var contentCells = Array.from(contentTable.getElementsByTagName('td')).slice(0, headingCells.length);

      if (this.hasSelectionColumn()) headingCells.shift() && contentCells.shift();
      var dynamicWidths = contentCells.map(function (c, i) {
        return getInnerCellWidth(c, headingCells[i], columns[i]);
      });
      this.setState({ dynamicWidths: dynamicWidths });
    }
  }, {
    key: 'getInnerCellWidth',
    value: function getInnerCellWidth(cell, headingCell, _ref2) {
      var key = _ref2.key;

      if (key in this.widthCache) return this.widthCache[key];

      var contentWidth = cell.clientWidth;
      var headingWidth = headingCell.clientWidth;
      var grabStyle = function grabStyle(prop) {
        return parseInt(window.getComputedStyle(cell, null).getPropertyValue(prop));
      };

      var leftPadding = grabStyle('padding-left');
      var rightPadding = grabStyle('padding-right');
      var leftBorder = grabStyle('border-left-width');
      var rightBorder = grabStyle('border-right-width');
      var widthOffset = leftPadding + rightPadding + leftBorder + rightBorder;

      var higher = Math.max(contentWidth, headingWidth);
      return this.widthCache[key] = higher;

      var lower = Math.min(contentWidth, headingWidth);
      var split = Math.abs(contentWidth - headingWidth) / 2;
      var width = Math.ceil(lower + split) - widthOffset + 'px';

      return this.widthCache[key] = width;
    }
  }, {
    key: 'hasSelectionColumn',
    value: function hasSelectionColumn() {
      var _props2 = this.props,
          options = _props2.options,
          eventHandlers = _props2.eventHandlers;

      return typeof options.isRowSelected === 'function' && typeof eventHandlers.onRowSelect === 'function' && typeof eventHandlers.onRowDeselect === 'function';
    }
  }, {
    key: 'handleTableBodyScroll',
    value: function handleTableBodyScroll(e) {
      var offset = this.bodyNode.scrollLeft;
      this.headerNode.scrollLeft = offset;
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  }, {
    key: 'renderStickyTable',
    value: function renderStickyTable() {
      var _this3 = this;

      var _props3 = this.props,
          options = _props3.options,
          columns = _props3.columns,
          rows = _props3.rows,
          filteredRows = _props3.filteredRows,
          actions = _props3.actions,
          eventHandlers = _props3.eventHandlers,
          uiState = _props3.uiState;
      var dynamicWidths = this.state.dynamicWidths;

      var newColumns = columns.every(function (_ref3) {
        var width = _ref3.width;
        return width;
      }) || !dynamicWidths || dynamicWidths.length == 0 ? columns : columns.map(function (column, index) {
        return Object.assign({}, column, { width: dynamicWidths[index] });
      });
      var maxHeight = { maxHeight: options ? options.tableBodyMaxHeight : null };
      var maxWidth = { minWidth: dynamicWidths ? (0, _Utils.combineWidths)(columns.map(function (_ref4) {
          var width = _ref4.width;
          return width;
        })) : null };
      var tableLayout = { tableLayout: dynamicWidths ? 'fixed' : 'auto' };
      var tableProps = { options: options, rows: rows, filteredRows: filteredRows, actions: actions, eventHandlers: eventHandlers, uiState: uiState, columns: newColumns };
      return _react2.default.createElement(
        'div',
        { className: 'MesaComponent' },
        _react2.default.createElement(
          'div',
          { className: dataTableClass(), style: maxWidth },
          _react2.default.createElement(
            'div',
            { className: dataTableClass('Sticky'), style: maxWidth },
            _react2.default.createElement(
              'div',
              {
                ref: function ref(node) {
                  return _this3.headerNode = node;
                },
                className: dataTableClass('Header') },
              _react2.default.createElement(
                'table',
                {
                  cellSpacing: 0,
                  cellPadding: 0,
                  style: tableLayout,
                  ref: function ref(node) {
                    return _this3.headingTable = node;
                  } },
                _react2.default.createElement(
                  'thead',
                  null,
                  _react2.default.createElement(_HeadingRow2.default, tableProps)
                )
              )
            ),
            _react2.default.createElement(
              'div',
              {
                style: maxHeight,
                ref: function ref(node) {
                  return _this3.bodyNode = node;
                },
                className: dataTableClass('Body'),
                onScroll: this.handleTableBodyScroll },
              _react2.default.createElement(
                'table',
                {
                  cellSpacing: 0,
                  cellPadding: 0,
                  style: tableLayout,
                  ref: function ref(node) {
                    return _this3.contentTable = node;
                  } },
                _react2.default.createElement(_DataRowList2.default, tableProps)
              )
            )
          )
        )
      );
    }
  }, {
    key: 'renderPlainTable',
    value: function renderPlainTable() {
      var props = this.props;

      return _react2.default.createElement(
        'div',
        { className: 'MesaComponent' },
        _react2.default.createElement(
          'div',
          { className: dataTableClass() },
          _react2.default.createElement(
            'table',
            { cellSpacing: '0', cellPadding: '0' },
            _react2.default.createElement(
              'thead',
              null,
              _react2.default.createElement(_HeadingRow2.default, props)
            ),
            _react2.default.createElement(_DataRowList2.default, props)
          )
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var shouldUseStickyHeader = this.shouldUseStickyHeader,
          renderStickyTable = this.renderStickyTable,
          renderPlainTable = this.renderPlainTable;

      return shouldUseStickyHeader() ? renderStickyTable() : renderPlainTable();
    }
  }]);

  return DataTable;
}(_react2.default.Component);

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