#!/bin/csh

set libDir=$GUS_HOME/lib/java

set cp=""
set cp=${cp}:$libDir/axis.jar:$libDir/saaj.jar:$libDir/jaxrpc.jar:$libDir/wsdl4j-1.5.1.jar
set cp=${cp}:$libDir/commons-discovery-0.2.jar:$libDir/commons-logging-api.jar
set cp=${cp}:$libDir/WDK-ServiceStub.jar.x:$libDir/WDK-Model.jar:$libDir/junit.jar

echo $cp

java -Dprocess.name=NcbiBlastProcessor -Ddatabase.name=c.parvum.nt -Dservice.url="http://delphi.pcbi.upenn.edu:8080/axis/services/WdkProcessService" -cp $cp org.gusdb.wdk.model.test.WdkProcessClientTest
