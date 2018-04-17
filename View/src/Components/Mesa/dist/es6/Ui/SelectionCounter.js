'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCounter = function (_React$Component) {
  _inherits(SelectionCounter, _React$Component);

  function SelectionCounter(props) {
    _classCallCheck(this, SelectionCounter);

    var _this = _possibleConstructorReturn(this, (SelectionCounter.__proto__ || Object.getPrototypeOf(SelectionCounter)).call(this, props));

    _this.noun = _this.noun.bind(_this);
    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    return _this;
  }

  _createClass(SelectionCounter, [{
    key: 'noun',
    value: function noun(size) {
      var _props = this.props,
          selectedNoun = _props.selectedNoun,
          selectedPluralNoun = _props.selectedPluralNoun;

      size = typeof size === 'number' ? size : size.length;
      return !selectedNoun && !selectedPluralNoun ? 'row' + (size === 1 ? '' : 's') : size === 1 ? selectedNoun || 'row' : selectedPluralNoun || 'rows';
    }
  }, {
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props2 = this.props,
          rows = _props2.rows,
          isRowSelected = _props2.isRowSelected,
          onRowSelect = _props2.onRowSelect,
          onMultipleRowSelect = _props2.onMultipleRowSelect;

      var unselectedRows = rows.filter(function (row) {
        return !isRowSelected(row);
      });
      if (typeof onMultipleRowSelect === 'function') onMultipleRowSelect(unselectedRows);else unselectedRows.forEach(function (row) {
        return onRowSelect(row);
      });
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props3 = this.props,
          rows = _props3.rows,
          isRowSelected = _props3.isRowSelected,
          onRowDeselect = _props3.onRowDeselect,
          onMultipleRowDeselect = _props3.onMultipleRowDeselect;

      var selection = rows.filter(isRowSelected);
      if (typeof onMultipleRowDeselect === 'function') onMultipleRowDeselect(selection);else selection.forEach(function (row) {
        return onRowDeselect(row);
      });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          rows = _props4.rows,
          isRowSelected = _props4.isRowSelected,
          onRowDeselect = _props4.onRowDeselect,
          onMultipleRowDeselect = _props4.onMultipleRowDeselect;

      var selection = rows.filter(isRowSelected);
      if (!selection.length) return null;
      var allSelected = rows.every(function (row) {
        return selection.includes(row);
      });

      return _react2.default.createElement(
        'div',
        { className: 'SelectionCounter' },
        _react2.default.createElement(
          'b',
          null,
          selection.length,
          ' '
        ),
        this.noun(selection),
        ' selected.',
        _react2.default.createElement('br', null),
        !onRowDeselect && !onMultipleRowDeselect ? null : _react2.default.createElement(
          'a',
          { onClick: this.deselectAllRows },
          'Clear selection.'
        )
      );
    }
  }]);

  return SelectionCounter;
}(_react2.default.Component);

;

SelectionCounter.propTypes = {
  // all/total "rows" in the table
  rows: _propTypes2.default.array.isRequired,
  // predicate to test for 'selectedness'
  isRowSelected: _propTypes2.default.func.isRequired,

  // noun and plural to use for selections (e.g. "25 Datasets selected")
  selectedNoun: _propTypes2.default.string,
  selectedPluralNoun: _propTypes2.default.string,
  // single and multiple select/deselect handlers
  onRowSelect: _propTypes2.default.func,
  onRowDeselect: _propTypes2.default.func,
  onMultipleRowSelect: _propTypes2.default.func,
  onMultipleRowDeselect: _propTypes2.default.func
};

