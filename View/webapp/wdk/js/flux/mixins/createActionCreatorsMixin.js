import React from 'react';

export default function createActionCreatorsMixin(...actionCreatorsNames) {

  return {

    contextTypes: {
      lookup: React.PropTypes.func.isRequired
    },

    componentDidMount() {

      // For each actionCreatorsNames, find the instance using the lookup()
      // method and assign a property of the same name to this component.
      actionCreatorsNames.forEach(actionCreatorsName => {
        var actionCreators = this.context.lookup(actionCreatorsName);
        this[actionCreatorsName] = actionCreators;
      });
    }

  };

}
