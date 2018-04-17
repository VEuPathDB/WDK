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
      isDragTarget: false,
      clickStart: null
    };

    _this.getClassName = _this.getClassName.bind(_this);
    _this.getDomEvents = _this.getDomEvents.bind(_this);
    _this.sortColumn = _this.sortColumn.bind(_this);
    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.renderContent = _this.renderContent.bind(_this);
    _this.renderSortTrigger = _this.renderSortTrigger.bind(_this);
    _this.renderHelpTrigger = _this.renderHelpTrigger.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);

    _this.onDrop = _this.onDrop.bind(_this);
    _this.onDragEnd = _this.onDragEnd.bind(_this);
    _this.onDragExit = _this.onDragExit.bind(_this);
    _this.onDragOver = _this.onDragOver.bind(_this);
    _this.onDragStart = _this.onDragStart.bind(_this);
    _this.onDragEnter = _this.onDragEnter.bind(_this);
    _this.onDragLeave = _this.onDragLeave.bind(_this);
    _this.onMouseDown = _this.onMouseDown.bind(_this);
    _this.onMouseUp = _this.onMouseUp.bind(_this);
    return _this;
  }

  _createClass(HeadingCell, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      Object.values(this.listeners).forEach(function (listenerId) {
        return _Events2.default.remove(listenerId);
      });
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

      if (!element) return;
      var offset = _Tooltip2.default.getOffset(element);
      this.setState({ offset: offset });
    }
  }, {
    key: 'sortColumn',
    value: function sortColumn() {
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
  }, {
    key: 'onMouseDown',
    value: function onMouseDown(e) {
      var clickStart = new Date().getTime();
      this.setState({ clickStart: clickStart });
    }
  }, {
    key: 'onMouseUp',
    value: function onMouseUp(e) {
      var clickStart = this.state.clickStart;

      if (!clickStart) return;
      var clickEnd = new Date().getTime();
      var totalTime = clickEnd - clickStart;
      this.setState({ clickStart: null, isDragTarget: false });
      if (totalTime <= 500) this.sortColumn();
      if (this.element) this.element.blur();
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  }, {
    key: 'wrapContent',
    value: function wrapContent() {
      var content = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;

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
          content
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
    key: 'renderContent',
    value: function renderContent() {
      var _props2 = this.props,
          column = _props2.column,
          columnIndex = _props2.columnIndex,
          headingRowIndex = _props2.headingRowIndex;

      var SortTrigger = this.renderSortTrigger;
      var HelpTrigger = this.renderHelpTrigger;
      var ClickBoundary = this.renderClickBoundary;

      if ('renderHeading' in column && column.renderHeading === false) return null;
      if (!'renderHeading' in column || typeof column.renderHeading !== 'function') return this.wrapContent(_Templates2.default.heading(column, columnIndex));

      var content = column.renderHeading(column, columnIndex, { SortTrigger: SortTrigger, HelpTrigger: HelpTrigger, ClickBoundary: ClickBoundary });
      var wrapCustomHeadings = column.wrapCustomHeadings;

      var shouldWrap = wrapCustomHeadings && typeof wrapCustomHeadings === 'function' ? wrapCustomHeadings({ column: column, columnIndex: columnIndex, headingRowIndex: headingRowIndex }) : wrapCustomHeadings;

      return shouldWrap ? this.wrapContent(content) : content;
    }
  }, {
    key: 'renderClickBoundary',
    value: function renderClickBoundary(_ref) {
      var children = _ref.children;

      var style = { display: 'inline-block' };
      var stopPropagation = function stopPropagation(node) {
        if (!node) return null;
        var instance = new _Events.EventsFactory(node);
        instance.add('click', function (e) {
          e.stopPropagation();
        });
      };
      return _react2.default.createElement('div', { ref: stopPropagation, style: style, children: children });
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

      var sortIcon = !isActive ? 'sort inactive' : 'sort-amount-' + direction + ' active';

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
      this.setState({ isDragging: false, isDragTarget: false });
      this.element.blur();
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
      this.setState({ isDragTarget: false });
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
      this.setState({ isDragTarget: false });
      this.element.blur();
      event.preventDefault();
    }
  }, {
    key: 'onDrop',
    value: function onDrop(event) {
      this.element.blur();
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
      var onMouseDown = this.onMouseDown,
          onMouseUp = this.onMouseUp,
          onDragStart = this.onDragStart,
          onDragEnd = this.onDragEnd,
          onDragEnter = this.onDragEnter,
          onDragExit = this.onDragExit,
          onDragOver = this.onDragOver,
          onDragLeave = this.onDragLeave,
          onDrop = this.onDrop;

      return {
        onMouseDown: onMouseDown, onMouseUp: onMouseUp,
        onDragStart: onDragStart, onDragEnd: onDragEnd,
        onDragEnter: onDragEnter, onDragExit: onDragExit,
        onDragOver: onDragOver, onDragLeave: onDragLeave,
        onDrop: onDrop
      };
    }
  }, {
    key: 'getClassName',
    value: function getClassName() {
      var _props$column = this.props.column,
          key = _props$column.key,
          headingClassName = _props$column.headingClassName;
      var _state = this.state,
          isDragging = _state.isDragging,
          isDragTarget = _state.isDragTarget;

      var modifiers = ['key-' + key];
      if (isDragging) modifiers.push('Dragging');
      if (isDragTarget) modifiers.push('DragTarget');
      return (typeof headingClassName === 'string' ? headingClassName + ' ' : '') + headingCellClass(null, modifiers);
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var _props5 = this.props,
          column = _props5.column,
          eventHandlers = _props5.eventHandlers,
          primary = _props5.primary;
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

      var draggable = primary && column.moveable && !column.primary && typeof eventHandlers.onColumnReorder === 'function';

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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9IZWFkaW5nQ2VsbC5qc3giXSwibmFtZXMiOlsiaGVhZGluZ0NlbGxDbGFzcyIsIkhlYWRpbmdDZWxsIiwicHJvcHMiLCJzdGF0ZSIsIm9mZnNldCIsImlzRHJhZ2dpbmciLCJpc0RyYWdUYXJnZXQiLCJjbGlja1N0YXJ0IiwiZ2V0Q2xhc3NOYW1lIiwiYmluZCIsImdldERvbUV2ZW50cyIsInNvcnRDb2x1bW4iLCJ1cGRhdGVPZmZzZXQiLCJyZW5kZXJDb250ZW50IiwicmVuZGVyU29ydFRyaWdnZXIiLCJyZW5kZXJIZWxwVHJpZ2dlciIsImNvbXBvbmVudERpZE1vdW50Iiwib25Ecm9wIiwib25EcmFnRW5kIiwib25EcmFnRXhpdCIsIm9uRHJhZ092ZXIiLCJvbkRyYWdTdGFydCIsIm9uRHJhZ0VudGVyIiwib25EcmFnTGVhdmUiLCJvbk1vdXNlRG93biIsIm9uTW91c2VVcCIsImxpc3RlbmVycyIsInNjcm9sbCIsImFkZCIsInJlc2l6ZSIsIk1lc2FTY3JvbGwiLCJNZXNhUmVmbG93IiwiT2JqZWN0IiwidmFsdWVzIiwiZm9yRWFjaCIsInJlbW92ZSIsImxpc3RlbmVySWQiLCJuZXdQcm9wcyIsImNvbHVtbiIsIndpZHRoIiwiZWxlbWVudCIsImdldE9mZnNldCIsInNldFN0YXRlIiwic29ydCIsImV2ZW50SGFuZGxlcnMiLCJvblNvcnQiLCJzb3J0YWJsZSIsImN1cnJlbnRseVNvcnRpbmciLCJjb2x1bW5LZXkiLCJrZXkiLCJkaXJlY3Rpb24iLCJlIiwiRGF0ZSIsImdldFRpbWUiLCJjbGlja0VuZCIsInRvdGFsVGltZSIsImJsdXIiLCJjb250ZW50IiwiU29ydFRyaWdnZXIiLCJIZWxwVHJpZ2dlciIsIkNsaWNrQm91bmRhcnkiLCJyZW5kZXJDbGlja0JvdW5kYXJ5IiwiY29sdW1uSW5kZXgiLCJoZWFkaW5nUm93SW5kZXgiLCJyZW5kZXJIZWFkaW5nIiwid3JhcENvbnRlbnQiLCJoZWFkaW5nIiwid3JhcEN1c3RvbUhlYWRpbmdzIiwic2hvdWxkV3JhcCIsImNoaWxkcmVuIiwic3R5bGUiLCJkaXNwbGF5Iiwic3RvcFByb3BhZ2F0aW9uIiwibm9kZSIsImluc3RhbmNlIiwiaXNBY3RpdmUiLCJzb3J0SWNvbiIsInRvcCIsImxlZnQiLCJoZWlnaHQiLCJwb3NpdGlvbiIsImhlbHBUZXh0IiwiZXZlbnQiLCJkYXRhVHJhbnNmZXIiLCJlZmZlY3RBbGxvd2VkIiwic2V0RGF0YSIsInByZXZlbnREZWZhdWx0IiwiZHJhZ2VlIiwiZ2V0RGF0YSIsIm9uQ29sdW1uUmVvcmRlciIsImRyYWdnZWRDb2x1bW4iLCJoZWFkaW5nQ2xhc3NOYW1lIiwibW9kaWZpZXJzIiwicHVzaCIsInByaW1hcnkiLCJoZWFkaW5nU3R5bGUiLCJ3aWR0aFN0eWxlIiwibWF4V2lkdGgiLCJtaW5XaWR0aCIsImFzc2lnbiIsInJlZiIsImNsYXNzTmFtZSIsImRvbUV2ZW50cyIsImRyYWdnYWJsZSIsIm1vdmVhYmxlIiwiaGlkZGVuIiwiUHVyZUNvbXBvbmVudCIsInByb3BUeXBlcyIsIm9iamVjdCIsImlzUmVxdWlyZWQiLCJudW1iZXIiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7O0FBQ0E7Ozs7Ozs7Ozs7OztBQUVBLElBQU1BLG1CQUFtQiwyQkFBZSxhQUFmLENBQXpCOztJQUVNQyxXOzs7QUFDSix1QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDBIQUNaQSxLQURZOztBQUVsQixVQUFLQyxLQUFMLEdBQWE7QUFDWEMsY0FBUSxJQURHO0FBRVhDLGtCQUFZLEtBRkQ7QUFHWEMsb0JBQWMsS0FISDtBQUlYQyxrQkFBWTtBQUpELEtBQWI7O0FBT0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUNBLFVBQUtDLFlBQUwsR0FBb0IsTUFBS0EsWUFBTCxDQUFrQkQsSUFBbEIsT0FBcEI7QUFDQSxVQUFLRSxVQUFMLEdBQWtCLE1B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LGE7O0FBd1EvQjs7QUFFRHpHLFlBQVkwRyxTQUFaLEdBQXdCO0FBQ3RCaEUsUUFBTSxvQkFBVWlFLE1BRE07QUFFdEJoRSxpQkFBZSxvQkFBVWdFLE1BRkg7QUFHdEJ0RSxVQUFRLG9CQUFVc0UsTUFBVixDQUFpQkMsVUFISDtBQUl0Qi9DLGVBQWEsb0JBQVVnRCxNQUFWLENBQWlCRDtBQUpSLENBQXhCOztrQkFPZTVHLFciLCJmaWxlIjoiSGVhZGluZ0NlbGwuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IFRlbXBsYXRlcyBmcm9tICcuLi9UZW1wbGF0ZXMnO1xuaW1wb3J0IEljb24gZnJvbSAnLi4vQ29tcG9uZW50cy9JY29uJztcbmltcG9ydCBUb29sdGlwIGZyb20gJy4uL0NvbXBvbmVudHMvVG9vbHRpcCc7XG5pbXBvcnQgeyBtYWtlQ2xhc3NpZmllciB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcbmltcG9ydCBFdmVudHMsIHsgRXZlbnRzRmFjdG9yeSB9IGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNvbnN0IGhlYWRpbmdDZWxsQ2xhc3MgPSBtYWtlQ2xhc3NpZmllcignSGVhZGluZ0NlbGwnKTtcblxuY2xhc3MgSGVhZGluZ0NlbGwgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBvZmZzZXQ6IG51bGwsXG4gICAgICBpc0RyYWdnaW5nOiBmYWxzZSxcbiAgICAgIGlzRHJhZ1RhcmdldDogZmFsc2UsXG4gICAgICBjbGlja1N0YXJ0OiBudWxsXG4gICAgfTtcblxuICAgIHRoaXMuZ2V0Q2xhc3NOYW1lID0gdGhpcy5nZXRDbGFzc05hbWUuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldERvbUV2ZW50cyA9IHRoaXMuZ2V0RG9tRXZlbnRzLmJpbmQodGhpcyk7XG4gICAgdGhpcy5zb3J0Q29sdW1uID0gdGhpcy5zb3J0Q29sdW1uLmJpbmQodGhpcyk7XG4gICAgdGhpcy51cGRhdGVPZmZzZXQgPSB0aGlzLnVwZGF0ZU9mZnNldC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyQ29udGVudCA9IHRoaXMucmVuZGVyQ29udGVudC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyU29ydFRyaWdnZXIgPSB0aGlzLnJlbmRlclNvcnRUcmlnZ2VyLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJIZWxwVHJpZ2dlciA9IHRoaXMucmVuZGVySGVscFRyaWdnZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudERpZE1vdW50ID0gdGhpcy5jb21wb25lbnREaWRNb3VudC5iaW5kKHRoaXMpO1xuXG4gICAgdGhpcy5vbkRyb3AgPSB0aGlzLm9uRHJvcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25EcmFnRW5kID0gdGhpcy5vbkRyYWdFbmQuYmluZCh0aGlzKTtcbiAgICB0aGlzLm9uRHJhZ0V4aXQgPSB0aGlzLm9uRHJhZ0V4aXQuYmluZCh0aGlzKTtcbiAgICB0aGlzLm9uRHJhZ092ZXIgPSB0aGlzLm9uRHJhZ092ZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLm9uRHJhZ1N0YXJ0ID0gdGhpcy5vbkRyYWdTdGFydC5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25EcmFnRW50ZXIgPSB0aGlzLm9uRHJhZ0VudGVyLmJpbmQodGhpcyk7XG4gICAgdGhpcy5vbkRyYWdMZWF2ZSA9IHRoaXMub25EcmFnTGVhdmUuYmluZCh0aGlzKTtcbiAgICB0aGlzLm9uTW91c2VEb3duID0gdGhpcy5vbk1vdXNlRG93bi5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25Nb3VzZVVwID0gdGhpcy5vbk1vdXNlVXAuYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50ICgpIHtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCgpO1xuICAgIHRoaXMubGlzdGVuZXJzID0ge1xuICAgICAgc2Nyb2xsOiBFdmVudHMuYWRkKCdzY3JvbGwnLCB0aGlzLnVwZGF0ZU9mZnNldCksXG4gICAgICByZXNpemU6IEV2ZW50cy5hZGQoJ3Jlc2l6ZScsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FTY3JvbGw6IEV2ZW50cy5hZGQoJ01lc2FTY3JvbGwnLCB0aGlzLnVwZGF0ZU9mZnNldCksXG4gICAgICBNZXNhUmVmbG93OiBFdmVudHMuYWRkKCdNZXNhUmVmbG93JywgdGhpcy51cGRhdGVPZmZzZXQpXG4gICAgfTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50ICgpIHtcbiAgICBPYmplY3QudmFsdWVzKHRoaXMubGlzdGVuZXJzKS5mb3JFYWNoKGxpc3RlbmVySWQgPT4gRXZlbnRzLnJlbW92ZShsaXN0ZW5lcklkKSk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIChuZXdQcm9wcykge1xuICAgIGlmIChuZXdQcm9wc1xuICAgICAgJiYgbmV3UHJvcHMuY29sdW1uICE9PSB0aGlzLnByb3BzLmNvbHVtblxuICAgICAgfHwgbmV3UHJvcHMuY29sdW1uLndpZHRoICE9PSB0aGlzLnByb3BzLmNvbHVtbi53aWR0aCkge1xuICAgICAgdGhpcy51cGRhdGVPZmZzZXQoKTtcbiAgICB9XG4gIH1cblxuICB1cGRhdGVPZmZzZXQgKCkge1xuICAgIGNvbnN0IHsgZWxlbWVudCB9ID0gdGhpcztcbiAgICBpZiAoIWVsZW1lbnQpIHJldHVybjtcbiAgICBsZXQgb2Zmc2V0ID0gVG9vbHRpcC5nZXRPZmZzZXQoZWxlbWVudCk7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IG9mZnNldCB9KTtcbiAgfVxuXG4gIHNvcnRDb2x1bW4gKCkge1xuICAgIGNvbnN0IHsgY29sdW1uLCBzb3J0LCBldmVudEhhbmRsZXJzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgb25Tb3J0IH0gPSBldmVudEhhbmRsZXJzO1xuICAgIGlmICh0eXBlb2Ygb25Tb3J0ICE9PSAnZnVuY3Rpb24nIHx8ICFjb2x1bW4uc29ydGFibGUpIHJldHVybjtcbiAgICBjb25zdCBjdXJyZW50bHlTb3J0aW5nID0gc29ydCAmJiBzb3J0LmNvbHVtbktleSA9PT0gY29sdW1uLmtleTtcbiAgICBjb25zdCBkaXJlY3Rpb24gPSBjdXJyZW50bHlTb3J0aW5nICYmIHNvcnQuZGlyZWN0aW9uID09PSAnYXNjJyA/ICdkZXNjJyA6ICdhc2MnO1xuICAgIHJldHVybiBvblNvcnQoY29sdW1uLCBkaXJlY3Rpb24pO1xuICB9XG5cbiAgb25Nb3VzZURvd24gKGUpIHtcbiAgICBjb25zdCBjbGlja1N0YXJ0ID0gKG5ldyBEYXRlKS5nZXRUaW1lKCk7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IGNsaWNrU3RhcnQgfSk7XG4gIH1cblxuICBvbk1vdXNlVXAgKGUpIHtcbiAgICBjb25zdCB7IGNsaWNrU3RhcnQgfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKCFjbGlja1N0YXJ0KSByZXR1cm47XG4gICAgY29uc3QgY2xpY2tFbmQgPSAobmV3IERhdGUpLmdldFRpbWUoKTtcbiAgICBjb25zdCB0b3RhbFRpbWUgPSAoY2xpY2tFbmQgLSBjbGlja1N0YXJ0KTtcbiAgICB0aGlzLnNldFN0YXRlKHsgY2xpY2tTdGFydDogbnVsbCwgaXNEcmFnVGFyZ2V0OiBmYWxzZSB9KVxuICAgIGlmICh0b3RhbFRpbWUgPD0gNTAwKSB0aGlzLnNvcnRDb2x1bW4oKTtcbiAgICBpZiAodGhpcy5lbGVtZW50KSB0aGlzLmVsZW1lbnQuYmx1cigpO1xuICB9XG5cbiAgLy8gLT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09XG5cbiAgd3JhcENvbnRlbnQgKGNvbnRlbnQgPSBudWxsKSB7XG4gICAgY29uc3QgU29ydFRyaWdnZXIgPSB0aGlzLnJlbmRlclNvcnRUcmlnZ2VyO1xuICAgIGNvbnN0IEhlbHBUcmlnZ2VyID0gdGhpcy5yZW5kZXJIZWxwVHJpZ2dlcjtcbiAgICBjb25zdCBDbGlja0JvdW5kYXJ5ID0gdGhpcy5yZW5kZXJDbGlja0JvdW5kYXJ5O1xuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17aGVhZGluZ0NlbGxDbGFzcygnQ29udGVudCcpfT5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9e2hlYWRpbmdDZWxsQ2xhc3MoWydDb250ZW50JywgJ0FzaWRlJ10pfT5cbiAgICAgICAgICA8U29ydFRyaWdnZXIgLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPXtoZWFkaW5nQ2VsbENsYXNzKFsnQ29udGVudCcsICdMYWJlbCddKX0+XG4gICAgICAgICAge2NvbnRlbnR9XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17aGVhZGluZ0NlbGxDbGFzcyhbJ0NvbnRlbnQnLCAnQXNpZGUnXSl9PlxuICAgICAgICAgIDxDbGlja0JvdW5kYXJ5PlxuICAgICAgICAgICAgPEhlbHBUcmlnZ2VyIC8+XG4gICAgICAgICAgPC9DbGlja0JvdW5kYXJ5PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXJDb250ZW50ICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbiwgY29sdW1uSW5kZXgsIGhlYWRpbmdSb3dJbmRleCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBTb3J0VHJpZ2dlciA9IHRoaXMucmVuZGVyU29ydFRyaWdnZXI7XG4gICAgY29uc3QgSGVscFRyaWdnZXIgPSB0aGlzLnJlbmRlckhlbHBUcmlnZ2VyO1xuICAgIGNvbnN0IENsaWNrQm91bmRhcnkgPSB0aGlzLnJlbmRlckNsaWNrQm91bmRhcnk7XG5cbiAgICBpZiAoJ3JlbmRlckhlYWRpbmcnIGluIGNvbHVtbiAmJiBjb2x1bW4ucmVuZGVySGVhZGluZyA9PT0gZmFsc2UpXG4gICAgICByZXR1cm4gbnVsbDtcbiAgICBpZiAoISdyZW5kZXJIZWFkaW5nJyBpbiBjb2x1bW4gfHwgdHlwZW9mIGNvbHVtbi5yZW5kZXJIZWFkaW5nICE9PSAnZnVuY3Rpb24nKVxuICAgICAgcmV0dXJuIHRoaXMud3JhcENvbnRlbnQoVGVtcGxhdGVzLmhlYWRpbmcoY29sdW1uLCBjb2x1bW5JbmRleCkpO1xuXG4gICAgY29uc3QgY29udGVudCA9IGNvbHVtbi5yZW5kZXJIZWFkaW5nKGNvbHVtbiwgY29sdW1uSW5kZXgsIHsgU29ydFRyaWdnZXIsIEhlbHBUcmlnZ2VyLCBDbGlja0JvdW5kYXJ5IH0pO1xuICAgIGNvbnN0IHsgd3JhcEN1c3RvbUhlYWRpbmdzIH0gPSBjb2x1bW47XG4gICAgY29uc3Qgc2hvdWxkV3JhcCA9ICh3cmFwQ3VzdG9tSGVhZGluZ3MgJiYgdHlwZW9mIHdyYXBDdXN0b21IZWFkaW5ncyA9PT0gJ2Z1bmN0aW9uJylcbiAgICAgID8gd3JhcEN1c3RvbUhlYWRpbmdzKHsgY29sdW1uLCBjb2x1bW5JbmRleCwgaGVhZGluZ1Jvd0luZGV4IH0pXG4gICAgICA6IHdyYXBDdXN0b21IZWFkaW5ncztcblxuICAgIHJldHVybiBzaG91bGRXcmFwID8gdGhpcy53cmFwQ29udGVudChjb250ZW50KSA6IGNvbnRlbnQ7XG4gIH1cblxuICByZW5kZXJDbGlja0JvdW5kYXJ5ICh7IGNoaWxkcmVuIH0pIHtcbiAgICBjb25zdCBzdHlsZSA9IHsgZGlzcGxheTogJ2lubGluZS1ibG9jaycgfTtcbiAgICBjb25zdCBzdG9wUHJvcGFnYXRpb24gPSAobm9kZSkgPT4ge1xuICAgICAgaWYgKCFub2RlKSByZXR1cm4gbnVsbDtcbiAgICAgIGNvbnN0IGluc3RhbmNlID0gbmV3IEV2ZW50c0ZhY3Rvcnkobm9kZSk7XG4gICAgICBpbnN0YW5jZS5hZGQoJ2NsaWNrJywgKGUpID0+IHtcbiAgICAgICAgZS5zdG9wUHJvcGFnYXRpb24oKTtcbiAgICAgIH0pO1xuICAgIH1cbiAgICByZXR1cm4gPGRpdiByZWY9e3N0b3BQcm9wYWdhdGlvbn0gc3R5bGU9e3N0eWxlfSBjaGlsZHJlbj17Y2hpbGRyZW59IC8+XG4gIH1cblxuICByZW5kZXJTb3J0VHJpZ2dlciAoKSB7XG4gICAgY29uc3QgeyBjb2x1bW4sIHNvcnQsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBjb2x1bW5LZXksIGRpcmVjdGlvbiB9ID0gc29ydCA/IHNvcnQgOiB7fTtcbiAgICBjb25zdCB7IGtleSwgc29ydGFibGUgfSA9IGNvbHVtbiA/IGNvbHVtbiA6IHt9O1xuICAgIGNvbnN0IHsgb25Tb3J0IH0gPSBldmVudEhhbmRsZXJzID8gZXZlbnRIYW5kbGVycyA6IHt9O1xuICAgIGNvbnN0IGlzQWN0aXZlID0gY29sdW1uS2V5ID09PSBrZXk7XG5cbiAgICBpZiAoIXNvcnRhYmxlIHx8ICh0eXBlb2Ygb25Tb3J0ICE9PSAnZnVuY3Rpb24nICYmICFpc0FjdGl2ZSkpIHJldHVybiBudWxsO1xuXG4gICAgY29uc3Qgc29ydEljb24gPSAhaXNBY3RpdmVcbiAgICAgID8gJ3NvcnQgaW5hY3RpdmUnXG4gICAgICA6ICdzb3J0LWFtb3VudC0nICsgZGlyZWN0aW9uICsgJyBhY3RpdmUnO1xuXG4gICAgcmV0dXJuICg8SWNvbiBmYT17c29ydEljb24gKyAnIFRyaWdnZXIgU29ydFRyaWdnZXInfSAvPik7XG4gIH1cblxuICByZW5kZXJIZWxwVHJpZ2dlciAoKSB7XG4gICAgY29uc3QgeyBjb2x1bW4gfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBvZmZzZXQgfSA9IHRoaXMuc3RhdGU7XG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIGhlaWdodCB9ID0gb2Zmc2V0ID8gb2Zmc2V0IDoge307XG4gICAgY29uc3QgcG9zaXRpb24gPSB7IHRvcDogdG9wICsgaGVpZ2h0LCBsZWZ0IH07XG5cbiAgICBpZiAoIWNvbHVtbi5oZWxwVGV4dCkgcmV0dXJuIG51bGw7XG4gICAgcmV0dXJuIChcbiAgICAgIDxUb29sdGlwIHBvc2l0aW9uPXtwb3NpdGlvbn0gY2xhc3NOYW1lPVwiVHJpZ2dlciBIZWxwVHJpZ2dlclwiIGNvbnRlbnQ9e2NvbHVtbi5oZWxwVGV4dH0+XG4gICAgICAgIDxJY29uIGZhPVwicXVlc3Rpb24tY2lyY2xlXCIgLz5cbiAgICAgIDwvVG9vbHRpcD5cbiAgICApO1xuICB9XG5cbiAgLy8gLT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09XG5cbiAgb25EcmFnU3RhcnQgKGV2ZW50KSB7XG4gICAgY29uc3QgeyBrZXkgfSA9IHRoaXMucHJvcHMuY29sdW1uO1xuICAgIGV2ZW50LmRhdGFUcmFuc2Zlci5lZmZlY3RBbGxvd2VkID0gJ2NvcHknO1xuICAgIGV2ZW50LmRhdGFUcmFuc2Zlci5zZXREYXRhKCd0ZXh0Jywga2V5KTtcbiAgICB0aGlzLnNldFN0YXRlKHsgaXNEcmFnZ2luZzogdHJ1ZSB9KTtcbiAgICByZXR1cm4gZXZlbnQ7XG4gIH1cblxuICBvbkRyYWdFbmQgKGV2ZW50KSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IGlzRHJhZ2dpbmc6IGZhbHNlLCBpc0RyYWdUYXJnZXQ6IGZhbHNlIH0pO1xuICAgIHRoaXMuZWxlbWVudC5ibHVyKCk7XG4gICAgZXZlbnQucHJldmVudERlZmF1bHQoKTtcbiAgfVxuXG4gIG9uRHJhZ0VudGVyIChldmVudCkge1xuICAgIGNvbnN0IGRyYWdlZSA9IGV2ZW50LmRhdGFUcmFuc2Zlci5nZXREYXRhKCd0ZXh0Jyk7XG4gICAgaWYgKCF0aGlzLnN0YXRlLmlzRHJhZ1RhcmdldCkgdGhpcy5zZXRTdGF0ZSh7IGlzRHJhZ1RhcmdldDogdHJ1ZSB9KTtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICB9XG5cbiAgb25EcmFnRXhpdCAoZXZlbnQpIHtcbiAgICB0aGlzLnNldFN0YXRlKHsgaXNEcmFnVGFyZ2V0OiBmYWxzZSB9KTtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICB9XG5cbiAgb25EcmFnT3ZlciAoZXZlbnQpIHtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICB9XG5cbiAgb25EcmFnTGVhdmUgKGV2ZW50KSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IGlzRHJhZ1RhcmdldDogZmFsc2UgfSk7XG4gICAgdGhpcy5lbGVtZW50LmJsdXIoKTtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICB9XG5cbiAgb25Ecm9wIChldmVudCkge1xuICAgIHRoaXMuZWxlbWVudC5ibHVyKCk7XG4gICAgZXZlbnQucHJldmVudERlZmF1bHQoKTtcbiAgICBjb25zdCB7IGV2ZW50SGFuZGxlcnMsIGNvbHVtbkluZGV4IH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgb25Db2x1bW5SZW9yZGVyIH0gPSBldmVudEhhbmRsZXJzO1xuICAgIGlmICh0eXBlb2Ygb25Db2x1bW5SZW9yZGVyICE9PSAnZnVuY3Rpb24nKSByZXR1cm47XG4gICAgY29uc3QgZHJhZ2dlZENvbHVtbiA9IGV2ZW50LmRhdGFUcmFuc2Zlci5nZXREYXRhKCd0ZXh0Jyk7XG4gICAgaWYgKHRoaXMuc3RhdGUuaXNEcmFnVGFyZ2V0KSB0aGlzLnNldFN0YXRlKHsgaXNEcmFnVGFyZ2V0OiBmYWxzZSB9KTtcbiAgICBvbkNvbHVtblJlb3JkZXIoZHJhZ2dlZENvbHVtbiwgY29sdW1uSW5kZXgpO1xuICB9XG5cbiAgZ2V0RG9tRXZlbnRzICgpIHtcbiAgICBjb25zdCB7XG4gICAgICBvbk1vdXNlRG93biwgb25Nb3VzZVVwLFxuICAgICAgb25EcmFnU3RhcnQsIG9uRHJhZ0VuZCxcbiAgICAgIG9uRHJhZ0VudGVyLCBvbkRyYWdFeGl0LFxuICAgICAgb25EcmFnT3Zlciwgb25EcmFnTGVhdmUsXG4gICAgICBvbkRyb3BcbiAgICB9ID0gdGhpcztcbiAgICByZXR1cm4ge1xuICAgICAgb25Nb3VzZURvd24sIG9uTW91c2VVcCxcbiAgICAgIG9uRHJhZ1N0YXJ0LCBvbkRyYWdFbmQsXG4gICAgICBvbkRyYWdFbnRlciwgb25EcmFnRXhpdCxcbiAgICAgIG9uRHJhZ092ZXIsIG9uRHJhZ0xlYXZlLFxuICAgICAgb25Ecm9wXG4gICAgfTtcbiAgfVxuXG5cbiAgZ2V0Q2xhc3NOYW1lICgpIHtcbiAgICBjb25zdCB7IGtleSwgaGVhZGluZ0NsYXNzTmFtZSB9ID0gdGhpcy5wcm9wcy5jb2x1bW47XG4gICAgY29uc3QgeyBpc0RyYWdnaW5nLCBpc0RyYWdUYXJnZXQgfSA9IHRoaXMuc3RhdGU7XG4gICAgY29uc3QgbW9kaWZpZXJzID0gWydrZXktJyArIGtleV07XG4gICAgaWYgKGlzRHJhZ2dpbmcpIG1vZGlmaWVycy5wdXNoKCdEcmFnZ2luZycpO1xuICAgIGlmIChpc0RyYWdUYXJnZXQpIG1vZGlmaWVycy5wdXNoKCdEcmFnVGFyZ2V0Jyk7XG4gICAgcmV0dXJuICh0eXBlb2YgaGVhZGluZ0NsYXNzTmFtZSA9PT0gJ3N0cmluZycgPyBoZWFkaW5nQ2xhc3NOYW1lICsgJyAnIDogJycpICsgaGVhZGluZ0NlbGxDbGFzcyhudWxsLCBtb2RpZmllcnMpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbiwgZXZlbnRIYW5kbGVycywgcHJpbWFyeSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGtleSwgaGVhZGluZ1N0eWxlLCB3aWR0aCwgcmVuZGVySGVhZGluZyB9ID0gY29sdW1uO1xuICAgIGNvbnN0IHdpZHRoU3R5bGUgPSB3aWR0aCA/IHsgd2lkdGgsIG1heFdpZHRoOiB3aWR0aCwgbWluV2lkdGg6IHdpZHRoIH0gOiB7fTtcblxuICAgIGNvbnN0IHN0eWxlID0gT2JqZWN0LmFzc2lnbih7fSwgaGVhZGluZ1N0eWxlID8gaGVhZGluZ1N0eWxlIDoge30sIHdpZHRoU3R5bGUpO1xuICAgIGNvbnN0IHJlZiA9IGVsZW1lbnQgPT4gdGhpcy5lbGVtZW50ID0gZWxlbWVudDtcblxuICAgIGNvbnN0IGNoaWxkcmVuID0gdGhpcy5yZW5kZXJDb250ZW50KCk7XG4gICAgY29uc3QgY2xhc3NOYW1lID0gdGhpcy5nZXRDbGFzc05hbWUoKTtcbiAgICBjb25zdCBkb21FdmVudHMgPSB0aGlzLmdldERvbUV2ZW50cygpO1xuXG4gICAgY29uc3QgZHJhZ2dhYmxlID0gcHJpbWFyeVxuICAgICAgJiYgY29sdW1uLm1vdmVhYmxlXG4gICAgICAmJiAhY29sdW1uLnByaW1hcnlcbiAgICAgICYmIHR5cGVvZiBldmVudEhhbmRsZXJzLm9uQ29sdW1uUmVvcmRlciA9PT0gJ2Z1bmN0aW9uJztcblxuICAgIGNvbnN0IHByb3BzID0geyBzdHlsZSwga2V5LCByZWYsIGRyYWdnYWJsZSwgY2hpbGRyZW4sIGNsYXNzTmFtZSB9O1xuXG4gICAgcmV0dXJuIGNvbHVtbi5oaWRkZW4gPyBudWxsIDogPHRoIHsuLi5wcm9wc30gey4uLmRvbUV2ZW50c30gLz5cbiAgfVxufTtcblxuSGVhZGluZ0NlbGwucHJvcFR5cGVzID0ge1xuICBzb3J0OiBQcm9wVHlwZXMub2JqZWN0LFxuICBldmVudEhhbmRsZXJzOiBQcm9wVHlwZXMub2JqZWN0LFxuICBjb2x1bW46IFByb3BUeXBlcy5vYmplY3QuaXNSZXF1aXJlZCxcbiAgY29sdW1uSW5kZXg6IFByb3BUeXBlcy5udW1iZXIuaXNSZXF1aXJlZFxufTtcblxuZXhwb3J0IGRlZmF1bHQgSGVhZGluZ0NlbGw7XG4iXX0=