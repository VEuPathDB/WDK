import { PropTypes } from 'react';

let treePropType = PropTypes.arrayOf(PropTypes.shape({
  children: treePropType
}));

let identity = n => n;

Tree.propTypes = {

  /**
   * A function that returns an identifier for a tree node. This only needs to
   * be unique amongst the node's siblings. This will be used as the component
   * key.
   */
  id: PropTypes.func.isRequired,

  /**
   * Array of tree nodes
   */
  tree: treePropType,

  /**
   * Function that returns a React renderable. It will be passed
   * the same props as Tree, as well as node={currentNode}
   */
  node: PropTypes.func,

  /**
   * Function that returns an array children for a node.
   */
  childNodes: PropTypes.func,

  /**
   * Number of ancestors.
   */
  depth: PropTypes.number
};


Tree.defaultProps = {
  node: identity,
  tree: [],
  childNodes: node => node.children,
  depth: 0
};

/**
 * Renders a tree, bottom-up. For each node, it's rendered children will be
 * passed in the usual React `props.children` manner. This allows a given node
 * to determine where exactly to place its children.
 *
 *
 * Example:
 *
 *    let Node = (props) => (
 *      <li>
 *        {props.node.value}
 *        <ul>{props.children}</ul>
 *      </li>
 *    );
 *
 *    let ValueTree = (props) => (
 *      <ul>
 *        <Tree
 *          tree={props.valueTree}
 *          node={Node}
 *          id={value => value}
 *          childNodes={node => node.children}
 *        />
 *      </ul>
 *    );
 *
 *
 *    let valueTree = [
 *      {
 *        value: 'A',
 *        children: [
 *          {
 *            value: 'B'
 *          },
 *          {
 *            value: 'C'
 *          }
 *        ]
 *      }
 *    ];
 *
 *
 *    ReactDOM.render(ValueTree, { valueTree }, document.getElementById("value-tree"));
 *
 *    // This will produce the following DOM structure:
 *
 *    <ul>
 *      <li>A
 *        <ul>
 *          <li>B</li>
 *          <li>C</li>
 *        </ul>
 *      </li>
 *    <ul>
 */
export default function Tree(props) {
  let NodeComponent = props.node;

  return (
    <div>
      {props.tree.map((node, index) => (
        <div key={props.id(node)}>
          <NodeComponent {...props} node={node} depth={props.depth} index={index}>
            <Tree {...props} tree={props.childNodes(node)} depth={props.depth + 1}/>
          </NodeComponent>
        </div>
      ))}
    </div>
  );
}
