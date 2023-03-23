#!/bin/bash

src_dir=./
bin_dir=./bin
main_class=SocketServer
main_java=SocketServer.java
jar_file=exc.jar
doc_dir=./doc


cd "$(dirname "$0")"

javac -cp . $main_java -d $bin_dir
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    read -p "Press enter to continue..."
    exit 1
fi

cd $bin_dir
jar cvfe $jar_file $main_class .
if [ $? -ne 0 ]; then
    echo "JAR creation failed!"
    read -p "Press enter to continue..."
    exit 1
fi

cd ../
javadoc -d $doc_dir -sourcepath $src_dir -subpackages .

echo "Success!"
java -jar $bin_dir/$jar_file
read -p "Press enter to continue..."