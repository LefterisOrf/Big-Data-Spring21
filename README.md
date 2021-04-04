# Big-Data-Spring21

*Compilation* : javac -sourcepath src -d build/ src/**/**/*.java -cp ".;src/resources/commons-lang3-3.12.0.jar"   


*Execution* :  
  KVBroker - java -cp "./build;./src/resources/commons-lang3-3.12.0.jar" com.main.KVBroker -s serverFile.txt -i ./src/indexData.txt -k 1  
  KVServer - java -cp ";./build;src/resources/commons-lang3-3.12.0.jar" com.main.KVServer  
  CreateData - java -cp "./build;./src/resources/commons-lang3-3.12.0.jar" com.main.CreateData -k keyFile.txt -n 100000 -d 4 -l 5 -m 5 > indexData.txt  
