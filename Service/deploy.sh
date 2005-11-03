cp -f lib/java/*.jar $TOMCAT_HOME/webapps/axis/WEB-INF/lib/


cd classes

jar cvf wdk-service.jar org

mv -f wdk-service.jar $TOMCAT_HOME/webapps/axis/WEB-INF/lib/

cd ..

cp -f ncbiBlast-config.xml $TOMCAT_HOME/webapps/axis/WEB-INF/
cp -f wdkService-config.xml $TOMCAT_HOME/webapps/axis/WEB-INF/


