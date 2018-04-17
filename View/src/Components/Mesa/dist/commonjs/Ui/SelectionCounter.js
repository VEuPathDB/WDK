'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _PaginationUtils = require('../Utils/PaginationUtils');

var _PaginationUtils2 = _interopRequireDefault(_PaginationUtils);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCounter = function (_React$Component) {
  _inherits(SelectionCounter, _React$Component);

  function SelectionCounter(props) {
    _classCallCheck(this, SelectionCounter);

    var _this = _possibleConstructorReturn(this, (SelectionCounter.__proto__ || Object.getPrototypeOf(SelectionCounter)).call(this, props));

    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    _this.goToSelection = _this.goToSelection.bind(_this);
    return _this;
  }

  _createClass(SelectionCounter, [{
    key: 'noun',
    value: function noun(size) {
      size = typeof size === 'number' ? size : size.length;
      return 'row' + (size === 1 ? '' : 's');
    }

    /* Actions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

  }, {
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props = this.props,
          dispatch = _props.dispatch,
          filteredRows = _props.filteredRows;

      var ids = filteredRows.map(function (row) {
        return row.__id;
      });
      dispatch((0, _Actions.selectRowsByIds)(ids));
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props2 = this.props,
          dispatch = _props2.dispatch,
          filteredRows = _props2.filteredRows;

      var ids = filteredRows.map(function (row) {
        return row.__id;
      });
      dispatch((0, _Actions.deselectRowsByIds)(ids));
    }
  }, {
    key: 'goToSelection',
    value: function goToSelection() {
      var _props3 = this.props,
          state = _props3.state,
          dispatch = _props3.dispatch,
          filteredRows = _props3.filteredRows;
      var _state$ui = state.ui,
          selection = _state$ui.selection,
          pagination = _state$ui.pagination;
      var paginate = state.options.paginate;

      var spread = _PaginationUtils2.default.getSpread(filteredRows, pagination, paginate);

      var target = selection.find(function (id) {
        return !spread.includes(id);
      });
      if (!target) return;

      var targetIndex = filteredRows.findIndex(function (row) {
        return row.__id === target;
      });
      if (targetIndex < 0) return;

      dispatch((0, _Actions.setPaginatedActiveItem)(targetIndex + 1));
    }

    /* Renderers -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

  }, {
    key: 'renderSelectionCount',
    value: function renderSelectionCount() {
      var _props4 = this.props,
          state = _props4.state,
          dispatch = _props4.dispatch,
          filteredRows = _props4.filteredRows;
      var selection = state.ui.selection;


      var allIds = filteredRows.map(function (row) {
        return row.__id;
      });
      var allSelected = _PaginationUtils2.default.isSpreadSelected(allIds, selection);

      return _react2.default.createElement(
        'div',
        { className: 'SelectionCounter' },
        allSelected ? 'All ' : '',
        _react2.default.createElement(
          'b',
          null,
          selection.length
        ),
        ' ',
        this.noun(selection),
        ' ',
        allSelected ? 'are' : '',
        ' selected.',
        _react2.default.createElement('br', null),
        _react2.default.createElement(
          'a',
          { onClick: this.deselectAllRows },
          'Clear selection.'
        )
      );
    }
  }, {
    key: 'renderPaginatedSelectionCount',
    value: function renderPaginatedSelectionCount() {
      var _props5 = this.props,
          state = _props5.state,
          dispatch = _props5.dispatch,
          filteredRows = _props5.filteredRows;
      var _state$ui2 = state.ui,
          selection = _state$ui2.selection,
          pagination = _state$ui2.pagination;

      var allIds = filteredRows.map(function (row) {
        return row.__id;
      });
      var allSelected = _PaginationUtils2.default.isSpreadSelected(allIds, selection);
      var spread = _PaginationUtils2.default.getSpread(filteredRows, pagination, true);
      var pageCoverage = _PaginationUtils2.default.countSelectedInSpread(spread, selection);
      var totalCoverage = _PaginationUtils2.default.countSelectedInSpread(allIds, selection);

      var outsideCoverage = selection.length - pageCoverage;
      var pageSelected = pageCoverage === spread.length;

      return _react2.default.createElement(
        'div',
        { className: 'SelectionCounter' },
        pageSelected && !allSelected ? _react2.default.createElement(
          'span',
          null,
          'All ',
          _react2.default.createElement(
            'b',
            null,
            pageCoverage
          ),
          ' ',
          this.noun(pageCoverage),
          ' on this page are selected. '
        ) : null,
        pageCoverage && !pageSelected && !allSelected ? _react2.default.createElement(
          'span',
          null,
          _react2.default.createElement(
            'b',
            null,
            pageCoverage
          ),
          ' ',
          this.noun(pageCoverage),
          ' selected on this page. '
        ) : null,
        outsideCoverage && !allSelected ? _react2.default.createElement(
          'span',
          null,
          _react2.default.createElement(
            'b',
            null,
            outsideCoverage
          ),
          ' ',
          this.noun(outsideCoverage),
          ' selected on ',
          _react2.default.createElement(
            'a',
            { onClick: this.goToSelection },
            'other pages'
          ),
          '. '
        ) : null,
        allSelected ? _react2.default.createElement(
          'span',
          null,
          'All ',
          _react2.default.createElement(
            'b',
            null,
            totalCoverage
          ),
          ' ',
          this.noun(totalCoverage),
          ' are selected. '
        ) : null,
        _react2.default.createElement('br', null),
        pageSelected && !allSelected ? _react2.default.createElement(
          'a',
          { onClick: this.selectAllRows },
          'Select all ',
          _react2.default.createElement(
            'b',
            null,
            filteredRows.length
          ),
          ' ',
          this.noun(filteredRows.length),
          '. '
        ) : null,
        _react2.default.createElement(
          'a',
          { onClick: this.deselectAllRows },
          'Clear selection.'
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var paginate = this.props.state.options.paginate;
      var selection = this.props.state.ui.selection;


      if (!selection.length) return null;
      return !paginate ? this.renderSelectionCount() : this.renderPaginatedSelectionCount();
    }
  }]);

  return SelectionCounter;
}(_react2.default.Component);

;

exports.default = SelectionCounter;