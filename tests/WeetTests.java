import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import uk.ac.warwick.java.cs126.services.IWeetStore;
import uk.ac.warwick.java.cs126.services.WeetStore;
import uk.ac.warwick.java.cs126.models.Weet;

class WeetTests {
    

    /*
     * Tests the ability to add a weet
     * @return Returns true is the test passed, false is it failed
     */
    protected boolean testAddWeet() 
    {
        // Create new Weet Store
        IWeetStore weetStore = new WeetStore();

        // Generate Weet to add
        Weet weet = new Weet(1, 1, "Hello World!", createDate("02/11/2012 23:11"));

        // Issue the command, suitably storing the return value
        boolean result = weetStore.addWeet( weet );

        // Check the return value for the expected result
        if (result == true) // Here we expect addWeet to return true if it worked
        {
            return true;
        }
        else 
        { 
            return false;
        }
    }

    /*
     * Tests the ability to retrieve a weet
     * @return Returns true is the test passed, false is it failed
     */
    protected boolean testGetWeetSuccess()
    {
        // Create new Weet Store
        IWeetStore weetStore = new WeetStore();

        // Generate Weet to add
        int weetId = 1;
        Weet weet = new Weet(weetId, 1, "Hello World!", createDate("02/11/2012 23:11"));

        // Add that weet
        weetStore.addWeet( weet );

        // Issue the command, suitably storing the return value
        Weet returnedWeet = weetStore.getWeet(weetId);
        
        // Check the return value for the expected result
        if (returnedWeet == weet) // We expect the Weet we get back to be the same as the one put in 
        {
            return true;
        }
        else 
        { 
            return false;
        }

    }

    /*
     * Tests what happens when an invalid Weet is searched for 
     * @return Returns true is the test passed, false is it failed
     */
    protected boolean testGetWeetFail()
    {
        // Create new Weet Store
        IWeetStore weetStore = new WeetStore();

        int weetId = 2;
        // Try retrieve a Weet that won't be in the store 
        Weet returnedWeet = weetStore.getWeet(weetId);
        
        // Check the return value for the expected result
        if (returnedWeet == null) // We expect it to return null 
        {
            return true;
        }
        else 
        { 
            return false;
        }
    }

    /*
     * Returns a Date based on the input string
     * @param inputString Takes a string of form dd/MM/yy hour:minute. Example: 01/03/12 18:00
     * @return Returns the create Date
     */
    private Date createDate(String inputString) // This method is useful for creating dates quickly, where can be good for testing
    {
        try {
            // Take input string and create date
            DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy H:m");
            return dateFormatter.parse(inputString);
        }
        catch (ParseException pe) 
        {
            // Bad input string
            System.out.println("Couldn't parse " + inputString);
            return new Date();
        }
    }
}
