import React from 'react';
import {Link as RRLink} from 'react-router-dom';
import {wrappable} from 'Utils/ComponentUtils';

/** React Router Link decorator that adds className */
function Link(props: any) {
  return (
    <RRLink {...props} className={'wdk-ReactRouterLink ' + props.className}/>
  );
}

export default wrappable(Link);
