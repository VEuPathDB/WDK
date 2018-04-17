'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Templates = require('../Templates');

var _Templates2 = _interopRequireDefault(_Templates);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Tooltip = require('../Components/Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Utils = require('../Utils/Utils');

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

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

    _this.state = {
      offset: null,
      isDragging: false,
      isDragTarget: false
    };

    _this.getClassName = _this.getClassName.bind(_this);
    _this.getDomEvents = _this.getDomEvents.bind(_this);

    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.renderContent = _this.renderContent.bind(_this);
    _this.renderSortTrigger = _this.renderSortTrigger.bind(_this);
    _this.renderHelpTrigger = _this.renderHelpTrigger.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);

    _this.onDrop = _this.onDrop.bind(_this);
    _this.onClick = _this.onClick.bind(_this);
    _this.onDragEnd = _this.onDragEnd.bind(_this);
    _this.onDragExit = _this.onDragExit.bind(_this);
    _this.onDragOver = _this.onDragOver.bind(_this);
    _this.onDragStart = _this.onDragStart.bind(_this);
    _this.onDragEnter = _this.onDragEnter.bind(_this);
    _this.onDragLeave = _this.onDragLeave.bind(_this);
    return _this;
  }

  _createClass(HeadingCell, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.updateOffset();
      _Events2.default.add('scroll', this.updateOffset);
      _Events2.default.add('resize', this.updateOffset);
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      if (newProps && newProps.column !== this.props.column || newProps.column.width !== this.props.column.width) {
        this.updateOffset();
      }
    }
  }, {
    key: 'updateOffset',
    value: function updateOffset() {
      var element = this.element;
      var offsetLeft = this.props.offsetLeft;

      if (!element) return;
      var offset = _Tooltip2.default.getOffset(element);
      if (offsetLeft && offset.left) offset.left += offsetLeft;
      this.setState({ offset: offset });
    }
  }, {
    key: 'onClick',
    value: function onClick() {
      var _props = this.props,
          column = _props.column,
          sort = _props.sort,
          eventHandlers = _props.eventHandlers;
      var onSort = eventHandlers.onSort;

      if (typeof onSort !== 'function' || !column.sortable) return;
      var currentlySorting = sort && sort.columnKey === column.key;
      var direction = currentlySorting && sort.direction === 'asc' ? 'desc' : 'asc';
      return onSort(column, direction);
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  }, {
    key: 'renderContent',
    value: function renderContent() {
      var _props2 = this.props,
          column = _props2.column,
          columnIndex = _props2.columnIndex;

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

      var _ref3 = column ? column : {},
          key = _ref3.key,
          sortable = _ref3.sortable;

      var _ref4 = eventHandlers ? eventHandlers : {},
          onSort = _ref4.onSort;

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
          right = _ref5.right,
          width = _ref5.width,
          height = _ref5.height,
          x = _ref5.x,
          y = _ref5.y;

      var position = { top: top + height, left: left };

      if (!column.helpText) return null;
      return _react2.default.createElement(
        _Tooltip2.default,
        { position: position, className: 'Trigger HelpTrigger', content: column.helpText },
        _react2.default.createElement(_Icon2.default, { fa: 'question-circle' })
      );
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  }, {
    key: 'onDragStart',
    value: function onDragStart(event) {
      var key = this.props.column.key;

      event.dataTransfer.effectAllowed = 'copy';
      event.dataTransfer.setData('text', key);
      this.setState({ isDragging: true });
      return event;
    }
  }, {
    key: 'onDragEnd',
    value: function onDragEnd(event) {
      if (this.state.isDragging || this.state.isDragTarget) this.setState({ isDragging: false, isDragTarget: false });
      event.preventDefault();
    }
  }, {
    key: 'onDragEnter',
    value: function onDragEnter(event) {
      var dragee = event.dataTransfer.getData('text');
      if (!this.state.isDragTarget) this.setState({ isDragTarget: true });
      event.preventDefault();
    }
  }, {
    key: 'onDragExit',
    value: function onDragExit(event) {
      var dragee = event.dataTransfer.getData('text');
      event.preventDefault();
    }
  }, {
    key: 'onDragOver',
    value: function onDragOver(event) {
      event.preventDefault();
    }
  }, {
    key: 'onDragLeave',
    value: function onDragLeave(event) {
      if (this.state.isDragTarget) this.setState({ isDragTarget: false });
      event.preventDefault();
    }
  }, {
    key: 'onDrop',
    value: function onDrop(event) {
      event.preventDefault();
      var _props4 = this.props,
          eventHandlers = _props4.eventHandlers,
          columnIndex = _props4.columnIndex;
      var onColumnReorder = eventHandlers.onColumnReorder;

      if (typeof onColumnReorder !== 'function') return;
      var draggedColumn = event.dataTransfer.getData('text');
      if (this.state.isDragTarget) this.setState({ isDragTarget: false });
      onColumnReorder(draggedColumn, columnIndex);
    }
  }, {
    key: 'getDomEvents',
    value: function getDomEvents() {
      var onClick = this.onClick,
          onDragStart = this.onDragStart,
          onDragEnd = this.onDragEnd,
          onDragEnter = this.onDragEnter,
          onDragExit = this.onDragExit,
          onDragOver = this.onDragOver,
          onDragLeave = this.onDragLeave,
          onDrop = this.onDrop;

      return {
        onClick: onClick,
        onDragStart: onDragStart, onDragEnd: onDragEnd,
        onDragEnter: onDragEnter, onDragExit: onDragExit,
        onDragOver: onDragOver, onDragLeave: onDragLeave,
        onDrop: onDrop
      };
    }
  }, {
    key: 'getClassName',
    value: function getClassName() {
      var key = this.props.column.key;
      var _state = this.state,
          isDragging = _state.isDragging,
          isDragTarget = _state.isDragTarget;

      var modifiers = ['key-' + key];
      if (isDragging) modifiers.push('Dragging');
      if (isDragTarget) modifiers.push('DragTarget');
      var className = headingCellClass(null, modifiers);
      return className;
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var _props5 = this.props,
          column = _props5.column,
          eventHandlers = _props5.eventHandlers;
      var key = column.key,
          headingStyle = column.headingStyle,
          width = column.width,
          renderHeading = column.renderHeading;

      var widthStyle = width ? { width: width, maxWidth: width, minWidth: width } : {};

      var style = Object.assign({}, headingStyle ? headingStyle : {}, widthStyle);
      var ref = function ref(element) {
        return _this2.element = element;
      };

      var children = this.renderContent();
      var className = this.getClassName();
      var domEvents = this.getDomEvents();

      var draggable = column.moveable && !column.primary && typeof eventHandlers.onColumnReorder === 'function';

      var props = { style: style, key: key, ref: ref, draggable: draggable, children: children, className: className };

      return column.hidden ? null : _react2.default.createElement('th', _extends({}, props, domEvents));
    }
  }]);

  return HeadingCell;
}(_react2.default.PureComponent);

;

HeadingCell.propTypes = {
  sort: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.object,
  column: _propTypes2.default.object.isRequired,
  columnIndex: _propTypes2.default.number.isRequired
};

exports.default = HeadingCell;