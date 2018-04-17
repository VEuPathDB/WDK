'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Modal = require('../Components/Modal');

var _Modal2 = _interopRequireDefault(_Modal);

var _Checkbox = require('../Components/Checkbox');

var _Checkbox2 = _interopRequireDefault(_Checkbox);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var columnEditorClass = (0, _Utils.makeClassifier)('ColumnEditor');

var ColumnEditor = function (_React$PureComponent) {
  _inherits(ColumnEditor, _React$PureComponent);

  function ColumnEditor(props) {
    _classCallCheck(this, ColumnEditor);

    var _this = _possibleConstructorReturn(this, (ColumnEditor.__proto__ || Object.getPrototypeOf(ColumnEditor)).call(this, props));

    _this.state = {
      editorOpen: false
    };

    _this.openEditor = _this.openEditor.bind(_this);
    _this.closeEditor = _this.closeEditor.bind(_this);
    _this.toggleEditor = _this.toggleEditor.bind(_this);

    _this.renderModal = _this.renderModal.bind(_this);
    _this.renderTrigger = _this.renderTrigger.bind(_this);
    _this.renderColumnListItem = _this.renderColumnListItem.bind(_this);

    _this.showColumn = _this.showColumn.bind(_this);
    _this.hideColumn = _this.hideColumn.bind(_this);
    _this.showAllColumns = _this.showAllColumns.bind(_this);
    _this.hideAllColumns = _this.hideAllColumns.bind(_this);
    return _this;
  }

  _createClass(ColumnEditor, [{
    key: 'openEditor',
    value: function openEditor() {
      this.setState({ editorOpen: true });
      if (!this.closeListener) this.closeListener = _Events2.default.onKey('esc', this.closeEditor);
    }
  }, {
    key: 'closeEditor',
    value: function closeEditor() {
      this.setState({ editorOpen: false });
      if (this.closeListener) _Events2.default.remove(this.closeListener);
    }
  }, {
    key: 'toggleEditor',
    value: function toggleEditor() {
      var editorOpen = this.state.editorOpen;

      return editorOpen ? this.closeEditor() : this.openEditor();
    }
  }, {
    key: 'showColumn',
    value: function showColumn(column) {
      var eventHandlers = this.props.eventHandlers;
      var onShowColumn = eventHandlers.onShowColumn;

      if (onShowColumn) onShowColumn(column);
    }
  }, {
    key: 'hideColumn',
    value: function hideColumn(column) {
      var eventHandlers = this.props.eventHandlers;
      var onHideColumn = eventHandlers.onHideColumn;

      if (onHideColumn) onHideColumn(column);
    }
  }, {
    key: 'showAllColumns',
    value: function showAllColumns() {
      var _this2 = this;

      var _props = this.props,
          columns = _props.columns,
          eventHandlers = _props.eventHandlers;

      var hiddenColumns = columns.filter(function (col) {
        return col.hidden;
      });
      hiddenColumns.forEach(function (column) {
        return _this2.showColumn(column);
      });
    }
  }, {
    key: 'hideAllColumns',
    value: function hideAllColumns() {
      var _this3 = this;

      var _props2 = this.props,
          columns = _props2.columns,
          eventHandlers = _props2.eventHandlers;

      var shownColumns = columns.filter(function (col) {
        return !col.hidden;
      });
      shownColumns.forEach(function (column) {
        return _this3.hideColumn(column);
      });
    }
  }, {
    key: 'renderTrigger',
    value: function renderTrigger() {
      var children = this.props.children;

      return _react2.default.createElement(
        'div',
        { className: columnEditorClass('Trigger'), onClick: this.toggleEditor },
        children
      );
    }
  }, {
    key: 'renderColumnListItem',
    value: function renderColumnListItem(column) {
      var _this4 = this;

      return _react2.default.createElement(
        'li',
        { className: columnEditorClass('ListItem'), key: column.key },
        _react2.default.createElement(_Checkbox2.default, {
          checked: !column.hidden,
          disabled: !column.hideable,
          onChange: function onChange() {
            return (column.hidden ? _this4.showColumn : _this4.hideColumn)(column);
          }
        }),
        ' ' + (column.name || column.key)
      );
    }
  }, {
    key: 'renderModal',
    value: function renderModal() {
      var editorOpen = this.state.editorOpen;

      return _react2.default.createElement(
        _Modal2.default,
        { open: editorOpen, onClose: this.closeEditor },
        _react2.default.createElement(
          'h3',
          null,
          'Add / Remove Columns'
        ),
        _react2.default.createElement(
          'small',
          null,
          _react2.default.createElement(
            'a',
            { onClick: this.showAllColumns },
            'Select All'
          ),
          _react2.default.createElement(
            'span',
            null,
            ' | '
          ),
          _react2.default.createElement(
            'a',
            { onClick: this.hideAllColumns },
            'Clear All'
          )
        ),
        _react2.default.createElement(
          'ul',
          { className: columnEditorClass('List') },
          columns.map(this.renderColumnListItem)
        ),
        _react2.default.createElement(
          'button',
          { onClick: this.closeEditor, style: { margin: '0 auto', display: 'block' } },
          'Close'
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var modal = this.renderModal();
      var trigger = this.renderTrigger();

      return _react2.default.createElement(
        'div',
        { className: columnEditorClass() },
        trigger,
        modal
      );
    }
  }]);

  return ColumnEditor;
}(_react2.default.PureComponent);

exports.default = ColumnEditor;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9Db2x1bW5FZGl0b3IuanN4Il0sIm5hbWVzIjpbImNvbHVtbkVkaXRvckNsYXNzIiwiQ29sdW1uRWRpdG9yIiwicHJvcHMiLCJzdGF0ZSIsImVkaXRvck9wZW4iLCJvcGVuRWRpdG9yIiwiYmluZCIsImNsb3NlRWRpdG9yIiwidG9nZ2xlRWRpdG9yIiwicmVuZGVyTW9kYWwiLCJyZW5kZXJUcmlnZ2VyIiwicmVuZGVyQ29sdW1uTGlzdEl0ZW0iLCJzaG93Q29sdW1uIiwiaGlkZUNvbHVtbiIsInNob3dBbGxDb2x1bW5zIiwiaGlkZUFsbENvbHVtbnMiLCJzZXRTdGF0ZSIsImNsb3NlTGlzdGVuZXIiLCJvbktleSIsInJlbW92ZSIsImNvbHVtbiIsImV2ZW50SGFuZGxlcnMiLCJvblNob3dDb2x1bW4iLCJvbkhpZGVDb2x1bW4iLCJjb2x1bW5zIiwiaGlkZGVuQ29sdW1ucyIsImZpbHRlciIsImNvbCIsImhpZGRlbiIsImZvckVhY2giLCJzaG93bkNvbHVtbnMiLCJjaGlsZHJlbiIsImtleSIsImhpZGVhYmxlIiwibmFtZSIsIm1hcCIsIm1hcmdpbiIsImRpc3BsYXkiLCJtb2RhbCIsInRyaWdnZXIiLCJQdXJlQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7Ozs7OztBQUVBLElBQU1BLG9CQUFvQiwyQkFBZSxjQUFmLENBQTFCOztJQUVNQyxZOzs7QUFDSix3QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDRIQUNaQSxLQURZOztBQUdsQixVQUFLQyxLQUFMLEdBQWE7QUFDWEMsa0JBQVk7QUFERCxLQUFiOztBQUlBLFVBQUtDLFVBQUwsR0FBa0IsTUFBS0EsVUFBTCxDQUFnQkMsSUFBaEIsT0FBbEI7QUFDQSxVQUFLQyxXQUFMLEdBQW1CLE1BQUtBLFdBQUwsQ0FBaUJELElBQWpCLE9BQW5CO0FBQ0EsVUFBS0UsWUFBTCxHQUFv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TUQ7Ozs7RUFqSHdCLGdCQUFNRSxhOztrQkFvSGxCdkMsWSIsImZpbGUiOiJDb2x1bW5FZGl0b3IuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgRXZlbnRzIGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuaW1wb3J0IE1vZGFsIGZyb20gJy4uL0NvbXBvbmVudHMvTW9kYWwnO1xuaW1wb3J0IENoZWNrYm94IGZyb20gJy4uL0NvbXBvbmVudHMvQ2hlY2tib3gnO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IGNvbHVtbkVkaXRvckNsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ0NvbHVtbkVkaXRvcicpO1xuXG5jbGFzcyBDb2x1bW5FZGl0b3IgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGVkaXRvck9wZW46IGZhbHNlXG4gICAgfTtcblxuICAgIHRoaXMub3BlbkVkaXRvciA9IHRoaXMub3BlbkVkaXRvci5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY2xvc2VFZGl0b3IgPSB0aGlzLmNsb3NlRWRpdG9yLmJpbmQodGhpcyk7XG4gICAgdGhpcy50b2dnbGVFZGl0b3IgPSB0aGlzLnRvZ2dsZUVkaXRvci5iaW5kKHRoaXMpO1xuXG4gICAgdGhpcy5yZW5kZXJNb2RhbCA9IHRoaXMucmVuZGVyTW9kYWwuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlclRyaWdnZXIgPSB0aGlzLnJlbmRlclRyaWdnZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlckNvbHVtbkxpc3RJdGVtID0gdGhpcy5yZW5kZXJDb2x1bW5MaXN0SXRlbS5iaW5kKHRoaXMpO1xuXG4gICAgdGhpcy5zaG93Q29sdW1uID0gdGhpcy5zaG93Q29sdW1uLmJpbmQodGhpcyk7XG4gICAgdGhpcy5oaWRlQ29sdW1uID0gdGhpcy5oaWRlQ29sdW1uLmJpbmQodGhpcyk7XG4gICAgdGhpcy5zaG93QWxsQ29sdW1ucyA9IHRoaXMuc2hvd0FsbENvbHVtbnMuYmluZCh0aGlzKTtcbiAgICB0aGlzLmhpZGVBbGxDb2x1bW5zID0gdGhpcy5oaWRlQWxsQ29sdW1ucy5iaW5kKHRoaXMpO1xuICB9XG5cbiAgb3BlbkVkaXRvciAoKSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IGVkaXRvck9wZW46IHRydWUgfSlcbiAgICBpZiAoIXRoaXMuY2xvc2VMaXN0ZW5lcikgdGhpcy5jbG9zZUxpc3RlbmVyID0gRXZlbnRzLm9uS2V5KCdlc2MnLCB0aGlzLmNsb3NlRWRpdG9yKTtcbiAgfVxuXG4gIGNsb3NlRWRpdG9yICgpIHtcbiAgICB0aGlzLnNldFN0YXRlKHsgZWRpdG9yT3BlbjogZmFsc2UgfSlcbiAgICBpZiAodGhpcy5jbG9zZUxpc3RlbmVyKSBFdmVudHMucmVtb3ZlKHRoaXMuY2xvc2VMaXN0ZW5lcik7XG4gIH1cblxuICB0b2dnbGVFZGl0b3IgKCkge1xuICAgIGxldCB7IGVkaXRvck9wZW4gfSA9IHRoaXMuc3RhdGU7XG4gICAgcmV0dXJuIGVkaXRvck9wZW4gPyB0aGlzLmNsb3NlRWRpdG9yKCkgOiB0aGlzLm9wZW5FZGl0b3IoKTtcbiAgfVxuXG4gIHNob3dDb2x1bW4gKGNvbHVtbikge1xuICAgIGNvbnN0IHsgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IG9uU2hvd0NvbHVtbiB9ID0gZXZlbnRIYW5kbGVycztcbiAgICBpZiAob25TaG93Q29sdW1uKSBvblNob3dDb2x1bW4oY29sdW1uKTtcbiAgfVxuXG4gIGhpZGVDb2x1bW4gKGNvbHVtbikge1xuICAgIGNvbnN0IHsgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IG9uSGlkZUNvbHVtbiB9ID0gZXZlbnRIYW5kbGVycztcbiAgICBpZiAob25IaWRlQ29sdW1uKSBvbkhpZGVDb2x1bW4oY29sdW1uKTtcbiAgfVxuXG4gIHNob3dBbGxDb2x1bW5zICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbnMsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgaGlkZGVuQ29sdW1ucyA9IGNvbHVtbnMuZmlsdGVyKGNvbCA9PiBjb2wuaGlkZGVuKTtcbiAgICBoaWRkZW5Db2x1bW5zLmZvckVhY2goY29sdW1uID0+IHRoaXMuc2hvd0NvbHVtbihjb2x1bW4pKTtcbiAgfVxuXG4gIGhpZGVBbGxDb2x1bW5zICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbnMsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3Qgc2hvd25Db2x1bW5zID0gY29sdW1ucy5maWx0ZXIoY29sID0+ICFjb2wuaGlkZGVuKTtcbiAgICBzaG93bkNvbHVtbnMuZm9yRWFjaChjb2x1bW4gPT4gdGhpcy5oaWRlQ29sdW1uKGNvbHVtbikpO1xuICB9XG5cbiAgcmVuZGVyVHJpZ2dlciAoKSB7XG4gICAgY29uc3QgeyBjaGlsZHJlbiB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9e2NvbHVtbkVkaXRvckNsYXNzKCdUcmlnZ2VyJyl9IG9uQ2xpY2s9e3RoaXMudG9nZ2xlRWRpdG9yfT5cbiAgICAgICAge2NoaWxkcmVufVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG5cbiAgcmVuZGVyQ29sdW1uTGlzdEl0ZW0gKGNvbHVtbikge1xuICAgIHJldHVybiAoXG4gICAgICA8bGkgY2xhc3NOYW1lPXtjb2x1bW5FZGl0b3JDbGFzcygnTGlzdEl0ZW0nKX0ga2V5PXtjb2x1bW4ua2V5fT5cbiAgICAgICAgPENoZWNrYm94XG4gICAgICAgICAgY2hlY2tlZD17IWNvbHVtbi5oaWRkZW59XG4gICAgICAgICAgZGlzYWJsZWQ9eyFjb2x1bW4uaGlkZWFibGV9XG4gICAgICAgICAgb25DaGFuZ2U9eygpID0+ICgoY29sdW1uLmhpZGRlbiA/IHRoaXMuc2hvd0NvbHVtbiA6IHRoaXMuaGlkZUNvbHVtbikoY29sdW1uKSl9XG4gICAgICAgIC8+XG4gICAgICAgIHsnICcgKyAoY29sdW1uLm5hbWUgfHwgY29sdW1uLmtleSl9XG4gICAgICA8L2xpPlxuICAgICk7XG4gIH1cblxuICByZW5kZXJNb2RhbCAoKSB7XG4gICAgY29uc3QgeyBlZGl0b3JPcGVuIH0gPSB0aGlzLnN0YXRlO1xuICAgIHJldHVybiAoXG4gICAgICA8TW9kYWwgb3Blbj17ZWRpdG9yT3Blbn0gb25DbG9zZT17dGhpcy5jbG9zZUVkaXRvcn0+XG4gICAgICAgIDxoMz5BZGQgLyBSZW1vdmUgQ29sdW1uczwvaDM+XG4gICAgICAgIDxzbWFsbD5cbiAgICAgICAgICA8YSBvbkNsaWNrPXt0aGlzLnNob3dBbGxDb2x1bW5zfT5TZWxlY3QgQWxsPC9hPlxuICAgICAgICAgIDxzcGFuPiB8IDwvc3Bhbj5cbiAgICAgICAgICA8YSBvbkNsaWNrPXt0aGlzLmhpZGVBbGxDb2x1bW5zfT5DbGVhciBBbGw8L2E+XG4gICAgICAgIDwvc21hbGw+XG4gICAgICAgIDx1bCBjbGFzc05hbWU9e2NvbHVtbkVkaXRvckNsYXNzKCdMaXN0Jyl9PlxuICAgICAgICAgIHtjb2x1bW5zLm1hcCh0aGlzLnJlbmRlckNvbHVtbkxpc3RJdGVtKX1cbiAgICAgICAgPC91bD5cbiAgICAgICAgPGJ1dHRvbiBvbkNsaWNrPXt0aGlzLmNsb3NlRWRpdG9yfSBzdHlsZT17eyBtYXJnaW46ICcwIGF1dG8nLCBkaXNwbGF5OiAnYmxvY2snIH19PlxuICAgICAgICAgIENsb3NlXG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9Nb2RhbD5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCBtb2RhbCA9IHRoaXMucmVuZGVyTW9kYWwoKTtcbiAgICBjb25zdCB0cmlnZ2VyID0gdGhpcy5yZW5kZXJUcmlnZ2VyKCk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9e2NvbHVtbkVkaXRvckNsYXNzKCl9PlxuICAgICAgICB7dHJpZ2dlcn1cbiAgICAgICAge21vZGFsfVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59XG5cbmV4cG9ydCBkZWZhdWx0IENvbHVtbkVkaXRvcjtcbiJdfQ==