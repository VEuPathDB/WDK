#!/bin/bash
################################################################################
##
## This script generates a graph of the JSP and custom tag dependencies across
## multiple projects.  It requires the 'dot' executable, part of the GraphViz
## project (http://www.graphviz.org).
##
## Running it will scan all the projects currently in your $PROJECT_HOME for
## .jsp and .tag files, consolidate the names, and search through all those
## files for other .jsp and .tag files.  It will then link the strings together
## into dependencies, and generate the graph.
##
## Notes:
##   - Ignores href attributes.  This should not have to be an issue since any
##     hrefs should never point directly to JSPs, but there are some in WDK.
##   - Even though we capture which project the file came out of,
##     the dependencies are not differentiated.  This means that files of the
##     same name will be represented by one node on the graph.  This is an
##     area for future development.
##
################################################################################

# Change these constants if you don't like the names.  Files will be placed in
# the current directory.  Extensions matter.  Only $imageFile will remain after
# successful completion; others are deleted.
viewFiles="fileListFile"
depListFile="depListFile"
dotFile="dependencyTree.dot"
imageFile="dependencyTree.png"

# Custom JSPs (i.e. those with names containing "Classes." or "Questions.") can
# be filtered out of the graph.  To do so, set the following to true.
filterCustomJsps=false

# remove previous versions of files
rm -f $viewFiles $depListFile $dotFile $imageFile

# save current dir and go to project home
currentDir=`pwd`
cd $PROJECT_HOME

find . -name "*.jsp" | sed 's|.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.jsp\)|\1|' >> $currentDir/$viewFiles
find . -name "*.tag" | sed 's|.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.tag\)|\1|' >> $currentDir/$viewFiles

# find jsp dependencies in jsp files
find . -name "*.jsp" | xargs grep "\.jsp" | grep -v jspwrap | grep -v href | \
  sed 's|\./\([a-zA-Z][a-zA-Z]*\)/.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.jsp\):.*[/" ]\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.jsp\).*|\1 \2 \3|' >> $currentDir/$depListFile

# find jsp dependencies in tag files
find . -name "*.tag" | xargs grep "\.jsp" | grep -v jspwrap | grep -v href | \
  sed 's|\./\([a-zA-Z][a-zA-Z]*\)/.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.tag\):.*[/" ]\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.jsp\).*|\1 \2 \3|' >> $currentDir/$depListFile

# find tag dependencies in jsp files
find . -name "*.jsp" | xargs grep "\<imp:" | \
  sed 's|\./\([a-zA-Z][a-zA-Z]*\)/.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.jsp\):.*imp:\([a-zA-Z][a-zA-Z]*\).*|\1 \2 \3.tag|' >> $currentDir/$depListFile

# find tag dependencies in tag files
find . -name "*.tag" | xargs grep "\<imp:" | \
  sed 's|\./\([a-zA-Z][a-zA-Z]*\)/.*/\([\.a-zA-Z0-9_-][\.a-zA-Z0-9_-]*\.tag\):.*imp:\([a-zA-Z][a-zA-Z]*\).*|\1 \2 \3.tag|' >> $currentDir/$depListFile

# go back to working directory
cd $currentDir

# begin dot file
echo "digraph G {" > $dotFile

# possible dot customizations (defaults are fine for now)
#echo "  graph [ fontsize=8, rankdir=\"TB\" ]" >> $dotFile
#echo "  node [ fontsize=8, height=0, width=0, margin=0.03,0.02 ]" >> $dotFile
#echo "  edge [ fontsize=8, arrowhead=open ]" >> $dotFile

# add nodes to dot file
cat $viewFiles | sort -u | awk '{ print "  \"" $1 "\" [ shape=rectangle, color=black ]" }' >> $dotFile

# add dependencies to dot file
if $filterCustomJsps; then
  cat $depListFile | grep -v "Classes\." | grep -v "Questions\." | sort -u | awk '{ print "  \"" $2 "\" -> \"" $3 "\""}' >> $dotFile
else
  cat $depListFile | sort -u | awk '{ print "  \"" $2 "\" -> \"" $3 "\""}' >> $dotFile
fi

# complete dot file
echo "}" >> $dotFile

# run dot to generate the graph
dot -Tpng -o$imageFile $dotFile

# remove temporary files
rm -f $viewFiles $depListFile $dotFile
