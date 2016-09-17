mvn install:install-file -Dfile=./../lib/cloudera-impala-jdbc-4.1_2.5.32/ImpalaJDBC41.jar -DgroupId=impala-jdbc -DartifactId=jdbc-driver -Dversion=4.1.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=./../lib/cloudera-impala-jdbc-4.1_2.5.32/TCLIServiceClient.jar -DgroupId=impala-jdbc -DartifactId=jdbc-driver-tcli-service-client -Dversion=4.1.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=./../lib/cloudera-impala-jdbc-4.1_2.5.32/ql.jar -DgroupId=impala-jdbc -DartifactId=jdbc-driver-ql -Dversion=4.1.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=./../lib/cloudera-impala-jdbc-4.1_2.5.32/hive_metastore.jar -DgroupId=impala-jdbc -DartifactId=hive_metastore -Dversion=4.1.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=./../lib/cloudera-impala-jdbc-4.1_2.5.32/hive_service.jar -DgroupId=impala-jdbc -DartifactId=hive_service -Dversion=4.1.2 -Dpackaging=jar -DgeneratePom=true

