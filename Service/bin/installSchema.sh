#!/bin/bash

if [ $# != 2 ]; then
  echo; echo "USAGE: installSchema.sh <projectHome> <gusHome>"; echo
  exit 1
fi

projectHome=$1
gusHome=$2

if ! [ -e $projectHome ]; then
  echo "Provided project_home $projectHome does not exist."
  exit 1
fi

if ! [ -e $gusHome ]; then
  echo "Provided gus_home $gusHome does not exist."
  exit 1
fi

# install dependencies
yarn

# remove any existing schema files
find $gusHome/doc/WDK/Service/schema -name "*.json" | xargs rm

# for each schema file in project_home, process and write to gus_home
for inputFile in $(find $projectHome/WDK/Service/doc/schema -name "*.json" | grep -v includes); do
  relativeFile=${inputFile/$projectHome\/WDK\/Service\/doc\/schema\//}
  outputFile=$gusHome/doc/WDK/Service/schema/$relativeFile
  echo "Processing $relativeFile"
  schemaBuilder.js $inputFile $outputFile
  [ $? -eq 0 ] || exit 1
done


