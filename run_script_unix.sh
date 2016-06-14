WORK_DIR=`pwd`

echo "[INFO]:  Setting up temporary directories..."

mkdir $WORK_DIR/witter-tmp

cp Witter.jar $WORK_DIR/witter-tmp/

cd $WORK_DIR/witter-tmp

jar xf Witter.jar

cd $WORK_DIR

cp UserStore.java $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/

cp WeetStore.java $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/

cp FollowerStore.java $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/

rm $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/WeetStore.class
rm $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/UserStore.class
rm $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/FollowerStore.class

cd $WORK_DIR/witter-tmp/WEB-INF/classes/ 

echo "[SUCCESS]:  Directories set up!"
echo "[INFO]:  Compiling your files..."

javac uk/ac/warwick/java/cs126/services/UserStore.java &> $WORK_DIR/UserStore.log
javac uk/ac/warwick/java/cs126/services/WeetStore.java &> $WORK_DIR/WeetStore.log
javac uk/ac/warwick/java/cs126/services/FollowerStore.java &> $WORK_DIR/FollowerStore.log

if [ ! -f $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/FollowerStore.class ]
then
    echo "[ERROR]:  FollowerStore hasn't compiled, check FollowerStore.log for details..."
    cd $WORK_DIR
    rm -rf witter-tmp
    exit 1
fi
if [ ! -f $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/UserStore.class ]
then
    echo "[ERROR]:  UserStore hasn't compiled, check UserStore.log for details..."
    cd $WORK_DIR
    rm -rf witter-tmp
    exit 1
fi 
if [ ! -f $WORK_DIR/witter-tmp/WEB-INF/classes/uk/ac/warwick/java/cs126/services/WeetStore.class ]
then
    echo "[ERROR]:  WeetStore hasn't compiled, check WeetStore.log for details..."
    cd $WORK_DIR
    rm -rf witter-tmp 
    exit 1
fi 

echo "[SUCCESS]:  Files compiled!"

cd $WORK_DIR/witter-tmp/
mkdir $WORK_DIR/witter-run/

cp $WORK_DIR/witter-tmp/WEB-INF/classes/weets.csv $WORK_DIR/witter-run/
cp $WORK_DIR/witter-tmp/WEB-INF/classes/names.csv $WORK_DIR/witter-run/
cp $WORK_DIR/witter-tmp/WEB-INF/classes/followers.csv $WORK_DIR/witter-run/

rm $WORK_DIR/witter-tmp/Witter.jar


jar cfM Witter-build.jar *

cp Witter-build.jar $WORK_DIR/witter-run/
cd $WORK_DIR/witter-run

echo "[INFO]:  Running the webapp, check out 0.0.0.0:8080/witter in your browser,"
echo "         Replace 8080 with the value you passed to the script if needed."
echo "  Use Control-C to stop the app."
echo "Don't worry if the page won't load straight away, be patient!"

if [ "$#" -ne 1 ]; then
    echo "[INFO]: Trying to run on port 8080"
    java -jar Witter-build.jar > /dev/null
elif [ "$#" == 1 ]; then
    echo "[INFO]: Trying to run on port $1"
    java -Djetty.port="$1" -jar Witter-build.jar > /dev/null
fi 

echo ""
echo "[INFO]:  Cleaning up..."
cd $WORK_DIR

rm -rf witter-tmp
rm -rf witter-run
rm *.log

echo "[SUCCESS]:  Run complete!"
