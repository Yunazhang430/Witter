# Building Custom Tester

This is the best, quickest and most reliable way to test your Witter solution. Simply running the website is not enough.

Copy your stores (WeetStore.java / UserStore.java / FollowerStore.java) to ./uk/ac/warwick/java/cs126/services/

Compile the incomplete version of the tests using:

    javac -cp witter-models.jar:. WeetTests.java
    javac -cp witter-models.jar:. TestRunner.java
    (note you may also need to re-complile your other java files, and may find it easier to reference *.java etc)
    
Then Run it using: 

    java -cp witter-models.jar:. TestRunner

You will then need to make files for both UserTests.java and FollwerTests.java, you can base these on WeetTests.java. 

You will need to add custom tests to all 3 files. 

You then need to extend the functionality of TestRunner.java, to use these files. 

Once you have added a good range of tests, this should be an easy and quick way to guarantee good functionality marks.

Good Luck and Happy Testing!

NB: java.lang.NoClassDefFoundError: TestRunner normally indicated lack of (re)compiling all required files.
