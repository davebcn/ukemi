CLASSPATH=$CLASSPATH:~/.m2/repository/org/mortbay/jetty/jetty/6.0.1/jetty-6.0.1.jar

echo "CLASSPATH is $CLASSPATH"

java -classpath $CLASSPATH -jar target/ukemi-1.0-jar-with-dependencies.jar $1 $2 
