# WDK View

The WDK View component provides client-side related files, including JSP,
javascript, image, and css files. The component can be installed with the
GUS build system.


## Static Assets

Static assets depend on a node.js based build tool (currently Grunt.js). You
can build the assets with the comand `npm run dist`. When building your WDK
site using the GUS build system (`bldw YourProject ...`), this biuld task will
be called automatically, and the files will be copied to the appropriate
location in your webapp dir.
