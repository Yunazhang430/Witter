@echo off
for /f "usebackq tokens=*" %%a in (`echo %cd%`) do SET WORK_DIR=%%a

echo [INFO]: Setting up temporary directories...
mkdir %WORK_DIR%\witter-tmp

copy Witter.jar %WORK_DIR%\witter-tmp\

cd %WORK_DIR%\witter-tmp

jar xf Witter.jar

cd %WORK_DIR%

copy UserStore.java %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\

copy WeetStore.java %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\

copy FollowerStore.java %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\

del %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\WeetStore.class
del %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\UserStore.class
del %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\FollowerStore.class

cd %WORK_DIR%\witter-tmp\WEB-INF\classes\ 

echo [SUCCESS]: Directories set up!
echo [INFO]:  Compiling your files...

javac uk\ac\warwick\java\cs126\services\UserStore.java > %WORK_DIR%\UserStore.log 2>&1
javac uk\ac\warwick\java\cs126\services\WeetStore.java > %WORK_DIR%\WeetStore.log 2>&1
javac uk\ac\warwick\java\cs126\services\FollowerStore.java > %WORK_DIR%\FollowerStore.log 2>&1

IF NOT EXIST %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\FollowerStore.class (
    echo [ERROR]: FollowerStore hasn't compiled, check FollowerStore.log for details...
    cd %WORK_DIR%
    rmdir /s/q witter-tmp
    exit /B 1
)

IF NOT EXIST %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\UserStore.class (
    echo [ERROR]: UserStore hasn't compiled, check UserStore.log for details...
    cd %WORK_DIR%
    rmdir /s/q witter-tmp
    exit /B 1
)
 
IF NOT EXIST %WORK_DIR%\witter-tmp\WEB-INF\classes\uk\ac\warwick\java\cs126\services\WeetStore.class (
    echo [ERROR]: WeetStore hasn't compiled, check WeetStore.log for details...
    cd %WORK_DIR%
    rmdir /s/q witter-tmp 
    exit /B 1
)

echo [SUCCESS]: Files compiled!

cd %WORK_DIR%\witter-tmp\
mkdir %WORK_DIR%\witter-run\

copy %WORK_DIR%\witter-tmp\WEB-INF\classes\weets.csv %WORK_DIR%\witter-run\
copy %WORK_DIR%\witter-tmp\WEB-INF\classes\names.csv %WORK_DIR%\witter-run\
copy %WORK_DIR%\witter-tmp\WEB-INF\classes\followers.csv %WORK_DIR%\witter-run\

del %WORK_DIR%\witter-tmp\Witter.jar

jar cfM Witter-build.jar *

copy Witter-build.jar %WORK_DIR%\witter-run\
cd %WORK_DIR%\witter-run

echo [INFO]: Running the webapp, check out localhost:8080/Witter in your browser...
echo Use Control-C to stop the app. 
echo Don't worry if the page won't load straight away, be patient!
java -jar Witter-build.jar > NUL 2>&1

echo [INFO]: Cleaning up... 
cd %WORK_DIR%

rmdir /s/q witter-tmp
rmdir /q/s witter-run
del FollowerStore.log
del UserStore.log
del WeetStore.log


echo [SUCCESS]: Run complete!
