import React from 'react';
import Tree from './Tree';
import wrappable from '../utils/wrappable';

function noop(){}

let sidebarClass = 'wdk-Record-sidebar';
let topBuffer = 10;

let RecordNavigationSection = React.createClass({

  getDefaultProps() {
    return {
      onVisibleChange: noop
    };
  },

  getInitialState() {
    return {
      isFixed: false
    };
  },

  componentDidMount() {
    this.node = React.findDOMNode(this);
    window.addEventListener('scroll', this.updateIsFixed);
  },

  componentWillUnmount() {
    window.removeEventListener('scroll', this.updateIsFixed);
  },

  // Set position to fixed if top is above threshold, otherwise
  // set position to absolute.
  updateIsFixed() {
    // See https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect
    let rect = this.node.getBoundingClientRect();
    if (rect.top < topBuffer && this.state.isFixed === false) {
      this.setState({
        isFixed: true
      });
    }
    else if (rect.top >= topBuffer && this.state.isFixed === true) {
      this.setState({
        isFixed: false
      });
    }
  },
  handleShowAll() {
  },

  handleShowNone() {
  },

  handleToggle(e, category) {
    this.props.onVisibleChange({
      category,
      isVisible: e.target.checked
    });
  },

  render() {
    let { recordClass, hiddenCategories } = this.props;
    let { isFixed } = this.state;
    let style = {
      position: isFixed ? 'fixed' : '',
      top: isFixed ? topBuffer : ''
    };
    return (
      <div> {/* This node is used to track scroll position */}
        <div className={sidebarClass} style={style}>
          <h3 className="wdk-RecordSidebarHeader">Categories</h3>
          <Tree
            items={recordClass.attributeCategories}
            maxDepth={1}
            childrenProperty="subCategories"
            getKey={item => String(item.name)}
            renderItem={category => {
              return (
                <div className="wdk-RecordNavigationItem">
                  <input
                    className="wdk-Record-sidebar-checkbox"
                    type="checkbox"
                    checked={!hiddenCategories.includes(category.name)}
                    onChange={(e) => {
                      this.handleToggle(e, category);
                    }}
                  />
                  <a href={'#' + category.name} className="wdk-Record-sidebar-title">
                    <strong>{category.displayName}</strong>
                  </a>
                </div>
              );
            }}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
