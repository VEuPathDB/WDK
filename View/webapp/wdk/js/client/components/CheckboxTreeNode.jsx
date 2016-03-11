import React,{PropTypes} from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';

const visibleElement = {display: "block"};
const hiddenElement = {display: "none"};

let CheckboxTreeNode = function(props) {

  let {
      node,
      nodeType,
      fieldName,
      isSearchMode,
      isExpanded,
      isSelected,
      isIndeterminate,
      isVisible,
      isMatching,
      removeCheckboxes,
      toggleCheckbox,
      toggleExpansion,
      getNodeFormValue,
      getBasicNodeReactElement,
      getNodeChildren,
      children
    } = props;

  let nodeVisibility = isMatching ? visibleElement : hiddenElement;
  let childrenVisibility = isVisible ? visibleElement : hiddenElement;

  return (
    <li className={nodeType} style={nodeVisibility}>
      {getNodeChildren(node) && !isSearchMode ?
      <AccordionButton expanded={isExpanded}
                       node={node}
                       toggleExpansion={toggleExpansion} /> : "" }
      <label>
        {!removeCheckboxes ?
          <IndeterminateCheckbox
            name={fieldName}
            checked={isSelected}
            indeterminate={isIndeterminate}
            node={node}
            value={getNodeFormValue(node)}
            toggleCheckbox={toggleCheckbox} />
          : ""
        }
        {getBasicNodeReactElement(node)}
      </label>
      {children.length > 0 ?
        <ul className="fa-ul wdk-CheckboxTree-list" style={childrenVisibility}>
          {props.children}
        </ul> : "" }
      </li>
  );
};

export default CheckboxTreeNode;
