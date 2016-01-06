# Examples Developement Server

The examples development server is useful for fast prototyping of React components.
It provides a way to quickly author React component in the WDK codebase, and to test
them with real data outside of a WDK application.


## Running the server

To before running the server, you will need to install webpack-dev-server globally, via
npm:

    $ npm i -g webpack-dev-server


In WDK/View, run the following command to start the server:

    $ make examples-server


This will cause the necessary npm modules for WDK to be installed, and it will start the
server on localhost. A link will be printed to the console to view the application. If
any code is modified while the server is running, the JavaScript will be recompiled and
the webpage will be automatically reloaded.


## Adding Examples

The examples app will automatically load any modules in the ./app/examples directory.
The module should export a React component named Example.

See ./app/examples/CheckboxList.js for an example.
