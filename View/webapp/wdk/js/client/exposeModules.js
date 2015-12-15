import _ from 'lodash';
import React from 'react';
import ReactRouter from 'react-router';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = ReactRouter;
