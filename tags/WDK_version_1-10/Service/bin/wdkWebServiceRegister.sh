#!/bin/csh

if ("$1$2$3" == "") then
  echo ""
  echo "usage: wdkWebServiceRegister.sh <libPath> <serverURL> <deployDescriptor>"
  echo ""
  exit
endif

set pwd=`pwd`
cd $1
set cp = ""
foreach f (*.jar)
  set cp = ${1}/${f}:${cp}
end
cd $pwd

java -cp $cp org.apache.axis.client.AdminClient -l${2}/AdminService $3
