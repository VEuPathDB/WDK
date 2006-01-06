cd deploy

del org\gusdb\wdk\service\WdkProcessServiceImp.java

javac -classpath bin/;../classes/;%CLASSPATH% -d bin org\gusdb\wdk\service\*.java

md bin
cd bin

jar cvf ../../process-client.jar org
