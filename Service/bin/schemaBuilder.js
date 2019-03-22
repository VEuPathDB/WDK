#!/usr/bin/env node

'use strict';

var fs = require('fs')
var refParser = require('json-schema-ref-parser');
var mergeAllOf = require('json-schema-merge-allof');

if (process.argv.length != 4) {
    console.log("\nUSAGE: schemaBuilder.js <inputDir> <outputDir>\n");
    process.exit();
}

var inputFile = process.argv[2];
var outputFile = process.argv[3];

//console.log("Input file: " + inputFile + "\nOutput file: " + outputFile);

function filterSchemaProps(obj, level) {
  level++;
  if (Array.isArray(obj)) {
    return obj.map(function(each) { return filterSchemaProps(each, level); });
  }
  else if (obj == null || typeof obj != 'object') {
    return obj;
  }
  else {
    Object.keys(obj).forEach(function(key) {
      obj[key] = (level == 1 || key != "$schema") ?
        filterSchemaProps(obj[key], level) : undefined;
    });
    return obj;
  }
}

refParser.dereference(inputFile, { dereference: { circular: "ignore" } })
  .then(function(schema) {
    var mergedSchema = mergeAllOf(schema);
    mergedSchema = filterSchemaProps(mergedSchema, 0);
    fs.writeFile(outputFile,
      JSON.stringify(mergedSchema, null, 2),
      function(err) {
        if (err) {
          console.error(err);
          process.exit(1);
        }
        //console.log("Done.");
      }
    );
  })
  .catch(function(err) {
    console.error(err);
    process.exit(1);
  });
