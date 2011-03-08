echo "Installing milton libs"
mvn install:install-file -DgroupId=com.ettrema -DartifactId=milton-servlet -Dversion=1.5.3-SNAPSHOT -Dpackaging=jar -Dfile=lib/milton-servlet-1.5.3-SNAPSHOT.jar
mvn install:install-file -DgroupId=com.ettrema -DartifactId=milton-api -Dversion=1.5.3-SNAPSHOT -Dpackaging=jar -Dfile=lib/milton-api-1.5.3-SNAPSHOT.jar
echo "Libs installed on local repo"
