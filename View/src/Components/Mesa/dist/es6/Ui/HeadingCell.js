'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Templates = require('../Templates');

var _Templates2 = _interopRequireDefault(_Templates);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Tooltip = require('../Components/Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var headingCellClass = (0, _Utils.makeClassifier)('HeadingCell');

var HeadingCell = function (_React$PureComponent) {
  _inherits(HeadingCell, _React$PureComponent);

  function HeadingCell(props) {
    _classCallCheck(this, HeadingCell);

    var _this = _possibleConstructorReturn(this, (HeadingCell.__proto__ || Object.getPrototypeOf(HeadingCell)).call(this, props));

    _this.state = { offset: null };
    _this.renderContent = _this.renderContent.bind(_this);
    _this.handleSortClick = _this.handleSortClick.bind(_this);
    _this.renderSortTrigger = _this.renderSortTrigger.bind(_this);
    _this.renderHelpTrigger = _this.renderHelpTrigger.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    return _this;
  }

  _createClass(HeadingCell, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      var element = this.element;

      if (!element) return;
      var offset = _Tooltip2.default.getOffset(element);
      this.setState({ offset: offset });
    }
  }, {
    key: 'renderContent',
    value: function renderContent() {
      var _props = this.props,
          column = _props.column,
          columnIndex = _props.columnIndex;


      if ('renderHeading' in column) return column.renderHeading(column, columnIndex);

      var SortTrigger = this.renderSortTrigger;
      var HelpTrigger = this.renderHelpTrigger;
      var ClickBoundary = this.renderClickBoundary;

      return _react2.default.createElement(
        'div',
        { className: headingCellClass('Content') },
        _react2.default.createElement(
          'div',
          { className: headingCellClass(['Content', 'Aside']) },
          _react2.default.createElement(SortTrigger, null)
        ),
        _react2.default.createElement(
          'div',
          { className: headingCellClass(['Content', 'Label']) },
          _Templates2.default.heading(column, columnIndex)
        ),
        _react2.default.createElement(
          'div',
          { className: headingCellClass(['Content', 'Aside']) },
          _react2.default.createElement(
            ClickBoundary,
            null,
            _react2.default.createElement(HelpTrigger, null)
          )
        )
      );
    }
  }, {
    key: 'handleSortClick',
    value: function handleSortClick() {
      var _props2 = this.props,
          column = _props2.column,
          sort = _props2.sort,
          eventHandlers = _props2.eventHandlers;
      var onSort = eventHandlers.onSort;

      if (typeof onSort !== 'function' || !column.sortable) return;
      var currentlySorting = sort.columnKey === column.key;
      var direction = currentlySorting && sort.direction === 'asc' ? 'desc' : 'asc';
      return onSort(column, direction);
    }
  }, {
    key: 'renderClickBoundary',
    value: function renderClickBoundary(_ref) {
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
    key: 'renderSortTrigger',
    value: function renderSortTrigger() {
      var _props3 = this.props,
          column = _props3.column,
          sort = _props3.sort,
          eventHandlers = _props3.eventHandlers;

      var _ref2 = sort ? sort : {},
          columnKey = _ref2.columnKey,
          direction = _ref2.direction;

      var _ref3 = eventHandlers ? eventHandlers : {},
          onSort = _ref3.onSort;

      var _ref4 = column ? column : {},
          key = _ref4.key,
          sortable = _ref4.sortable;

      var isActive = columnKey === key;

      if (!sortable || typeof onSort !== 'function' && !isActive) return null;

      var sortIcon = !isActive ? 'sort inactive' : direction === 'asc' ? 'sort-amount-asc active' : 'sort-amount-desc active';

      return _react2.default.createElement(_Icon2.default, { fa: sortIcon + ' Trigger SortTrigger' });
    }
  }, {
    key: 'renderHelpTrigger',
    value: function renderHelpTrigger() {
      var column = this.props.column;
      var offset = this.state.offset;

      var _ref5 = offset ? offset : {},
          top = _ref5.top,
          left = _ref5.left,
          height = _ref5.height;

      var position = { top: top + height, left: left };

      if (!column.helpText) return null;
      return _react2.default.createElement(
        _Tooltip2.default,
        { position: position, className: 'Trigger HelpTrigger', text: column.helpText },
        _react2.default.createElement(_Icon2.default, { fa: 'question-circle' })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          column = _props4.column,
          state = _props4.state,
          dispatch = _props4.dispatch;
      var headingStyle = column.headingStyle,
          width = column.width,
          renderHeading = column.renderHeading;


      var widthObj = width ? { width: width, maxWidth: width, minWidth: width } : {};

      var style = Object.assign({}, headingStyle ? headingStyle : {}, widthObj);

      var Content = this.renderContent;

      return column.hidden ? null : _react2.default.createElement(
        'th',
        {
          style: style,
          key: column.key,
          className: headingCellClass(),
          onClick: this.handleSortClick
        },
        _react2.default.createElement(Content, null)
      );
    }
  }]);

  return HeadingCell;
}(_react2.default.PureComponent);

;

exports.default = HeadingCell;