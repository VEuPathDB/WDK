import React from 'react';
import RecordMainCategorySection from './RecordMainCategorySection';
import RecordTable from './RecordTable';
import Tree from './Tree';
import wrappable from '../utils/wrappable';

let RecordMainSection = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { meta, record, recordClass, recordClasses, hiddenCategories } = this.props;
    let { attributes, tables } = record;

    let attributeCategories = recordClass.attributeCategories.filter(function(c) {
      return !hiddenCategories.includes(c.name);
    });

    return (
      <div className="wdk-Record-main">
        <Tree
          items={attributeCategories}
          childrenProperty="subCategories"
          getKey={category => String(category.name)}
          renderItem={function renderCategorySection(category, index, depth) {
            return (
              <RecordMainCategorySection
                depth={depth}
                category={category}
                record={record}
                recordClass={recordClass}
              />
            );
          }}
        />
      </div>
    );
  }

});

export default wrappable(RecordMainSection);
