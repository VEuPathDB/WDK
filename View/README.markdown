# WDK View

The WDK View component provides client-side related files, including JSP,
javascript, image, and css files. The component can be installed with the
GUS build system.


## Static Assets

Static assets depend on a node.js based build tool (currently webpack). You
can build the assets with the command `npm run dist`. When building your WDK
site using the GUS build system (`bldw YourProject ...`), this build task will
be called automatically, and the files will be copied to the appropriate
location in your webapp dir.


## Running JavaScript Tests

Tests reside in the `test/` directory. You can generate the test assets by
running the following command (in this directory) and open test/index.html
in your browser:

    npm run test

Alternatively, you can install webpack-dev-server (npm i -g webpack-dev-server)
and run the following command:

    npm run test-server

This will run a local server on your computer where you can view the test
runner output in your browser. As files are changed, the webpage will
automatically refresh (live reload). You can view the webpage at
http://localhost:8080/webpack-dev-server/test/ (the port number may vary --
the important part is the path that comes after: /webpack-dev-server/test/).
