#!/usr/bin/perl

#------------------------------------------------------------------------
# This module is used by WDK Model command line tools to set up command environment
#
# Thomas Gan
#
# $Revision: $ $Date: $ $Author: $
#------------------------------------------------------------------------

package WDK::Model::CommandHelper;

use strict;
use Carp;

sub getJavaClasspath {
  my $GUS_HOME = shift;
  my $javaDir = "$GUS_HOME/lib/java";

  opendir(JARDIR, $javaDir) ||
    &confess ("Error:  Could not open $javaDir.  Please check it exists and try again.");

  my $CLASSPATH = "";

  while (my $nextFileName = readdir(JARDIR)) {
    if ($nextFileName =~ /.*\.jar$/) {
      $CLASSPATH .= "$javaDir/$nextFileName" . ":";
    }
  }

  my $dbDriverDir = "$javaDir/db_driver";
  opendir(DBDRIVERDIR, $dbDriverDir) ||
    &confess("Error: Could not open $dbDriverDir. Please check it exists and try again");

  my $driverFiles = 0;
  while (my $nextFileName = readdir(DBDRIVERDIR)) {
    if ($nextFileName =~ /.*\.(jar|zip)$/) {
      $CLASSPATH .= $dbDriverDir . '/' . $nextFileName . ":";
      $driverFiles++;
    }
  }
  &confess("Error: No database driver files under ${dbDriverDir}.") if !$driverFiles;

  return $CLASSPATH;
}

sub getJavaArgs {
  my @args = @_;
  my $args = "";
  foreach my $arg (@args) {
    $args .= ($arg =~ /\-/ ? " $arg" : " \"$arg\"");
  }
  return $args;
}

sub getSystemArgs {
  my @args = @_;
  my $sysargs = "";
  foreach my $arg (@args) {
    if ($arg =~ /\-/) {
        $arg =~ s/\-/\-D/g;
        $sysargs .= " $arg=";
    } else {
        $sysargs .= "\"$arg\"";
    }
  }
  return $sysargs;
}

sub getSystemProps {
  my ($GUS_HOME, $cmdName) = @_;
  my $sysProps = "-DconfigDir=$GUS_HOME/config -DcmdName=$cmdName"
    . " -DschemaFile=$GUS_HOME/lib/rng/wdkModel.rng"
    . " -DxmlSchemaFile=$GUS_HOME/lib/rng/xmlAnswer.rng"
    . " -DxmlDataDir=$GUS_HOME/data/"
    . " -DsanitySchemaFile=$GUS_HOME/lib/rng/sanityModel.rng"
    . " -DgusHome=$GUS_HOME";
  return $sysProps;
}

1;

