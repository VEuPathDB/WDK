'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _PaginationUtils = require('../Utils/PaginationUtils');

var _PaginationUtils2 = _interopRequireDefault(_PaginationUtils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var RowCounter = function (_React$PureComponent) {
  _inherits(RowCounter, _React$PureComponent);

  function RowCounter(props) {
    _classCallCheck(this, RowCounter);

    var _this = _possibleConstructorReturn(this, (RowCounter.__proto__ || Object.getPrototypeOf(RowCounter)).call(this, props));

    _this.getPageString = _this.getPageString.bind(_this);
    _this.getFilteredString = _this.getFilteredString.bind(_this);
    _this.getStatistics = _this.getStatistics.bind(_this);
    return _this;
  }

  _createClass(RowCounter, [{
    key: 'getFilteredString',
    value: function getFilteredString() {
      var _getStatistics = this.getStatistics(),
          filtered = _getStatistics.filtered;

      var searchQuery = this.props.state.ui.searchQuery;

      return filtered && !searchQuery ? _react2.default.createElement(
        'span',
        { className: 'faded' },
        ' (',
        _react2.default.createElement(
          'b',
          null,
          filtered
        ),
        ' filtered)'
      ) : null;
    }
  }, {
    key: 'getStatistics',
    value: function getStatistics() {
      var _props = this.props,
          state = _props.state,
          filteredRows = _props.filteredRows;

      var total = state.rows.length;
      var effective = filteredRows.length;
      var filtered = total - effective;
      return { total: total, effective: effective, filtered: filtered };
    }
  }, {
    key: 'getPageString',
    value: function getPageString() {
      var _props2 = this.props,
          filteredRows = _props2.filteredRows,
          state = _props2.state;
      var paginate = state.options.paginate;
      var _state$ui = state.ui,
          pagination = _state$ui.pagination,
          searchQuery = _state$ui.searchQuery;

      var _getStatistics2 = this.getStatistics(),
          total = _getStatistics2.total,
          effective = _getStatistics2.effective,
          filtered = _getStatistics2.filtered;

      var noun = searchQuery ? 'Result' : 'Row';
      var plural = noun + (effective !== 1 ? 's' : '');

      var simple = _react2.default.createElement(
        'span',
        null,
        _react2.default.createElement(
          'b',
          null,
          effective
        ),
        '  ',
        plural
      );

      if (!paginate) return simple;

      var currentPage = _PaginationUtils2.default.getCurrentPageNumber(pagination);
      var firstOnPage = _PaginationUtils2.default.firstItemOnPage(currentPage, pagination);
      var lastOnPage = _PaginationUtils2.default.lastItemOnPage(currentPage, pagination, filteredRows);

      if (effective === lastOnPage && firstOnPage === 1) return simple;

      return _react2.default.createElement(
        'span',
        null,
        effective !== firstOnPage ? plural : noun,
        ' ',
        _react2.default.createElement(
          'b',
          null,
          firstOnPage
        ),
        firstOnPage !== lastOnPage ? _react2.default.createElement(
          'span',
          null,
          ' - ',
          _react2.default.createElement(
            'b',
            null,
            lastOnPage
          )
        ) : null,
        effective !== firstOnPage ? _react2.default.createElement(
          'span',
          null,
          ' of ',
          _react2.default.createElement(
            'b',
            null,
            effective
          )
        ) : null
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var filteredRows = this.props.filteredRows;

      var filteredString = this.getFilteredString();
      var pageString = this.getPageString();

      return !filteredRows.length ? null : _react2.default.createElement(
        'div',
        { className: 'RowCounter' },
        pageString,
        filteredString
      );
    }
  }]);

  return RowCounter;
}(_react2.default.PureComponent);

;

exports.default = RowCounter;