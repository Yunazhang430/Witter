public class TestRunner 
{

    public static void main(String[] args)
    {


        // Weet Tests
        WeetTests e = new WeetTests();
        System.out.println("[Testing Weets]");

        // Call our testAddWeet method in order to ensure that a weet is added succesfully
        System.out.print("--> testAddWeet : \t"); 
        boolean testAddWeet = e.testAddWeet();
        if (testAddWeet == true) {
            System.out.println("...success");
        }
        else {
            System.out.println("...fail.");
        }


        // Call our get weet fail method, remebering to test all possible outcomes of a method 
        System.out.print("--> testGetWeetFail : \t");
        boolean testGetWeetFail = e.testGetWeetFail();
        if (testGetWeetFail == true) {
            System.out.println("...success");
        }
        else {
            System.out.println("...fail.");
        }

        // Call our get weet success method, remebering to test all possible outcomes of a method 
        System.out.print("--> testGetWeetSuccess : \t");
        boolean testGetWeetSuccess = e.testGetWeetSuccess();
        if (testGetWeetSuccess == true) {
            System.out.println("...success");
        }
        else {
            System.out.println("...fail.");
        }


        // TODO: Test remaining IWeetStore methods (pass and fail).
        //
            System.out.println("...I need to add more tests to the WeetStore");
        

        // User Tests
        // TODO: Test IUserStore methods (pass and fail).
            System.out.println("--> I need to make and complete UserTests.java");


        // Follower Tests
        // TODO: Test IFollowerStore methods (pass and fail).
            System.out.println("--> I need to make and complete FollowerTests.java");
    
    }

}
