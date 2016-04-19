import { Component, PropTypes } from 'react';
import { isLeaf } from '../utils/TreeUtils';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';

const visibleElement = {display: "block"};
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
    let { name, checked, value } = this.props;
    return (
      <input type="radio" name={name} value={value} checked={checked} onChange={this.handleClick.bind(this)} />
    );
  }
}

class CheckboxTreeNode extends Component {

  shouldComponentUpdate(nextProps) {
    return (nextProps.node !== this.props.node);
  }

  render() {
    let {
      name,
      node,
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
    let nodeType = isLeafNode ? "wdk-CheckboxTree-leafItem" :
      isExpanded ? "wdk-CheckboxTree-expandedItem" : "wdk-CheckboxTree-collapsedItem";
    let NodeComponent = nodeComponent;

    return (
      <li className={nodeType} style={nodeVisibilityCss}>
        {isLeafNode || isActiveSearch ? "" :
          (<AccordionButton expanded={isExpanded} node={node} toggleExpansion={toggleExpansion} />) }
        <label>
          {!isSelectable || (!isMultiPick && !isLeafNode) ? "" :
            isMultiPick ?
              <IndeterminateCheckbox
                name={name}
                checked={isSelected}
                indeterminate={isIndeterminate}
                node={node}
                value={getNodeId(node)}
                toggleCheckbox={toggleSelection} /> :
              <TreeRadio
                name={name}
                checked={isSelected}
                value={getNodeId(node)}
                node={node}
                onChange={toggleSelection} />
          }
          <NodeComponent node={node} />
        </label>
        {isLeafNode ? "" :
          <ul className="fa-ul wdk-CheckboxTree-list" style={childrenVisibilityCss}>
            {getNodeChildren(node).map(child =>
              <CheckboxTreeNode
                key={"node_" + getNodeId(child)}
                name={name}
                node={child}
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
