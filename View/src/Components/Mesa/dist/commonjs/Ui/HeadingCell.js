'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Templates = require('../Templates');

var _Templates2 = _interopRequireDefault(_Templates);

var _ColumnSorter = require('../Ui/ColumnSorter');

var _ColumnSorter2 = _interopRequireDefault(_ColumnSorter);

var _ColumnFilter = require('../Ui/ColumnFilter');

var _ColumnFilter2 = _interopRequireDefault(_ColumnFilter);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HeadingCell = function (_React$PureComponent) {
  _inherits(HeadingCell, _React$PureComponent);

  function HeadingCell(props) {
    _classCallCheck(this, HeadingCell);

    return _possibleConstructorReturn(this, (HeadingCell.__proto__ || Object.getPrototypeOf(HeadingCell)).call(this, props));
  }

  _createClass(HeadingCell, [{
    key: 'renderContent',
    value: function renderContent() {
      var column = this.props.column;

      if ('renderHeading' in column) return column.renderHeading(column);
      return _Templates2.default.heading(column);
    }
  }, {
    key: 'handleSortClick',
    value: function handleSortClick() {
      var _props = this.props,
          column = _props.column,
          state = _props.state,
          dispatch = _props.dispatch;
      var sort = state.ui.sort;

      var currentlySorting = sort.byColumn === column;
      dispatch(currentlySorting ? (0, _Actions.toggleSortOrder)() : (0, _Actions.sortByColumn)(column));
    }
  }, {
    key: 'defuseSortClick',
    value: function defuseSortClick(_ref) {
      var children = _ref.children;

      var style = { display: 'inline-block' };
      return _react2.default.createElement(
        'div',
        { onClick: function onClick(e) {
            return e.stopPropagation();
          }, style: style },
        children
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var _props2 = this.props,
          column = _props2.column,
          state = _props2.state,
          dispatch = _props2.dispatch;

      var content = this.renderContent();
      var DefuseSortClick = this.defuseSortClick;
      var headingStyle = column.headingStyle;


      return column.hidden ? null : _react2.default.createElement(
        'th',
        {
          key: column.key,
          ref: function ref(el) {
            return _this2.element = el;
          },
          style: headingStyle,
          onClick: function onClick(e) {
            return column.sortable ? _this2.handleSortClick() : null;
          }
        },
        column.sortable && _react2.default.createElement(_ColumnSorter2.default, {
          column: column,
          state: state
        }),
        content,
        column.filterable && _react2.default.createElement(
          DefuseSortClick,
          null,
          _react2.default.createElement(_ColumnFilter2.default, {
            column: column,
            state: state,
            dispatch: dispatch
          })
        )
      );
    }
  }]);

  return HeadingCell;
}(_react2.default.PureComponent);

;

exports.default = HeadingCell;