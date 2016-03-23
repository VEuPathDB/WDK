import { Component, PropTypes } from 'react';
import { isLeaf } from '../utils/TreeUtils';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';

const visibleElement = {display: "block"};
const hiddenElement = {display: "none"};

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
          {isSelectable ?
            <IndeterminateCheckbox
              name={name}
              checked={isSelected}
              indeterminate={isIndeterminate}
              node={node}
              value={getNodeId(node)}
              toggleCheckbox={toggleSelection} />
            : ""
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
