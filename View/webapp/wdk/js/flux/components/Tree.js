import React from 'react';
import wrappable from '../utils/wrappable';

let identity = i => i;

let Tree = React.createClass({

  propTypes: {
    items: React.PropTypes.array,
    renderItem: React.PropTypes.func,
    childrenProperty: React.PropTypes.string,
    // Will be called as `getKey(item, index, items)`.
    getKey: React.PropTypes.func,
    depth: React.PropTypes.number,
    maxDepth: React.PropTypes.number
  },

  getDefaultProps() {
    return {
      items: [],
      renderItem: identity,
      childrenProperty: 'children',
      getKey: identity,
      depth: 1,
      maxDepth: Infinity
    };
  },

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { items, renderItem, getKey, depth } = this.props;
    if (items.length === 0) { return null; }
    return (
      <ul className="wdk-TreeNodeCollection">
        {items.map((item, index) => {
          let keyValue = getKey(item, index, items);
          return (
            <li className="wdk-TreeNode" key={keyValue}>
              {renderItem(item, index, depth)}
              {this.renderChildren(item)}
            </li>
          );
        })}
      </ul>
    );
  },

  renderChildren(item) {
    let { childrenProperty, depth, maxDepth } = this.props;
    let children = item[childrenProperty];
    if (!children || children.length === 0 || depth === maxDepth) { return null; }
    return (
      <Tree {...this.props} items={children} depth={depth + 1}/>
    );
  }

});

export default wrappable(Tree);