exports.default = SelectionCounter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9TZWxlY3Rpb25Db3VudGVyLmpzeCJdLCJuYW1lcyI6WyJTZWxlY3Rpb25Db3VudGVyIiwicHJvcHMiLCJub3VuIiwiYmluZCIsInNlbGVjdEFsbFJvd3MiLCJkZXNlbGVjdEFsbFJvd3MiLCJzaXplIiwic2VsZWN0ZWROb3VuIiwic2VsZWN0ZWRQbHVyYWxOb3VuIiwibGVuZ3RoIiwicm93cyIsImlzUm93U2VsZWN0ZWQiLCJvblJvd1NlbGVjdCIsIm9uTXVsdGlwbGVSb3dTZWxlY3QiLCJ1bnNlbGVjdGVkUm93cyIsImZpbHRlciIsInJvdyIsImZvckVhY2giLCJvblJvd0Rlc2VsZWN0Iiwib25NdWx0aXBsZVJvd0Rlc2VsZWN0Iiwic2VsZWN0aW9uIiwiYWxsU2VsZWN0ZWQiLCJldmVyeSIsImluY2x1ZGVzIiwiQ29tcG9uZW50IiwicHJvcFR5cGVzIiwiYXJyYXkiLCJpc1JlcXVpcmVkIiwiZnVuYyIsInN0cmluZyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFTUEsZ0I7OztBQUNKLDRCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsb0lBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLElBQUwsR0FBWSxNQUFLQSxJQUFMLENBQVVDLElBQVYsT0FBWjtBQUNBLFVBQUtDLGFBQUwsR0FBcUIsTUFBS0EsYUFBTCxDQUFtQkQsSUFBbkIsT0FBckI7QUFDQSxVQUFLRSxlQUFMLEdBQXVCLE1BQUtBLGVBQUwsQ0FBcUJGLElBQXJCLE9BQXZCO0FBSmtCO0FBS25COzs7O3lCQUVLRyxJLEVBQU07QUFBQSxtQkFDbUMsS0FBS0wsS0FEeEM7QUFBQSxVQUNGTSxZQURFLFVBQ0ZBLFlBREU7QUFBQSxVQUNZQyxrQkFEWixVQUNZQSxrQkFEWjs7QUFFVkYsYUFBUSxPQUFPQSxJQUFQLEtBQWdCLFFBQWhCLEdBQTJCQSxJQUEzQixHQUFrQ0EsS0FBS0csTUFBL0M7QUFDQSxhQUFRLENBQUNGLFlBQUQsSUFBaUIsQ0FBQ0Msa0JBQW5CLEdBQ0gsU0FBU0YsU0FBUyxDQUFULEdBQWEsRUFBYixHQUFrQixHQUEzQixDQURHLEdBRUhBLFNBQVMsQ0FBVCxHQUNHQyxnQkFBZ0IsS0FEbkIsR0FFR0Msc0JBQXNCLE1BSjdCO0FBS0Q7OztvQ0FFZ0I7QUFBQSxvQkFDbUQsS0FBS1AsS0FEeEQ7QUFBQSxVQUNQUyxJQURPLFdBQ1BBLElBRE87QUFBQSxVQUNEQyxhQURDLFdBQ0RBLGFBREM7QUFBQSxVQUNjQyxXQURkLFdBQ2NBLFdBRGQ7QUFBQSxVQUMyQkMsbUJBRDNCLFdBQzJCQSxtQkFEM0I7O0FBRWYsVUFBTUMsaUJBQWlCSixLQUFLSyxNQUFMLENBQVk7QUFBQSxlQUFPLENBQUNKLGNBQWNLLEdBQWQsQ0FBUjtBQUFBLE9BQVosQ0FBdkI7QUFDQSxVQUFJLE9BQU9ILG1CQUFQLEtBQStCLFVBQW5DLEVBQStDQSxvQkFBb0JDLGNBQXBCLEVBQS9DLEtBQ0tBLGVBQWVHLE9BQWYsQ0FBdUI7QUFBQSxlQUFPTCxZQUFZSSxHQUFaLENBQVA7QUFBQSxPQUF2QjtBQUNOOzs7c0NBRWtCO0FBQUEsb0JBQ3FELEtBQUtmLEtBRDFEO0FBQUEsVUFDVFMsSUFEUyxXQUNUQSxJQURTO0FBQUEsVUFDSEMsYUFERyxXQUNIQSxhQURHO0FBQUEsVUFDWU8sYUFEWixXQUNZQSxhQURaO0FBQUEsVUFDMkJDLHFCQUQzQixXQUMyQkEscUJBRDNCOztBQUVqQixVQUFNQyxZQUFZVixLQUFLSyxNQUFMLENBQVlKLGFBQVosQ0FBbEI7QUFDQSxVQUFJLE9BQU9RLHFCQUFQLEtBQWlDLFVBQXJDLEVBQWlEQSxzQkFBc0JDLFNBQXRCLEVBQWpELEtBQ0tBLFVBQVVILE9BQVYsQ0FBa0I7QUFBQSxlQUFPQyxjQUFjRixHQUFkLENBQVA7QUFBQSxPQUFsQjtBQUNOOzs7NkJBRVM7QUFBQSxvQkFDOEQsS0FBS2YsS0FEbkU7QUFBQSxVQUNBUyxJQURBLFdBQ0FBLElBREE7QUFBQSxVQUNNQyxhQUROLFdBQ01BLGFBRE47QUFBQSxVQUNxQk8sYUFEckIsV0FDcUJBLGFBRHJCO0FBQUEsVUFDb0NDLHFCQURwQyxXQUNvQ0EscUJBRHBDOztBQUVSLFVBQU1DLFlBQVlWLEtBQUtLLE1BQUwsQ0FBWUosYUFBWixDQUFsQjtBQUNBLFVBQUksQ0FBQ1MsVUFBVVgsTUFBZixFQUF1QixPQUFPLElBQVA7QUFDdkIsVUFBTVksY0FBY1gsS0FBS1ksS0FBTCxDQUFXO0FBQUEsZUFBT0YsVUFBVUcsUUFBVixDQUFtQlAsR0FBbkIsQ0FBUDtBQUFBLE9BQVgsQ0FBcEI7O0FBRUEsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFVLGtCQUFmO0FBQ0U7QUFBQTtBQUFBO0FBQUlJLG9CQUFVWCxNQUFkO0FBQUE7QUFBQSxTQURGO0FBRUcsYUFBS1AsSUFBTCxDQUFVa0IsU0FBVixDQUZIO0FBQUE7QUFHRSxpREFIRjtBQUlHLFNBQUNGLGFBQUQsSUFBa0IsQ0FBQ0MscUJBQW5CLEdBQTJDLElBQTNDLEdBQW1EO0FBQUE7QUFBQSxZQUFHLFNBQVMsS0FBS2QsZUFBakI7QUFBQTtBQUFBO0FBSnRELE9BREY7QUFRRDs7OztFQTlDNEIsZ0JBQU1tQixTOztBQStDcEM7O0FBRUR4QixpQkFBaUJ5QixTQUFqQixHQUE2QjtBQUMzQjtBQUNBZixRQUFNLG9CQUFVZ0IsS0FBVixDQUFnQkMsVUFGSztBQUczQjtBQUNBaEIsaUJBQWUsb0JBQVVpQixJQUFWLENBQWVELFVBSkg7O0FBTTNCO0FBQ0FwQixnQkFBYyxvQkFBVXNCLE1BUEc7QUFRM0JyQixzQkFBb0Isb0JBQVVxQixNQVJIO0FBUzNCO0FBQ0FqQixlQUFhLG9CQUFVZ0IsSUFWSTtBQVczQlYsaUJBQWUsb0JBQVVVLElBWEU7QUFZM0JmLHVCQUFxQixvQkFBVWUsSUFaSjtBQWEzQlQseUJBQXVCLG9CQUFVUztBQWJOLENBQTdCOztrQkFnQmU1QixnQiIsImZpbGUiOiJTZWxlY3Rpb25Db3VudGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmNsYXNzIFNlbGVjdGlvbkNvdW50ZXIgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5ub3VuID0gdGhpcy5ub3VuLmJpbmQodGhpcyk7XG4gICAgdGhpcy5zZWxlY3RBbGxSb3dzID0gdGhpcy5zZWxlY3RBbGxSb3dzLmJpbmQodGhpcyk7XG4gICAgdGhpcy5kZXNlbGVjdEFsbFJvd3MgPSB0aGlzLmRlc2VsZWN0QWxsUm93cy5iaW5kKHRoaXMpO1xuICB9XG5cbiAgbm91biAoc2l6ZSkge1xuICAgIGNvbnN0IHsgc2VsZWN0ZWROb3VuLCBzZWxlY3RlZFBsdXJhbE5vdW4gfSA9IHRoaXMucHJvcHM7XG4gICAgc2l6ZSA9ICh0eXBlb2Ygc2l6ZSA9PT0gJ251bWJlcicgPyBzaXplIDogc2l6ZS5sZW5ndGgpO1xuICAgIHJldHVybiAoIXNlbGVjdGVkTm91biAmJiAhc2VsZWN0ZWRQbHVyYWxOb3VuKVxuICAgICAgPyAncm93JyArIChzaXplID09PSAxID8gJycgOiAncycpXG4gICAgICA6IHNpemUgPT09IDFcbiAgICAgICAgPyAoc2VsZWN0ZWROb3VuIHx8ICdyb3cnKVxuICAgICAgICA6IChzZWxlY3RlZFBsdXJhbE5vdW4gfHwgJ3Jvd3MnKTtcbiAgfVxuXG4gIHNlbGVjdEFsbFJvd3MgKCkge1xuICAgIGNvbnN0IHsgcm93cywgaXNSb3dTZWxlY3RlZCwgb25Sb3dTZWxlY3QsIG9uTXVsdGlwbGVSb3dTZWxlY3QgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgdW5zZWxlY3RlZFJvd3MgPSByb3dzLmZpbHRlcihyb3cgPT4gIWlzUm93U2VsZWN0ZWQocm93KSk7XG4gICAgaWYgKHR5cGVvZiBvbk11bHRpcGxlUm93U2VsZWN0ID09PSAnZnVuY3Rpb24nKSBvbk11bHRpcGxlUm93U2VsZWN0KHVuc2VsZWN0ZWRSb3dzKTtcbiAgICBlbHNlIHVuc2VsZWN0ZWRSb3dzLmZvckVhY2gocm93ID0+IG9uUm93U2VsZWN0KHJvdykpO1xuICB9XG5cbiAgZGVzZWxlY3RBbGxSb3dzICgpIHtcbiAgICBjb25zdCB7IHJvd3MsIGlzUm93U2VsZWN0ZWQsIG9uUm93RGVzZWxlY3QsIG9uTXVsdGlwbGVSb3dEZXNlbGVjdCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBzZWxlY3Rpb24gPSByb3dzLmZpbHRlcihpc1Jvd1NlbGVjdGVkKTtcbiAgICBpZiAodHlwZW9mIG9uTXVsdGlwbGVSb3dEZXNlbGVjdCA9PT0gJ2Z1bmN0aW9uJykgb25NdWx0aXBsZVJvd0Rlc2VsZWN0KHNlbGVjdGlvbilcbiAgICBlbHNlIHNlbGVjdGlvbi5mb3JFYWNoKHJvdyA9PiBvblJvd0Rlc2VsZWN0KHJvdykpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IHJvd3MsIGlzUm93U2VsZWN0ZWQsIG9uUm93RGVzZWxlY3QsIG9uTXVsdGlwbGVSb3dEZXNlbGVjdCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBzZWxlY3Rpb24gPSByb3dzLmZpbHRlcihpc1Jvd1NlbGVjdGVkKTtcbiAgICBpZiAoIXNlbGVjdGlvbi5sZW5ndGgpIHJldHVybiBudWxsO1xuICAgIGNvbnN0IGFsbFNlbGVjdGVkID0gcm93cy5ldmVyeShyb3cgPT4gc2VsZWN0aW9uLmluY2x1ZGVzKHJvdykpO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiU2VsZWN0aW9uQ291bnRlclwiPlxuICAgICAgICA8Yj57c2VsZWN0aW9uLmxlbmd0aH0gPC9iPlxuICAgICAgICB7dGhpcy5ub3VuKHNlbGVjdGlvbil9IHNlbGVjdGVkLlxuICAgICAgICA8YnIgLz5cbiAgICAgICAgeyFvblJvd0Rlc2VsZWN0ICYmICFvbk11bHRpcGxlUm93RGVzZWxlY3QgPyBudWxsIDogKDxhIG9uQ2xpY2s9e3RoaXMuZGVzZWxlY3RBbGxSb3dzfT5DbGVhciBzZWxlY3Rpb24uPC9hPil9XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5TZWxlY3Rpb25Db3VudGVyLnByb3BUeXBlcyA9IHtcbiAgLy8gYWxsL3RvdGFsIFwicm93c1wiIGluIHRoZSB0YWJsZVxuICByb3dzOiBQcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZCxcbiAgLy8gcHJlZGljYXRlIHRvIHRlc3QgZm9yICdzZWxlY3RlZG5lc3MnXG4gIGlzUm93U2VsZWN0ZWQ6IFByb3BUeXBlcy5mdW5jLmlzUmVxdWlyZWQsXG5cbiAgLy8gbm91biBhbmQgcGx1cmFsIHRvIHVzZSBmb3Igc2VsZWN0aW9ucyAoZS5nLiBcIjI1IERhdGFzZXRzIHNlbGVjdGVkXCIpXG4gIHNlbGVjdGVkTm91bjogUHJvcFR5cGVzLnN0cmluZyxcbiAgc2VsZWN0ZWRQbHVyYWxOb3VuOiBQcm9wVHlwZXMuc3RyaW5nLFxuICAvLyBzaW5nbGUgYW5kIG11bHRpcGxlIHNlbGVjdC9kZXNlbGVjdCBoYW5kbGVyc1xuICBvblJvd1NlbGVjdDogUHJvcFR5cGVzLmZ1bmMsXG4gIG9uUm93RGVzZWxlY3Q6IFByb3BUeXBlcy5mdW5jLFxuICBvbk11bHRpcGxlUm93U2VsZWN0OiBQcm9wVHlwZXMuZnVuYyxcbiAgb25NdWx0aXBsZVJvd0Rlc2VsZWN0OiBQcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgU2VsZWN0aW9uQ291bnRlcjtcbiJdfQ==