'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TruncatedText = function (_React$Component) {
  _inherits(TruncatedText, _React$Component);

  function TruncatedText(props) {
    _classCallCheck(this, TruncatedText);

    var _this = _possibleConstructorReturn(this, (TruncatedText.__proto__ || Object.getPrototypeOf(TruncatedText)).call(this, props));

    _this.state = { expanded: false };
    _this.toggleExpansion = _this.toggleExpansion.bind(_this);
    return _this;
  }

  _createClass(TruncatedText, [{
    key: 'wordCount',
    value: function wordCount(text) {
      if (typeof text !== 'string') return undefined;
      return text.trim().split(' ').filter(function (x) {
        return x.length;
      }).length;
    }
  }, {
    key: 'reverseText',
    value: function reverseText(text) {
      if (typeof text !== 'string' || !text.length) return text;
      return text.split('').reverse().join('');
    }
  }, {
    key: 'trimInitialPunctuation',
    value: function trimInitialPunctuation(text) {
      if (typeof text !== 'string' || !text.length) return text;
      while (text.search(/[a-zA-Z0-9]/) !== 0) {
        text = text.substring(1);
      };
      return text;
    }
  }, {
    key: 'trimPunctuation',
    value: function trimPunctuation(text) {
      if (typeof text !== 'string' || !text.length) return text;

      text = this.trimInitialPunctuation(text);
      text = this.reverseText(text);
      text = this.trimInitialPunctuation(text);
      text = this.reverseText(text);

      return text;
    }
  }, {
    key: 'truncate',
    value: function truncate(text, cutoff) {
      if (typeof text !== 'string' || typeof cutoff !== 'number') return text;
      var count = this.wordCount(text);
      if (count < cutoff) return text;

      var words = text.trim().split(' ').filter(function (x) {
        return x.length;
      });
      var threshold = Math.ceil(cutoff * 0.66);
      var short = words.slice(0, threshold).join(' ');

      return this.trimPunctuation(short) + '...';
    }
  }, {
    key: 'toggleExpansion',
    value: function toggleExpansion() {
      var expanded = this.state.expanded;

      this.setState({ expanded: !expanded });
    }
  }, {
    key: 'render',
    value: function render() {
      var expanded = this.state.expanded;
      var _props = this.props,
          className = _props.className,
          cutoff = _props.cutoff,
          text = _props.text;

      cutoff = typeof cutoff === 'number' ? cutoff : 100;
      var expandable = this.wordCount(text) > cutoff;

      className = 'TruncatedText' + (className ? ' ' + className : '');
      text = expanded ? text : this.truncate(text, cutoff);

      return _react2.default.createElement(
        'div',
        { className: className },
        text,
        expandable && _react2.default.createElement(
          'button',
          { className: 'TruncatedText-Toggle', onClick: this.toggleExpansion },
          expanded ? 'Show Less' : 'Show More',
          _react2.default.createElement(_Icon2.default, { fa: expanded ? 'angle-double-up' : 'angle-double-down' })
        )
      );
    }
  }]);

  return TruncatedText;
}(_react2.default.Component);

;

