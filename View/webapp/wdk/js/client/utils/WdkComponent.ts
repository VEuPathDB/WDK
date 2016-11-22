import { ComponentClass, PureComponent } from 'react';

type Spec = {
  mapStoreToProps: (state: Object) => Object;
  mapActionsToProps: Object;
}


makeContainer({
  mapStoreToProps(state => state),
  mapActionsToProps: {
    startThing,
    stopThing
  }
})
function makeWdkContainer(spec: Spec, component: React.ComponentClass)