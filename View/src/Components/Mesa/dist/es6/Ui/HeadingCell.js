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
          className = _props$column.className;
      var _state = this.state,
          isDragging = _state.isDragging,
          isDragTarget = _state.isDragTarget;

      var modifiers = ['key-' + key];
      if (isDragging) modifiers.push('Dragging');
      if (isDragTarget) modifiers.push('DragTarget');
      return (typeof className === 'string' ? className + ' ' : '') + headingCellClass(null, modifiers);
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9IZWFkaW5nQ2VsbC5qc3giXSwibmFtZXMiOlsiaGVhZGluZ0NlbGxDbGFzcyIsIkhlYWRpbmdDZWxsIiwicHJvcHMiLCJzdGF0ZSIsIm9mZnNldCIsImlzRHJhZ2dpbmciLCJpc0RyYWdUYXJnZXQiLCJjbGlja1N0YXJ0IiwiZ2V0Q2xhc3NOYW1lIiwiYmluZCIsImdldERvbUV2ZW50cyIsInNvcnRDb2x1bW4iLCJ1cGRhdGVPZmZzZXQiLCJyZW5kZXJDb250ZW50IiwicmVuZGVyU29ydFRyaWdnZXIiLCJyZW5kZXJIZWxwVHJpZ2dlciIsImNvbXBvbmVudERpZE1vdW50Iiwib25Ecm9wIiwib25EcmFnRW5kIiwib25EcmFnRXhpdCIsIm9uRHJhZ092ZXIiLCJvbkRyYWdTdGFydCIsIm9uRHJhZ0VudGVyIiwib25EcmFnTGVhdmUiLCJvbk1vdXNlRG93biIsIm9uTW91c2VVcCIsImxpc3RlbmVycyIsInNjcm9sbCIsImFkZCIsInJlc2l6ZSIsIk1lc2FTY3JvbGwiLCJNZXNhUmVmbG93IiwiT2JqZWN0IiwidmFsdWVzIiwiZm9yRWFjaCIsInJlbW92ZSIsImxpc3RlbmVySWQiLCJuZXdQcm9wcyIsImNvbHVtbiIsIndpZHRoIiwiZWxlbWVudCIsImdldE9mZnNldCIsInNldFN0YXRlIiwic29ydCIsImV2ZW50SGFuZGxlcnMiLCJvblNvcnQiLCJzb3J0YWJsZSIsImN1cnJlbnRseVNvcnRpbmciLCJjb2x1bW5LZXkiLCJrZXkiLCJkaXJlY3Rpb24iLCJlIiwiRGF0ZSIsImdldFRpbWUiLCJjbGlja0VuZCIsInRvdGFsVGltZSIsImJsdXIiLCJjb250ZW50IiwiU29ydFRyaWdnZXIiLCJIZWxwVHJpZ2dlciIsIkNsaWNrQm91bmRhcnkiLCJyZW5kZXJDbGlja0JvdW5kYXJ5IiwiY29sdW1uSW5kZXgiLCJoZWFkaW5nUm93SW5kZXgiLCJyZW5kZXJIZWFkaW5nIiwid3JhcENvbnRlbnQiLCJoZWFkaW5nIiwid3JhcEN1c3RvbUhlYWRpbmdzIiwic2hvdWxkV3JhcCIsImNoaWxkcmVuIiwic3R5bGUiLCJkaXNwbGF5Iiwic3RvcFByb3BhZ2F0aW9uIiwibm9kZSIsImluc3RhbmNlIiwiaXNBY3RpdmUiLCJzb3J0SWNvbiIsInRvcCIsImxlZnQiLCJoZWlnaHQiLCJwb3NpdGlvbiIsImhlbHBUZXh0IiwiZXZlbnQiLCJkYXRhVHJhbnNmZXIiLCJlZmZlY3RBbGxvd2VkIiwic2V0RGF0YSIsInByZXZlbnREZWZhdWx0IiwiZHJhZ2VlIiwiZ2V0RGF0YSIsIm9uQ29sdW1uUmVvcmRlciIsImRyYWdnZWRDb2x1bW4iLCJjbGFzc05hbWUiLCJtb2RpZmllcnMiLCJwdXNoIiwicHJpbWFyeSIsImhlYWRpbmdTdHlsZSIsIndpZHRoU3R5bGUiLCJtYXhXaWR0aCIsIm1pbldpZHRoIiwiYXNzaWduIiwicmVmIiwiZG9tRXZlbnRzIiwiZHJhZ2dhYmxlIiwibW92ZWFibGUiLCJoaWRkZW4iLCJQdXJlQ29tcG9uZW50IiwicHJvcFR5cGVzIiwib2JqZWN0IiwiaXNSZXF1aXJlZCIsIm51bWJlciJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7QUFDQTs7Ozs7Ozs7Ozs7O0FBRUEsSUFBTUEsbUJBQW1CLDJCQUFlLGFBQWYsQ0FBekI7O0lBRU1DLFc7OztBQUNKLHVCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsMEhBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLEtBQUwsR0FBYTtBQUNYQyxjQUFRLElBREc7QUFFWEMsa0JBQVksS0FGRDtBQUdYQyxvQkFBYyxLQUhIO0FBSVhDLGtCQUFZO0FBSkQsS0FBYjs7QUFPQSxVQUFLQyxZQUFMLEdBQW9CLE1BQUtBLFlBQUwsQ0FBa0JDLElBQWxCLE9BQXBCO0FBQ0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCRCxJQUFsQixPQUFwQjtBQUNBLFVBQUtFLFVBQUwsR0FBa0IsTUFBS0EsVUFBTCxDQUFnQkYsSUFB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LFNBQVosR0FBd0I7QUFDdEIvRCxRQUFNLG9CQUFVZ0UsTUFETTtBQUV0Qi9ELGlCQUFlLG9CQUFVK0QsTUFGSDtBQUd0QnJFLFVBQVEsb0JBQVVxRSxNQUFWLENBQWlCQyxVQUhIO0FBSXRCOUMsZUFBYSxvQkFBVStDLE1BQVYsQ0FBaUJEO0FBSlIsQ0FBeEI7O2tCQU9lM0csVyIsImZpbGUiOiJIZWFkaW5nQ2VsbC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgVGVtcGxhdGVzIGZyb20gJy4uL1RlbXBsYXRlcyc7XG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuaW1wb3J0IFRvb2x0aXAgZnJvbSAnLi4vQ29tcG9uZW50cy9Ub29sdGlwJztcbmltcG9ydCB7IG1ha2VDbGFzc2lmaWVyIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuaW1wb3J0IEV2ZW50cywgeyBFdmVudHNGYWN0b3J5IH0gZnJvbSAnLi4vVXRpbHMvRXZlbnRzJztcblxuY29uc3QgaGVhZGluZ0NlbGxDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdIZWFkaW5nQ2VsbCcpO1xuXG5jbGFzcyBIZWFkaW5nQ2VsbCBleHRlbmRzIFJlYWN0LlB1cmVDb21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIG9mZnNldDogbnVsbCxcbiAgICAgIGlzRHJhZ2dpbmc6IGZhbHNlLFxuICAgICAgaXNEcmFnVGFyZ2V0OiBmYWxzZSxcbiAgICAgIGNsaWNrU3RhcnQ6IG51bGxcbiAgICB9O1xuXG4gICAgdGhpcy5nZXRDbGFzc05hbWUgPSB0aGlzLmdldENsYXNzTmFtZS5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0RG9tRXZlbnRzID0gdGhpcy5nZXREb21FdmVudHMuYmluZCh0aGlzKTtcbiAgICB0aGlzLnNvcnRDb2x1bW4gPSB0aGlzLnNvcnRDb2x1bW4uYmluZCh0aGlzKTtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCA9IHRoaXMudXBkYXRlT2Zmc2V0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJDb250ZW50ID0gdGhpcy5yZW5kZXJDb250ZW50LmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJTb3J0VHJpZ2dlciA9IHRoaXMucmVuZGVyU29ydFRyaWdnZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlckhlbHBUcmlnZ2VyID0gdGhpcy5yZW5kZXJIZWxwVHJpZ2dlci5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50RGlkTW91bnQgPSB0aGlzLmNvbXBvbmVudERpZE1vdW50LmJpbmQodGhpcyk7XG5cbiAgICB0aGlzLm9uRHJvcCA9IHRoaXMub25Ecm9wLmJpbmQodGhpcyk7XG4gICAgdGhpcy5vbkRyYWdFbmQgPSB0aGlzLm9uRHJhZ0VuZC5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25EcmFnRXhpdCA9IHRoaXMub25EcmFnRXhpdC5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25EcmFnT3ZlciA9IHRoaXMub25EcmFnT3Zlci5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25EcmFnU3RhcnQgPSB0aGlzLm9uRHJhZ1N0YXJ0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5vbkRyYWdFbnRlciA9IHRoaXMub25EcmFnRW50ZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLm9uRHJhZ0xlYXZlID0gdGhpcy5vbkRyYWdMZWF2ZS5iaW5kKHRoaXMpO1xuICAgIHRoaXMub25Nb3VzZURvd24gPSB0aGlzLm9uTW91c2VEb3duLmJpbmQodGhpcyk7XG4gICAgdGhpcy5vbk1vdXNlVXAgPSB0aGlzLm9uTW91c2VVcC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0KCk7XG4gICAgdGhpcy5saXN0ZW5lcnMgPSB7XG4gICAgICBzY3JvbGw6IEV2ZW50cy5hZGQoJ3Njcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIHJlc2l6ZTogRXZlbnRzLmFkZCgncmVzaXplJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVNjcm9sbDogRXZlbnRzLmFkZCgnTWVzYVNjcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FSZWZsb3c6IEV2ZW50cy5hZGQoJ01lc2FSZWZsb3cnLCB0aGlzLnVwZGF0ZU9mZnNldClcbiAgICB9O1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIE9iamVjdC52YWx1ZXModGhpcy5saXN0ZW5lcnMpLmZvckVhY2gobGlzdGVuZXJJZCA9PiBFdmVudHMucmVtb3ZlKGxpc3RlbmVySWQpKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgKG5ld1Byb3BzKSB7XG4gICAgaWYgKG5ld1Byb3BzXG4gICAgICAmJiBuZXdQcm9wcy5jb2x1bW4gIT09IHRoaXMucHJvcHMuY29sdW1uXG4gICAgICB8fCBuZXdQcm9wcy5jb2x1bW4ud2lkdGggIT09IHRoaXMucHJvcHMuY29sdW1uLndpZHRoKSB7XG4gICAgICB0aGlzLnVwZGF0ZU9mZnNldCgpO1xuICAgIH1cbiAgfVxuXG4gIHVwZGF0ZU9mZnNldCAoKSB7XG4gICAgY29uc3QgeyBlbGVtZW50IH0gPSB0aGlzO1xuICAgIGlmICghZWxlbWVudCkgcmV0dXJuO1xuICAgIGxldCBvZmZzZXQgPSBUb29sdGlwLmdldE9mZnNldChlbGVtZW50KTtcbiAgICB0aGlzLnNldFN0YXRlKHsgb2Zmc2V0IH0pO1xuICB9XG5cbiAgc29ydENvbHVtbiAoKSB7XG4gICAgY29uc3QgeyBjb2x1bW4sIHNvcnQsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBvblNvcnQgfSA9IGV2ZW50SGFuZGxlcnM7XG4gICAgaWYgKHR5cGVvZiBvblNvcnQgIT09ICdmdW5jdGlvbicgfHwgIWNvbHVtbi5zb3J0YWJsZSkgcmV0dXJuO1xuICAgIGNvbnN0IGN1cnJlbnRseVNvcnRpbmcgPSBzb3J0ICYmIHNvcnQuY29sdW1uS2V5ID09PSBjb2x1bW4ua2V5O1xuICAgIGNvbnN0IGRpcmVjdGlvbiA9IGN1cnJlbnRseVNvcnRpbmcgJiYgc29ydC5kaXJlY3Rpb24gPT09ICdhc2MnID8gJ2Rlc2MnIDogJ2FzYyc7XG4gICAgcmV0dXJuIG9uU29ydChjb2x1bW4sIGRpcmVjdGlvbik7XG4gIH1cblxuICBvbk1vdXNlRG93biAoZSkge1xuICAgIGNvbnN0IGNsaWNrU3RhcnQgPSAobmV3IERhdGUpLmdldFRpbWUoKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgY2xpY2tTdGFydCB9KTtcbiAgfVxuXG4gIG9uTW91c2VVcCAoZSkge1xuICAgIGNvbnN0IHsgY2xpY2tTdGFydCB9ID0gdGhpcy5zdGF0ZTtcbiAgICBpZiAoIWNsaWNrU3RhcnQpIHJldHVybjtcbiAgICBjb25zdCBjbGlja0VuZCA9IChuZXcgRGF0ZSkuZ2V0VGltZSgpO1xuICAgIGNvbnN0IHRvdGFsVGltZSA9IChjbGlja0VuZCAtIGNsaWNrU3RhcnQpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBjbGlja1N0YXJ0OiBudWxsLCBpc0RyYWdUYXJnZXQ6IGZhbHNlIH0pXG4gICAgaWYgKHRvdGFsVGltZSA8PSA1MDApIHRoaXMuc29ydENvbHVtbigpO1xuICAgIGlmICh0aGlzLmVsZW1lbnQpIHRoaXMuZWxlbWVudC5ibHVyKCk7XG4gIH1cblxuICAvLyAtPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT1cblxuICB3cmFwQ29udGVudCAoY29udGVudCA9IG51bGwpIHtcbiAgICBjb25zdCBTb3J0VHJpZ2dlciA9IHRoaXMucmVuZGVyU29ydFRyaWdnZXI7XG4gICAgY29uc3QgSGVscFRyaWdnZXIgPSB0aGlzLnJlbmRlckhlbHBUcmlnZ2VyO1xuICAgIGNvbnN0IENsaWNrQm91bmRhcnkgPSB0aGlzLnJlbmRlckNsaWNrQm91bmRhcnk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtoZWFkaW5nQ2VsbENsYXNzKCdDb250ZW50Jyl9PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17aGVhZGluZ0NlbGxDbGFzcyhbJ0NvbnRlbnQnLCAnQXNpZGUnXSl9PlxuICAgICAgICAgIDxTb3J0VHJpZ2dlciAvPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9e2hlYWRpbmdDZWxsQ2xhc3MoWydDb250ZW50JywgJ0xhYmVsJ10pfT5cbiAgICAgICAgICB7Y29udGVudH1cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPXtoZWFkaW5nQ2VsbENsYXNzKFsnQ29udGVudCcsICdBc2lkZSddKX0+XG4gICAgICAgICAgPENsaWNrQm91bmRhcnk+XG4gICAgICAgICAgICA8SGVscFRyaWdnZXIgLz5cbiAgICAgICAgICA8L0NsaWNrQm91bmRhcnk+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlckNvbnRlbnQgKCkge1xuICAgIGNvbnN0IHsgY29sdW1uLCBjb2x1bW5JbmRleCwgaGVhZGluZ1Jvd0luZGV4IH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IFNvcnRUcmlnZ2VyID0gdGhpcy5yZW5kZXJTb3J0VHJpZ2dlcjtcbiAgICBjb25zdCBIZWxwVHJpZ2dlciA9IHRoaXMucmVuZGVySGVscFRyaWdnZXI7XG4gICAgY29uc3QgQ2xpY2tCb3VuZGFyeSA9IHRoaXMucmVuZGVyQ2xpY2tCb3VuZGFyeTtcblxuICAgIGlmICgncmVuZGVySGVhZGluZycgaW4gY29sdW1uICYmIGNvbHVtbi5yZW5kZXJIZWFkaW5nID09PSBmYWxzZSlcbiAgICAgIHJldHVybiBudWxsO1xuICAgIGlmICghJ3JlbmRlckhlYWRpbmcnIGluIGNvbHVtbiB8fCB0eXBlb2YgY29sdW1uLnJlbmRlckhlYWRpbmcgIT09ICdmdW5jdGlvbicpXG4gICAgICByZXR1cm4gdGhpcy53cmFwQ29udGVudChUZW1wbGF0ZXMuaGVhZGluZyhjb2x1bW4sIGNvbHVtbkluZGV4KSk7XG5cbiAgICBjb25zdCBjb250ZW50ID0gY29sdW1uLnJlbmRlckhlYWRpbmcoY29sdW1uLCBjb2x1bW5JbmRleCwgeyBTb3J0VHJpZ2dlciwgSGVscFRyaWdnZXIsIENsaWNrQm91bmRhcnkgfSk7XG4gICAgY29uc3QgeyB3cmFwQ3VzdG9tSGVhZGluZ3MgfSA9IGNvbHVtbjtcbiAgICBjb25zdCBzaG91bGRXcmFwID0gKHdyYXBDdXN0b21IZWFkaW5ncyAmJiB0eXBlb2Ygd3JhcEN1c3RvbUhlYWRpbmdzID09PSAnZnVuY3Rpb24nKVxuICAgICAgPyB3cmFwQ3VzdG9tSGVhZGluZ3MoeyBjb2x1bW4sIGNvbHVtbkluZGV4LCBoZWFkaW5nUm93SW5kZXggfSlcbiAgICAgIDogd3JhcEN1c3RvbUhlYWRpbmdzO1xuXG4gICAgcmV0dXJuIHNob3VsZFdyYXAgPyB0aGlzLndyYXBDb250ZW50KGNvbnRlbnQpIDogY29udGVudDtcbiAgfVxuXG4gIHJlbmRlckNsaWNrQm91bmRhcnkgKHsgY2hpbGRyZW4gfSkge1xuICAgIGNvbnN0IHN0eWxlID0geyBkaXNwbGF5OiAnaW5saW5lLWJsb2NrJyB9O1xuICAgIGNvbnN0IHN0b3BQcm9wYWdhdGlvbiA9IChub2RlKSA9PiB7XG4gICAgICBpZiAoIW5vZGUpIHJldHVybiBudWxsO1xuICAgICAgY29uc3QgaW5zdGFuY2UgPSBuZXcgRXZlbnRzRmFjdG9yeShub2RlKTtcbiAgICAgIGluc3RhbmNlLmFkZCgnY2xpY2snLCAoZSkgPT4ge1xuICAgICAgICBlLnN0b3BQcm9wYWdhdGlvbigpO1xuICAgICAgfSk7XG4gICAgfVxuICAgIHJldHVybiA8ZGl2IHJlZj17c3RvcFByb3BhZ2F0aW9ufSBzdHlsZT17c3R5bGV9IGNoaWxkcmVuPXtjaGlsZHJlbn0gLz5cbiAgfVxuXG4gIHJlbmRlclNvcnRUcmlnZ2VyICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbiwgc29ydCwgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGNvbHVtbktleSwgZGlyZWN0aW9uIH0gPSBzb3J0ID8gc29ydCA6IHt9O1xuICAgIGNvbnN0IHsga2V5LCBzb3J0YWJsZSB9ID0gY29sdW1uID8gY29sdW1uIDoge307XG4gICAgY29uc3QgeyBvblNvcnQgfSA9IGV2ZW50SGFuZGxlcnMgPyBldmVudEhhbmRsZXJzIDoge307XG4gICAgY29uc3QgaXNBY3RpdmUgPSBjb2x1bW5LZXkgPT09IGtleTtcblxuICAgIGlmICghc29ydGFibGUgfHwgKHR5cGVvZiBvblNvcnQgIT09ICdmdW5jdGlvbicgJiYgIWlzQWN0aXZlKSkgcmV0dXJuIG51bGw7XG5cbiAgICBjb25zdCBzb3J0SWNvbiA9ICFpc0FjdGl2ZVxuICAgICAgPyAnc29ydCBpbmFjdGl2ZSdcbiAgICAgIDogJ3NvcnQtYW1vdW50LScgKyBkaXJlY3Rpb24gKyAnIGFjdGl2ZSc7XG5cbiAgICByZXR1cm4gKDxJY29uIGZhPXtzb3J0SWNvbiArICcgVHJpZ2dlciBTb3J0VHJpZ2dlcid9IC8+KTtcbiAgfVxuXG4gIHJlbmRlckhlbHBUcmlnZ2VyICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbiB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IG9mZnNldCB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCB7IHRvcCwgbGVmdCwgaGVpZ2h0IH0gPSBvZmZzZXQgPyBvZmZzZXQgOiB7fTtcbiAgICBjb25zdCBwb3NpdGlvbiA9IHsgdG9wOiB0b3AgKyBoZWlnaHQsIGxlZnQgfTtcblxuICAgIGlmICghY29sdW1uLmhlbHBUZXh0KSByZXR1cm4gbnVsbDtcbiAgICByZXR1cm4gKFxuICAgICAgPFRvb2x0aXAgcG9zaXRpb249e3Bvc2l0aW9ufSBjbGFzc05hbWU9XCJUcmlnZ2VyIEhlbHBUcmlnZ2VyXCIgY29udGVudD17Y29sdW1uLmhlbHBUZXh0fT5cbiAgICAgICAgPEljb24gZmE9XCJxdWVzdGlvbi1jaXJjbGVcIiAvPlxuICAgICAgPC9Ub29sdGlwPlxuICAgICk7XG4gIH1cblxuICAvLyAtPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT1cblxuICBvbkRyYWdTdGFydCAoZXZlbnQpIHtcbiAgICBjb25zdCB7IGtleSB9ID0gdGhpcy5wcm9wcy5jb2x1bW47XG4gICAgZXZlbnQuZGF0YVRyYW5zZmVyLmVmZmVjdEFsbG93ZWQgPSAnY29weSc7XG4gICAgZXZlbnQuZGF0YVRyYW5zZmVyLnNldERhdGEoJ3RleHQnLCBrZXkpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBpc0RyYWdnaW5nOiB0cnVlIH0pO1xuICAgIHJldHVybiBldmVudDtcbiAgfVxuXG4gIG9uRHJhZ0VuZCAoZXZlbnQpIHtcbiAgICB0aGlzLnNldFN0YXRlKHsgaXNEcmFnZ2luZzogZmFsc2UsIGlzRHJhZ1RhcmdldDogZmFsc2UgfSk7XG4gICAgdGhpcy5lbGVtZW50LmJsdXIoKTtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICB9XG5cbiAgb25EcmFnRW50ZXIgKGV2ZW50KSB7XG4gICAgY29uc3QgZHJhZ2VlID0gZXZlbnQuZGF0YVRyYW5zZmVyLmdldERhdGEoJ3RleHQnKTtcbiAgICBpZiAoIXRoaXMuc3RhdGUuaXNEcmFnVGFyZ2V0KSB0aGlzLnNldFN0YXRlKHsgaXNEcmFnVGFyZ2V0OiB0cnVlIH0pO1xuICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XG4gIH1cblxuICBvbkRyYWdFeGl0IChldmVudCkge1xuICAgIHRoaXMuc2V0U3RhdGUoeyBpc0RyYWdUYXJnZXQ6IGZhbHNlIH0pO1xuICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XG4gIH1cblxuICBvbkRyYWdPdmVyIChldmVudCkge1xuICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XG4gIH1cblxuICBvbkRyYWdMZWF2ZSAoZXZlbnQpIHtcbiAgICB0aGlzLnNldFN0YXRlKHsgaXNEcmFnVGFyZ2V0OiBmYWxzZSB9KTtcbiAgICB0aGlzLmVsZW1lbnQuYmx1cigpO1xuICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XG4gIH1cblxuICBvbkRyb3AgKGV2ZW50KSB7XG4gICAgdGhpcy5lbGVtZW50LmJsdXIoKTtcbiAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xuICAgIGNvbnN0IHsgZXZlbnRIYW5kbGVycywgY29sdW1uSW5kZXggfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBvbkNvbHVtblJlb3JkZXIgfSA9IGV2ZW50SGFuZGxlcnM7XG4gICAgaWYgKHR5cGVvZiBvbkNvbHVtblJlb3JkZXIgIT09ICdmdW5jdGlvbicpIHJldHVybjtcbiAgICBjb25zdCBkcmFnZ2VkQ29sdW1uID0gZXZlbnQuZGF0YVRyYW5zZmVyLmdldERhdGEoJ3RleHQnKTtcbiAgICBpZiAodGhpcy5zdGF0ZS5pc0RyYWdUYXJnZXQpIHRoaXMuc2V0U3RhdGUoeyBpc0RyYWdUYXJnZXQ6IGZhbHNlIH0pO1xuICAgIG9uQ29sdW1uUmVvcmRlcihkcmFnZ2VkQ29sdW1uLCBjb2x1bW5JbmRleCk7XG4gIH1cblxuICBnZXREb21FdmVudHMgKCkge1xuICAgIGNvbnN0IHtcbiAgICAgIG9uTW91c2VEb3duLCBvbk1vdXNlVXAsXG4gICAgICBvbkRyYWdTdGFydCwgb25EcmFnRW5kLFxuICAgICAgb25EcmFnRW50ZXIsIG9uRHJhZ0V4aXQsXG4gICAgICBvbkRyYWdPdmVyLCBvbkRyYWdMZWF2ZSxcbiAgICAgIG9uRHJvcFxuICAgIH0gPSB0aGlzO1xuICAgIHJldHVybiB7XG4gICAgICBvbk1vdXNlRG93biwgb25Nb3VzZVVwLFxuICAgICAgb25EcmFnU3RhcnQsIG9uRHJhZ0VuZCxcbiAgICAgIG9uRHJhZ0VudGVyLCBvbkRyYWdFeGl0LFxuICAgICAgb25EcmFnT3Zlciwgb25EcmFnTGVhdmUsXG4gICAgICBvbkRyb3BcbiAgICB9O1xuICB9XG5cblxuICBnZXRDbGFzc05hbWUgKCkge1xuICAgIGNvbnN0IHsga2V5LCBjbGFzc05hbWUgfSA9IHRoaXMucHJvcHMuY29sdW1uO1xuICAgIGNvbnN0IHsgaXNEcmFnZ2luZywgaXNEcmFnVGFyZ2V0IH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IG1vZGlmaWVycyA9IFsna2V5LScgKyBrZXldO1xuICAgIGlmIChpc0RyYWdnaW5nKSBtb2RpZmllcnMucHVzaCgnRHJhZ2dpbmcnKTtcbiAgICBpZiAoaXNEcmFnVGFyZ2V0KSBtb2RpZmllcnMucHVzaCgnRHJhZ1RhcmdldCcpO1xuICAgIHJldHVybiAodHlwZW9mIGNsYXNzTmFtZSA9PT0gJ3N0cmluZycgPyBjbGFzc05hbWUgKyAnICcgOiAnJykgKyBoZWFkaW5nQ2VsbENsYXNzKG51bGwsIG1vZGlmaWVycyk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgY29sdW1uLCBldmVudEhhbmRsZXJzLCBwcmltYXJ5IH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsga2V5LCBoZWFkaW5nU3R5bGUsIHdpZHRoLCByZW5kZXJIZWFkaW5nIH0gPSBjb2x1bW47XG4gICAgY29uc3Qgd2lkdGhTdHlsZSA9IHdpZHRoID8geyB3aWR0aCwgbWF4V2lkdGg6IHdpZHRoLCBtaW5XaWR0aDogd2lkdGggfSA6IHt9O1xuXG4gICAgY29uc3Qgc3R5bGUgPSBPYmplY3QuYXNzaWduKHt9LCBoZWFkaW5nU3R5bGUgPyBoZWFkaW5nU3R5bGUgOiB7fSwgd2lkdGhTdHlsZSk7XG4gICAgY29uc3QgcmVmID0gZWxlbWVudCA9PiB0aGlzLmVsZW1lbnQgPSBlbGVtZW50O1xuXG4gICAgY29uc3QgY2hpbGRyZW4gPSB0aGlzLnJlbmRlckNvbnRlbnQoKTtcbiAgICBjb25zdCBjbGFzc05hbWUgPSB0aGlzLmdldENsYXNzTmFtZSgpO1xuICAgIGNvbnN0IGRvbUV2ZW50cyA9IHRoaXMuZ2V0RG9tRXZlbnRzKCk7XG5cbiAgICBjb25zdCBkcmFnZ2FibGUgPSBwcmltYXJ5XG4gICAgICAmJiBjb2x1bW4ubW92ZWFibGVcbiAgICAgICYmICFjb2x1bW4ucHJpbWFyeVxuICAgICAgJiYgdHlwZW9mIGV2ZW50SGFuZGxlcnMub25Db2x1bW5SZW9yZGVyID09PSAnZnVuY3Rpb24nO1xuXG4gICAgY29uc3QgcHJvcHMgPSB7IHN0eWxlLCBrZXksIHJlZiwgZHJhZ2dhYmxlLCBjaGlsZHJlbiwgY2xhc3NOYW1lIH07XG5cbiAgICByZXR1cm4gY29sdW1uLmhpZGRlbiA/IG51bGwgOiA8dGggey4uLnByb3BzfSB7Li4uZG9tRXZlbnRzfSAvPlxuICB9XG59O1xuXG5IZWFkaW5nQ2VsbC5wcm9wVHlwZXMgPSB7XG4gIHNvcnQ6IFByb3BUeXBlcy5vYmplY3QsXG4gIGV2ZW50SGFuZGxlcnM6IFByb3BUeXBlcy5vYmplY3QsXG4gIGNvbHVtbjogUHJvcFR5cGVzLm9iamVjdC5pc1JlcXVpcmVkLFxuICBjb2x1bW5JbmRleDogUHJvcFR5cGVzLm51bWJlci5pc1JlcXVpcmVkXG59O1xuXG5leHBvcnQgZGVmYXVsdCBIZWFkaW5nQ2VsbDtcbiJdfQ==