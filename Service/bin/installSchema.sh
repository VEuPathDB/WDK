#!/bin/bash

if [ $# != 1 ]; then
  echo; echo "USAGE: installSchema.sh <gusHome>"; echo
  exit 1
fi

gusHome=$1

if ! [ -e $gusHome ]; then
  echo "Provided gus_home $gusHome does not exist."
  exit 1
fi

#install dependencies
yarn

for file in $(find $gusHome/doc/WDK/Service/schema -name "*.json" | grep -v includes); do
  echo "Processing $file"
  schemaBuilder.js $file $file
done


