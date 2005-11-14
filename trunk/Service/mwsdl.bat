java -Dcatalina.home=C:\Apache\Tomcat -cp classes;%CLASSPATH% org.apache.axis.wsdl.Java2WSDL -o WdkProcessService.wsdl -l"http://localhost:8080/axis/WdkProcessService" -n "service.wdk.gusdb.org" -p"org.gusdb.wdk.service" "urn:service.wdk.gusdb.org" org.gusdb.wdk.service.WdkProcessService


move *.wsdl deploy\

cd deploy

java -cp ../classes;%CLASSPATH% org.apache.axis.wsdl.WSDL2Java -o . -d Session -c org.gusdb.wdk.service.WdkProcessServiceImp -s WdkProcessService.wsdl

move org\gusdb\wdk\service\deploy.wsdd deploy.wsdd
move org\gusdb\wdk\service\undeploy.wsdd undeploy.wsdd

cd ../