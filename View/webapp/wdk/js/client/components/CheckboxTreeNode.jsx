import { Component, PropTypes } from 'react';
import { isLeaf } from '../utils/TreeUtils';
import IndeterminateCheckbox from './IndeterminateCheckbox';

const visibleElement = {display: ""};
const hiddenElement = {display: "none"};


/**
 * Expects the following props:
 *   name: string
 *   checked: bool
 *   value: string
 *   node: object
 *   onChange: func
 */
class TreeRadio extends Component {

  handleClick(event) {
    let { checked, onChange, node } = this.props;
    if (!checked) {
      onChange(node, false);
    }
  }

  render() {
    let { name, checked, value, className } = this.props;
    return (
      <input type="radio" className={className} name={name} value={value} checked={checked} onChange={this.handleClick.bind(this)} />
    );
  }
}

class CheckboxTreeNode extends Component {

  constructor(props) {
    super(props);
    this.handleLabelClick = () => {
      this.props.toggleExpansion(this.props.node);
    };
  }

  shouldComponentUpdate(nextProps) {
    return (nextProps.node !== this.props.node);
  }

  render() {
    let {
      name,
      node,
      listClassName,
      getNodeState,
      isSelectable,
      isMultiPick,
      isActiveSearch,
      toggleSelection,
      toggleExpansion,
      getNodeId,
      getNodeChildren,
      nodeComponent
    } = this.props;

    let { isSelected, isIndeterminate, isVisible, isExpanded } = getNodeState(node);
    let isLeafNode = isLeaf(node, getNodeChildren);
    let nodeVisibilityCss = isVisible ? visibleElement : hiddenElement;
    let childrenVisibilityCss = isExpanded ? visibleElement : hiddenElement;
    let nodeType = isLeafNode ? "leaf"
                 : isExpanded ? "expanded"
                 : "collapsed";
    let NodeComponent = nodeComponent;
    let classNames = 'wdk-CheckboxTreeItem wdk-CheckboxTreeItem__' + nodeType +
      (isSelectable ? ' wdk-CheckboxTreeItem__selectable' : '');

    return (
      <li className={classNames} style={nodeVisibilityCss}>
        {isLeafNode || isActiveSearch ? "" :
          <i
            className={'fa fa-li fa-caret-' + (isExpanded ? 'down ' : 'right ') +
              'wdk-CheckboxTreeToggle wdk-CheckboxTreeToggle__' + (isExpanded ? 'expanded' : 'collapsed') }
            onClick={this.handleLabelClick}
          />}

        <div className="wdk-CheckboxTreeNodeWrapper" onClick={isSelectable ? undefined : this.handleLabelClick}>
          {!isSelectable || (!isMultiPick && !isLeafNode) ? (
            <NodeComponent node={node} />
          ) : (
            <label>
              {isMultiPick ?
                <IndeterminateCheckbox
                  className="wdk-CheckboxTreeCheckbox"
                  name={name}
                  checked={isSelected}
                  indeterminate={isIndeterminate}
                  node={node}
                  value={getNodeId(node)}
                  toggleCheckbox={toggleSelection} /> :
                <TreeRadio
                  className="wdk-CheckboxTreeCheckbox"
                  name={name}
                  checked={isSelected}
                  value={getNodeId(node)}
                  node={node}
                  onChange={toggleSelection} />
              }
              <NodeComponent node={node} />
            </label>
          )}
        </div>
        {isLeafNode ? "" :
          <ul className={listClassName} style={childrenVisibilityCss}>
            {getNodeChildren(node).map(child =>
              <CheckboxTreeNode
                key={"node_" + getNodeId(child)}
                name={name}
                node={child}
                listClassName={listClassName}
                getNodeState={getNodeState}
                isSelectable={isSelectable}
                isMultiPick={isMultiPick}
                isActiveSearch={isActiveSearch}
                toggleSelection={toggleSelection}
                toggleExpansion={toggleExpansion}
                getNodeId={getNodeId}
                getNodeChildren={getNodeChildren}
                nodeComponent={nodeComponent} />
            )}
          </ul>
        }
      </li>
    );
  }
};

export default CheckboxTreeNode;
