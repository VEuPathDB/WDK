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

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ColumnEditor = function (_React$PureComponent) {
  _inherits(ColumnEditor, _React$PureComponent);

  function ColumnEditor(props) {
    _classCallCheck(this, ColumnEditor);

    var _this = _possibleConstructorReturn(this, (ColumnEditor.__proto__ || Object.getPrototypeOf(ColumnEditor)).call(this, props));

    _this.openEditor = _this.openEditor.bind(_this);
    _this.closeEditor = _this.closeEditor.bind(_this);
    _this.renderModal = _this.renderModal.bind(_this);
    _this.toggleEditor = _this.toggleEditor.bind(_this);
    _this.renderTrigger = _this.renderTrigger.bind(_this);
    _this.showAllColumns = _this.showAllColumns.bind(_this);
    _this.hideAllColumns = _this.hideAllColumns.bind(_this);
    _this.renderColumnListItem = _this.renderColumnListItem.bind(_this);
    return _this;
  }

  _createClass(ColumnEditor, [{
    key: 'openEditor',
    value: function openEditor() {
      var dispatch = this.props.dispatch;

      dispatch((0, _Actions.openColumnEditor)());
      if (!this.closeListener) this.closeListener = _Events2.default.onKey('esc', this.closeEditor);
    }
  }, {
    key: 'showAllColumns',
    value: function showAllColumns() {
      var _props = this.props,
          state = _props.state,
          dispatch = _props.dispatch;
      var columns = state.columns;

      return columns.forEach(function (col) {
        return dispatch((0, _Actions.showColumn)(col));
      });
    }
  }, {
    key: 'hideAllColumns',
    value: function hideAllColumns() {
      var _props2 = this.props,
          state = _props2.state,
          dispatch = _props2.dispatch;
      var columns = state.columns;

      var hideableColumns = columns.filter(function (col) {
        return col.hideable && !col.hidden;
      });
      return hideableColumns.forEach(function (col) {
        return dispatch((0, _Actions.hideColumn)(col));
      });
    }
  }, {
    key: 'closeEditor',
    value: function closeEditor() {
      var dispatch = this.props.dispatch;

      dispatch((0, _Actions.closeColumnEditor)());
      if (this.closeListener) _Events2.default.remove(this.closeListener);
    }
  }, {
    key: 'toggleEditor',
    value: function toggleEditor() {
      var dispatch = this.props.dispatch;

      dispatch((0, _Actions.toggleColumnEditor)());
    }
  }, {
    key: 'renderTrigger',
    value: function renderTrigger() {
      var children = this.props.children;

      return _react2.default.createElement(
        'div',
        { className: 'ColumnEditor-Trigger', onClick: this.toggleEditor },
        children
      );
    }
  }, {
    key: 'renderColumnListItem',
    value: function renderColumnListItem(column) {
      var dispatch = this.props.dispatch;

      var toggler = function toggler() {
        return dispatch(column.hidden ? (0, _Actions.showColumn)(column) : (0, _Actions.hideColumn)(column));
      };
      return _react2.default.createElement(
        'li',
        { className: 'ColumnEditor-List-Item', key: column.key },
        _react2.default.createElement(_Checkbox2.default, {
          checked: !column.hidden,
          disabled: !column.hideable,
          onChange: toggler
        }),
        ' ' + (column.name || column.key)
      );
    }
  }, {
    key: 'renderModal',
    value: function renderModal() {
      var state = this.props.state;
      var columns = state.columns,
          uiState = state.uiState;
      var columnEditorOpen = uiState.columnEditorOpen;


      return _react2.default.createElement(
        _Modal2.default,
        { open: columnEditorOpen, onClose: this.closeEditor },
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
          { className: 'ColumnEditor-List' },
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
        { className: 'ColumnEditor' },
        trigger,
        modal
      );
    }
  }]);

  return ColumnEditor;
}(_react2.default.PureComponent);

exports.default = ColumnEditor;