exports.default = TruncatedText;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1RydW5jYXRlZFRleHQuanN4Il0sIm5hbWVzIjpbIlRydW5jYXRlZFRleHQiLCJwcm9wcyIsInN0YXRlIiwiZXhwYW5kZWQiLCJ0b2dnbGVFeHBhbnNpb24iLCJiaW5kIiwidGV4dCIsInVuZGVmaW5lZCIsInRyaW0iLCJzcGxpdCIsImZpbHRlciIsIngiLCJsZW5ndGgiLCJyZXZlcnNlIiwiam9pbiIsInNlYXJjaCIsInN1YnN0cmluZyIsInRyaW1Jbml0aWFsUHVuY3R1YXRpb24iLCJyZXZlcnNlVGV4dCIsImN1dG9mZiIsImNvdW50Iiwid29yZENvdW50Iiwid29yZHMiLCJ0aHJlc2hvbGQiLCJNYXRoIiwiY2VpbCIsInNob3J0Iiwic2xpY2UiLCJ0cmltUHVuY3R1YXRpb24iLCJzZXRTdGF0ZSIsImNsYXNzTmFtZSIsImV4cGFuZGFibGUiLCJ0cnVuY2F0ZSIsIkNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7Ozs7Ozs7Ozs7SUFFTUEsYTs7O0FBQ0oseUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSw4SEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFVBQVUsS0FBWixFQUFiO0FBQ0EsVUFBS0MsZUFBTCxHQUF1QixNQUFLQSxlQUFMLENBQXFCQyxJQUFyQixPQUF2QjtBQUhrQjtBQUluQjs7Ozs4QkFFVUMsSSxFQUFNO0FBQ2YsVUFBSSxPQUFPQSxJQUFQLEtBQWdCLFFBQXBCLEVBQThCLE9BQU9DLFNBQVA7QUFDOUIsYUFBT0QsS0FBS0UsSUFBTCxHQUFZQyxLQUFaLENBQWtCLEdBQWxCLEVBQXVCQyxNQUF2QixDQUE4QjtBQUFBLGVBQUtDLEVBQUVDLE1BQVA7QUFBQSxPQUE5QixFQUE2Q0EsTUFBcEQ7QUFDRDs7O2dDQUVZTixJLEVBQU07QUFDakIsVUFBSSxPQUFPQSxJQUFQLEtBQWdCLFFBQWhCLElBQTRCLENBQUNBLEtBQUtNLE1BQXRD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QUEwRWpDOztrQkFFY2pDLGEiLCJmaWxlIjoiVHJ1bmNhdGVkVGV4dC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBJY29uIGZyb20gJy4uL0NvbXBvbmVudHMvSWNvbic7XG5cbmNsYXNzIFRydW5jYXRlZFRleHQgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5zdGF0ZSA9IHsgZXhwYW5kZWQ6IGZhbHNlIH07XG4gICAgdGhpcy50b2dnbGVFeHBhbnNpb24gPSB0aGlzLnRvZ2dsZUV4cGFuc2lvbi5iaW5kKHRoaXMpO1xuICB9XG5cbiAgd29yZENvdW50ICh0ZXh0KSB7XG4gICAgaWYgKHR5cGVvZiB0ZXh0ICE9PSAnc3RyaW5nJykgcmV0dXJuIHVuZGVmaW5lZDtcbiAgICByZXR1cm4gdGV4dC50cmltKCkuc3BsaXQoJyAnKS5maWx0ZXIoeCA9PiB4Lmxlbmd0aCkubGVuZ3RoO1xuICB9XG5cbiAgcmV2ZXJzZVRleHQgKHRleHQpIHtcbiAgICBpZiAodHlwZW9mIHRleHQgIT09ICdzdHJpbmcnIHx8ICF0ZXh0Lmxlbmd0aCkgcmV0dXJuIHRleHQ7XG4gICAgcmV0dXJuIHRleHQuc3BsaXQoJycpLnJldmVyc2UoKS5qb2luKCcnKTtcbiAgfVxuXG4gIHRyaW1Jbml0aWFsUHVuY3R1YXRpb24gKHRleHQpIHtcbiAgICBpZiAodHlwZW9mIHRleHQgIT09ICdzdHJpbmcnIHx8ICF0ZXh0Lmxlbmd0aCkgcmV0dXJuIHRleHQ7XG4gICAgd2hpbGUgKHRleHQuc2VhcmNoKC9bYS16QS1aMC05XS8pICE9PSAwKSB7XG4gICAgICB0ZXh0ID0gdGV4dC5zdWJzdHJpbmcoMSk7XG4gICAgfTtcbiAgICByZXR1cm4gdGV4dDtcbiAgfVxuXG4gIHRyaW1QdW5jdHVhdGlvbiAodGV4dCkge1xuICAgIGlmICh0eXBlb2YgdGV4dCAhPT0gJ3N0cmluZycgfHwgIXRleHQubGVuZ3RoKSByZXR1cm4gdGV4dDtcblxuICAgIHRleHQgPSB0aGlzLnRyaW1Jbml0aWFsUHVuY3R1YXRpb24odGV4dCk7XG4gICAgdGV4dCA9IHRoaXMucmV2ZXJzZVRleHQodGV4dCk7XG4gICAgdGV4dCA9IHRoaXMudHJpbUluaXRpYWxQdW5jdHVhdGlvbih0ZXh0KTtcbiAgICB0ZXh0ID0gdGhpcy5yZXZlcnNlVGV4dCh0ZXh0KTtcblxuICAgIHJldHVybiB0ZXh0O1xuICB9XG5cbiAgdHJ1bmNhdGUgKHRleHQsIGN1dG9mZikge1xuICAgIGlmICh0eXBlb2YgdGV4dCAhPT0gJ3N0cmluZycgfHwgdHlwZW9mIGN1dG9mZiAhPT0gJ251bWJlcicpIHJldHVybiB0ZXh0O1xuICAgIGxldCBjb3VudCA9IHRoaXMud29yZENvdW50KHRleHQpO1xuICAgIGlmIChjb3VudCA8IGN1dG9mZikgcmV0dXJuIHRleHQ7XG5cbiAgICBsZXQgd29yZHMgPSB0ZXh0LnRyaW0oKS5zcGxpdCgnICcpLmZpbHRlcih4ID0+IHgubGVuZ3RoKTtcbiAgICBsZXQgdGhyZXNob2xkID0gTWF0aC5jZWlsKGN1dG9mZiAqIDAuNjYpO1xuICAgIGxldCBzaG9ydCA9IHdvcmRzLnNsaWNlKDAsIHRocmVzaG9sZCkuam9pbignICcpO1xuXG4gICAgcmV0dXJuIHRoaXMudHJpbVB1bmN0dWF0aW9uKHNob3J0KSArICcuLi4nO1xuICB9XG5cbiAgdG9nZ2xlRXhwYW5zaW9uICgpIHtcbiAgICBsZXQgeyBleHBhbmRlZCB9ID0gdGhpcy5zdGF0ZTtcbiAgICB0aGlzLnNldFN0YXRlKHsgZXhwYW5kZWQ6ICFleHBhbmRlZCB9KTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgbGV0IHsgZXhwYW5kZWQgfSA9IHRoaXMuc3RhdGU7XG4gICAgbGV0IHsgY2xhc3NOYW1lLCBjdXRvZmYsIHRleHQgfSA9IHRoaXMucHJvcHM7XG4gICAgY3V0b2ZmID0gdHlwZW9mIGN1dG9mZiA9PT0gJ251bWJlcicgPyBjdXRvZmYgOiAxMDA7XG4gICAgbGV0IGV4cGFuZGFibGUgPSB0aGlzLndvcmRDb3VudCh0ZXh0KSA+IGN1dG9mZjtcblxuICAgIGNsYXNzTmFtZSA9ICdUcnVuY2F0ZWRUZXh0JyArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgdGV4dCA9IGV4cGFuZGVkID8gdGV4dCA6IHRoaXMudHJ1bmNhdGUodGV4dCwgY3V0b2ZmKTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17Y2xhc3NOYW1lfT5cbiAgICAgICAge3RleHR9XG4gICAgICAgIHtleHBhbmRhYmxlICYmIChcbiAgICAgICAgICA8YnV0dG9uIGNsYXNzTmFtZT1cIlRydW5jYXRlZFRleHQtVG9nZ2xlXCIgb25DbGljaz17dGhpcy50b2dnbGVFeHBhbnNpb259PlxuICAgICAgICAgICAge2V4cGFuZGVkID8gJ1Nob3cgTGVzcycgOiAnU2hvdyBNb3JlJ31cbiAgICAgICAgICAgIDxJY29uIGZhPXtleHBhbmRlZCA/ICdhbmdsZS1kb3VibGUtdXAnIDogJ2FuZ2xlLWRvdWJsZS1kb3duJ30gLz5cbiAgICAgICAgICA8L2J1dHRvbj5cbiAgICAgICAgKX1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IFRydW5jYXRlZFRleHQ7XG4iXX0=