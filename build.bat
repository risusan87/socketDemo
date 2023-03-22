@echo off

set src_dir=./src
set bin_dir=./bin
set main_class=picross.PicrossCore
set main_java=picross/PicrossCore.java
set jar_file=Game.jar
set doc_dir=./doc

cd /d "%~dp0"

javac -cp .;%src_dir% %src_dir%\%main_java% -d %bin_dir%
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

cd %bin_dir%
jar cvfe %jar_file% %main_class% .
if %errorlevel% neq 0 (
    echo JAR creation failed!
    pause
    exit /b 1
)

cd ..
javadoc -d %doc_dir% -sourcepath %src_dir% -subpackages .

echo Success!
java -jar %bin_dir%\%jar_file%
pause
