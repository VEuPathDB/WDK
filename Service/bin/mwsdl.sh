#!/bin/csh

set cp = ""
foreach f ($GUS_HOME/lib/java/*.jar)
  set cp = ${f}:${cp}
end

rm -rf deploy/*

java -cp $cp org.apache.axis.wsdl.Java2WSDL -o WdkProcessService.wsdl -l"http://delphi.pcbi.upenn.edu:8080/axis/WdkProcessService" -n "service.wdk.gusdb.org" -p"org.gusdb.wdk.service" "urn:service.wdk.gusdb.org" org.gusdb.wdk.service.WdkProcessService


mv *.wsdl deploy/

cd deploy

java -cp $cp org.apache.axis.wsdl.WSDL2Java -o . -d Session -c org.gusdb.wdk.service.WdkProcessServiceImp -s WdkProcessService.wsdl

mv org/gusdb/wdk/service/deploy.wsdd deploy.wsdd
mv org/gusdb/wdk/service/undeploy.wsdd undeploy.wsdd


rm -f org/gusdb/wdk/service/WdkProcessServiceImp.java

mkdir bin

javac -cp .:$cp -d bin/ org/gusdb/wdk/service/*.java

cd bin

jar cvf ../../WDK-Service-stub.jar org

cd ../..
