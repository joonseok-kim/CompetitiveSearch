@echo off
java -cp target/CSTS-1.0-jar-with-dependencies.jar org.joonseok.comset.ModelBuilder -c generate_matrix -config etc/config.properties
java -cp target/CSTS-1.0-jar-with-dependencies.jar org.joonseok.comset.ModelBuilder -c factorization -config etc/config.properties
@pause
