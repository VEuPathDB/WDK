rm -rf classes/*

javac -cp classes/:lib/java/junit.jar:$CLASSPATH -sourcepath src/java/ -d classes src/java/org/gusdb/wdk/service/test/WdkProcessServiceTest.java

cd classes

jar cvf ../wdk-service.jar org 
