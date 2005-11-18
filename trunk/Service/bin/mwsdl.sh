rm -rf deploy/*

java -Dcatalina.home=/usr/local/tomcat/tomcat/ -cp classes:$CLASSPATH org.apache.axis.wsdl.Java2WSDL -o WdkProcessService.wsdl -l"http://lime.ctegd.uga.edu:8888/axis/WdkProcessService" -n "service.wdk.gusdb.org" -p"org.gusdb.wdk.service" "urn:service.wdk.gusdb.org" org.gusdb.wdk.service.WdkProcessService


mv *.wsdl deploy/

cd deploy

java -cp ../classes:$CLASSPATH org.apache.axis.wsdl.WSDL2Java -o . -d Session -c org.gusdb.wdk.service.WdkProcessServiceImp -s WdkProcessService.wsdl

mv org/gusdb/wdk/service/deploy.wsdd deploy.wsdd
mv org/gusdb/wdk/service/undeploy.wsdd undeploy.wsdd


rm -f org/gusdb/wdk/service/WdkProcessServiceImp.java

mkdir bin

javac -cp .:$CLASSPATH -d bin/ org/gusdb/wdk/service/*.java

cd bin

jar cvf ../../process-client.jar org

cd ../..
