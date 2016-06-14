/**
 * WeetStore is implemented using three red black binary search trees. The first tree stores weets ordered by ID
 * for quick access to weets in the getWeet() method. The second tree is used to store weets as well, sorted by
 * date, being used to provide a fast way of carrying out operations that require getting weets sorted by date.
 * The third tree is used to store trending topics ordered lexicographically, allowing their number of occurences
 * to be easily modified for the getTrending() method.
 *
 * The memory complexity is O(n + m), where n is the number of weets, and m the number of trending topics.
 * However, 2n memory is in fact used to store weets, since they are stored twice.
 *
 * The time complexity for addWeet() is O(logn), since the insertion is done in binary search trees.
 * The time complexity for getWeet() is O(logn), since it implies retrieving an element from the first BST.
 * The time complexity for getWeets() is O(n), since all n weets are simply taken from the second BST, already in sorted order.
 * The time complexity for getWeetsByUser() is O(n), since all n weets have to be considered, and because they are
 * already sorted when taken from the second tree.
 * The time complexity for getWeetsContaining() is O(n), because of the same reasons as above.
 * The time complexity for getWeetsBefore() is O(n), because of the same reasons as above.
 * The time complexity for getWeetsOn() is O(n), because of the same reasons as above.
 * The time complexity for getTrending() is O(m*logm), since the trending topics must be retrieved from the BST (in O(m))
 * and then sorted (in O(m*logm)) using Quick Sort.
 *
 * The main advantage of using a red black binary search tree where weets are ordered by date is that it allows for easy
 * retrieval of weets, removing the need to sort them for every operation. So, even though insertions are done in O(logn),
 * getting weets is always done in O(n), which is better than inserting in O(1) and getting weets in O(nlogn) (what would
 * happen when using a hash table), since insertions are done only once, but retrievals are done many times.
 * Also, using another BST to store weets ordered by ID, while doubling the amount of memory required, has the advantage of
 * allowing quick lookups of weets by ID, in O(logn), instead of O(n) in the date-ordered tree. This is useful not only for
 * the getWeet() method, but also for the addWeet() method, which first requires a check to see if a weet can be added (done
 * with getWeet()).
 * Finally, the BST used to store trending topics and their frequencies allows quick access to them in order to
 * modify them in O(logn). However, the complexity for getTrending() will be O(m*logm), since the data must be sorted.
 *
 * While hash tables may have provided O(1) on average for getting weets by ID or trending topics, due to the fact that
 * the number of weets or trending topics can be extremely high, these hash tables would end up performing worse than BSTs,
 * or taking up unnecessarily much memory (for a large number of buckets). For example, if we assume that there will be at
 * most 1,000,000 trending topics, a desirable amount of buckets would be 5,0000 - 10,0000, a situation in which, supposing
 * that the hash function assures an even distribution, it would take around 10 - 20 steps to find an element. On the other
 * hand, using a BST, it would also take around 20 steps, but there will be no overheads regarding memory use.
 *
 * @author: 1504815
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.*; // used in pattern matching to find trending topics in a weet

public class WeetStore implements IWeetStore {
    /*
     * Red Black Binary Search Tree adapted from http://algs4.cs.princeton.edu/33balanced/RedBlackLiteBST.java.html;
     * used to store weets ordered by ID and perform the getWeet() operation;
     */
    class WeetRedBlackBST {
        // BST helper node data type
        class Node {
            private Weet weet; // weet associated with the node; contains the key as well (its ID)
            private Node left, right; // links to left and right subtrees
            private boolean colour; // colour of parent link

            public Node(Weet weet, boolean colour) {
                this.weet = weet;
                this.colour = colour;
            }
        }

        // constants used for "colouring" the tree nodes
        private static final boolean RED = true;
        private static final boolean BLACK = false;

        private Node root; // root of the BST
        private int nodeCount; // number of weets in BST

        // basic constructor
        public WeetRedBlackBST() {
            root = null;
            nodeCount = 0;
        }

        // insert a new weet in the tree
        public void add(Weet weet) {
            root = insert(root, weet);
            root.colour = BLACK;
        }

        /*
         * used in the addWeet() method to add a weet in the given node's subtree - it is assumed that two weets with the same ID
         * will never be added, so the method doesn't check for duplicates (this is done in the addWeet() method of WeetStore)
         */
        private Node insert(Node h, Weet weet) {
            // if a null node has been reached, add the new weet there
            if (h == null) {
                nodeCount++;
                return new Node(weet, RED);
            }

            // smaller IDs go in the left subtree, greater IDs go in the right subtree
            if (weet.getId() < h.weet.getId()) {
                h.left = insert(h.left, weet);
            }
            else {
                h.right = insert(h.right, weet);
            }

            // balance the tree and correctly set colours
            if (isRed(h.right) && !isRed(h.left)) {
                h = rotateLeft(h);
            }
            if (isRed(h.left) && isRed(h.left.left)) {
                h = rotateRight(h);
            }
            if (isRed(h.left) && isRed(h.right)) {
                flipColours(h);
            }

            return h;
        }

        // check if Node x is red
        private boolean isRed(Node x) {
            if (x == null) {
                return false;
            }
            return x.colour == RED;
        }

        // rotate right to balance tree
        private Node rotateRight(Node h) {
            Node x = h.left;

            h.left = x.right;
            x.right = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // rotate left to balance tree
        private Node rotateLeft(Node h) {
            Node x = h.right;

            h.right = x.left;
            x.left = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // flip the colours of the node and its children; called only when node is black and children are red
        private void flipColours(Node h) {
            h.colour = RED;
            h.left.colour = BLACK;
            h.right.colour = BLACK;
        }

        // return the weet with the given ID, or null if it doesn't exist
        public Weet getWeetById(int id) {
            return getWeetById(root, id);
        }

        // search in the subtree given by node h for a weet with the given ID
        public Weet getWeetById(Node h, int id) {
            // if nothing was found, return null
            if (h == null) {
                return null;
            }
            // compare the ID to the current node's ID to determinte what to do
            if (id == h.weet.getId()) {
                return h.weet;
            }
            else if (id < h.weet.getId()) {
                return getWeetById(h.left, id);
            }
            return getWeetById(h.right, id);
        }
    }

    /*
     * Red Black Binary Search Tree adapted from http://algs4.cs.princeton.edu/33balanced/RedBlackLiteBST.java.html;
     * used to store weets and perform all operations that require weets returned in date order;
     */
    class DateRedBlackBST {
        // BST helper node data type
        class Node {
            private Weet weet; // weet associated with the node; contains the key as well (its ID)
            private Node left, right; // links to left and right subtrees
            private boolean colour; // colour of parent link

            public Node(Weet weet, boolean colour) {
                this.weet = weet;
                this.colour = colour;
            }
        }

        // constants used for "colouring" the tree nodes
        private static final boolean RED = true;
        private static final boolean BLACK = false;

        private Node root; // root of the BST
        private int nodeCount; // number of weets in BST

        // basic constructor
        public DateRedBlackBST() {
            root = null;
            nodeCount = 0;
        }

        // insert a new weet in the tree
        public void add(Weet weet) {
            root = insert(root, weet);
            root.colour = BLACK;
        }

        // used in the add method to add a weet in the given node's subtree
        private Node insert(Node h, Weet weet) {
            // if a null node has been reached, add the new weet there
            if (h == null) {
                nodeCount++;
                return new Node(weet, RED);
            }

            int comparison = weet.getDateWeeted().compareTo(h.weet.getDateWeeted());

            // earlier or equal dates go in the left subtree, later dates go in the right subtree
            if (comparison <= 0) {
                h.left = insert(h.left, weet);
            }
            else {
                h.right = insert(h.right, weet);
            }

            // balance the tree and correctly set colours
            if (isRed(h.right) && !isRed(h.left)) {
                h = rotateLeft(h);
            }
            if (isRed(h.left) && isRed(h.left.left)) {
                h = rotateRight(h);
            }
            if (isRed(h.left) && isRed(h.right)) {
                flipColours(h);
            }

            return h;
        }

        // check if Node x is red
        private boolean isRed(Node x) {
            if (x == null) {
                return false;
            }
            return x.colour == RED;
        }

        // rotate right to balance tree
        private Node rotateRight(Node h) {
            Node x = h.left;

            h.left = x.right;
            x.right = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // rotate left to balance tree
        private Node rotateLeft(Node h) {
            Node x = h.right;

            h.right = x.left;
            x.left = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // flip the colours of the node and its children; called only when node is black and children are red
        private void flipColours(Node h) {
            h.colour = RED;
            h.left.colour = BLACK;
            h.right.colour = BLACK;
        }

        /*
         * The following method pairs use an in-order traversal from right to left in order to return
         * lists of weets sorted by date, starting with the most recent. Each method is used by
         * a corresponding method from WeetStore. For each of these method pairs, the first one does
         * the initialisations and the array return, while the second recursively goes through all
         * nodes in the tree and adds the weets that correspond to the given criteria. Although these
         * methods could have been condensed in fewer methods with more filters, where some filters
         * would be null, depending on the WeetStore method called, this approach of using specialised
         * methods ensures better time performance, albeit more similar methods are written.
         */

        // return a list of all weets
        public Weet[] getWeetList() {
            Weet[] weetList = new Weet[nodeCount];
            int[] counter = {0}; // counter used as an array to be modified in subsequent method calls

            getWeetList(root, weetList, counter);
            if (counter[0] == 0) {
                return null;
            }
            return weetList;
        }

        private void getWeetList(Node x, Weet[] weetList, int[] counter) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getWeetList(x.right, weetList, counter); // go to the right subtree
            weetList[counter[0]++] = x.weet; // add the current node's weet
            getWeetList(x.left, weetList, counter); // go to the left subtree
        }

        // return a list of all weets belonging to the user with the given ID
        public Weet[] getWeetListByUserId(int uid) {
            // the weets will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList weetList = new LinkedList();

            getWeetListByUserId(root, weetList, uid);
            if (weetList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            Weet[] weetArray = new Weet[weetList.size()];
            ListElement temp = weetList.getHead();
            int counter = weetList.size();

            while (temp != null) {
                weetArray[--counter] = temp.getWeet();
                temp = temp.getNext();
            }
            return weetArray;
        }

        private void getWeetListByUserId(Node x, LinkedList weetList, int uid) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getWeetListByUserId(x.right, weetList, uid); // go to the right subtree
            // if the current weet belongs to the user with the given ID, add it to the list
            if (x.weet.getUserId() == uid) {
                weetList.add(x.weet);
            }
            getWeetListByUserId(x.left, weetList, uid); // go to the left subtree
        }

        // return the list of all weets containing the given query string
        public Weet[] getWeetListByQueryString(String query) {
            // the weets will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList weetList = new LinkedList();

            getWeetListByQueryString(root, weetList, query);
            if (weetList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            Weet[] weetArray = new Weet[weetList.size()];
            ListElement temp = weetList.getHead();
            int counter = weetList.size();

            while (temp != null) {
                weetArray[--counter] = temp.getWeet();
                temp = temp.getNext();
            }
            return weetArray;
        }

        private void getWeetListByQueryString(Node x, LinkedList weetList, String query) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getWeetListByQueryString(x.right, weetList, query); // go to the right subtree
            // if the current weet contains the given query string, add it to the list
            if (x.weet.getMessage().contains(query)) {
                weetList.add(x.weet);
            }
            getWeetListByQueryString(x.left, weetList, query); // go to the left subtree
        }

        // return the list of all weets posted on the given date
        private Weet[] getWeetListOnDate(Date date) {
            // the weets will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList weetList = new LinkedList();

            getWeetListOnDate(root, weetList, date);
            if (weetList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            Weet[] weetArray = new Weet[weetList.size()];
            ListElement temp = weetList.getHead();
            int counter = weetList.size();

            while (temp != null) {
                weetArray[--counter] = temp.getWeet();
                temp = temp.getNext();
            }
            return weetArray;
        }

        private void getWeetListOnDate(Node x, LinkedList weetList, Date date) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            int comparison = x.weet.getDateWeeted().compareTo(date);
            if (comparison >= 0) {
                // if the current node's date is equal to the given date, add the corresponding weet to the list
                if (comparison == 0) {
                    weetList.add(x.weet);
                }
                // since weets with equal dates to the current node's weet are stored in left subtree, go there
                getWeetListOnDate(x.left, weetList, date);
            }
            // if the current node's date is less than the given date, go to the right subtree, where greater dates are found
            else if (comparison < 0) {
                getWeetListOnDate(x.right, weetList, date);
            }
        }

        // return the list of all weets posted before the given date
        private Weet[] getWeetListBeforeDate(Date date) {
            // the weets will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList weetList = new LinkedList();

            getWeetListBeforeDate(root, weetList, date);
            if (weetList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            Weet[] weetArray = new Weet[weetList.size()];
            ListElement temp = weetList.getHead();
            int counter = weetList.size();

            while (temp != null) {
                weetArray[--counter] = temp.getWeet();
                temp = temp.getNext();
            }
            return weetArray;
        }

        private void getWeetListBeforeDate(Node x, LinkedList weetList, Date date) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getWeetListBeforeDate(x.right, weetList, date); // go to the right subtree
            // if the current weet was posted before or on the given date, add it to the list
            if (x.weet.getDateWeeted().compareTo(date) <= 0) {
                weetList.add(x.weet);
            }
            getWeetListBeforeDate(x.left, weetList, date); // go to the left subtree
        }
    }

    /*
     * Red Black Binary Search Tree adapted from http://algs4.cs.princeton.edu/33balanced/RedBlackLiteBST.java.html;
     * used to store trending topics ordered lexicographically and perform the getTrending() operation;
     */
    class TrendingRedBlackBST {
        // BST helper node data type
        class Node {
            private TrendingTopic trending; // trending topic associated with the node; contains the key as well (its text)
            private Node left, right; // links to left and right subtrees
            private boolean colour; // colour of parent link

            public Node(TrendingTopic trending, boolean colour) {
                this.trending = trending;
                this.colour = colour;
            }
        }

        // constants used for "colouring" the tree nodes
        private static final boolean RED = true;
        private static final boolean BLACK = false;

        private Node root; // root of the BST
        private int nodeCount; // number of trending topics in BST

        // basic constructor
        public TrendingRedBlackBST() {
            root = null;
            nodeCount = 0;
        }

        // insert a new trending topic in the tree
        public void add(TrendingTopic trending) {
            root = insert(root, trending);
            root.colour = BLACK;
        }

        /*
         * used in the addWeet() method to add a trending topic in the given node's subtree - it is assumed that two topics
         * with the same text will never be added, so the method doesn't check for duplicates (this is done in the addWeet()
         * method of WeetStore)
         */
        private Node insert(Node h, TrendingTopic trending) {
            // if a null node has been reached, add the new weet there
            if (h == null) {
                nodeCount++;
                return new Node(trending, RED);
            }

            int comparison = trending.getName().compareTo(h.trending.getName());

            // lexicographically smaller topics go in the left subtree, greater ones go in the right subtree
            if (comparison < 0) {
                h.left = insert(h.left, trending);
            }
            else {
                h.right = insert(h.right, trending);
            }

            // balance the tree and correctly set colours
            if (isRed(h.right) && !isRed(h.left)) {
                h = rotateLeft(h);
            }
            if (isRed(h.left) && isRed(h.left.left)) {
                h = rotateRight(h);
            }
            if (isRed(h.left) && isRed(h.right)) {
                flipColours(h);
            }

            return h;
        }

        // check if Node x is red
        private boolean isRed(Node x) {
            if (x == null) {
                return false;
            }
            return x.colour == RED;
        }

        // rotate right to balance tree
        private Node rotateRight(Node h) {
            Node x = h.left;

            h.left = x.right;
            x.right = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // rotate left to balance tree
        private Node rotateLeft(Node h) {
            Node x = h.right;

            h.right = x.left;
            x.left = h;
            x.colour = h.colour;
            h.colour = RED;

            return x;
        }

        // flip the colours of the node and its children; called only when node is black and children are red
        private void flipColours(Node h) {
            h.colour = RED;
            h.left.colour = BLACK;
            h.right.colour = BLACK;
        }

        // return the given trending topic, or null if it doesn't exist
        public TrendingTopic getTrendingTopic(String text) {
            return getTrendingTopic(root, text);
        }

        // search in the subtree given by node h for the given trending topic
        public TrendingTopic getTrendingTopic(Node h, String text) {
            // if nothing was found, return null
            if (h == null) {
                return null;
            }

            int comparison = text.compareTo(h.trending.getName());

            // compare the topic to the current node's topic to determine what to do
            if (comparison == 0) {
                return h.trending;
            }
            else if (comparison < 0) {
                return getTrendingTopic(h.left, text);
            }
            return getTrendingTopic(h.right, text);
        }

        // return a list of all trending topics
        public TrendingTopic[] getTrendingArray() {
            TrendingTopic[] trendingArray = new TrendingTopic[nodeCount];
            int[] counter = {0}; // counter used as an array to be modified in subsequent method calls

            getTrendingArray(root, trendingArray, counter);
            if (counter[0] == 0) {
                return null;
            }
            return trendingArray;
        }

        private void getTrendingArray(Node x, TrendingTopic[] trendingArray, int[] counter) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getTrendingArray(x.right, trendingArray, counter); // go to the right subtree
            trendingArray[counter[0]++] = x.trending; // add the current node's trending topic
            getTrendingArray(x.left, trendingArray, counter); // go to the left subtree
        }
    }

    // this class helps store Weet objects as elements of a Singly Linked List
    class ListElement {
        private Weet weet; // weet value of the list element
        private ListElement next; // link to the next element in the list

        // simple constructor that assigns a value to the list element
        public ListElement(Weet weet) {
            this.weet = weet;
        }

        // get the list element's weet
        public Weet getWeet() {
            return weet;
        }

        // get the next list element
        public ListElement getNext() {
            return next;
        }

        // set the next list element
        public void setNext(ListElement next) {
            this.next = next;
        }
    }

    /*
     * Singly Linked List of ListElement objects in which elements are added at the front; mainly
     * used when returning arrays, in order to allocate exactly as much memory as needed.
     */
    class LinkedList {
        private ListElement head; // head of the list
        private int size; // size of the list

        // empty list constructor
        public LinkedList() {
            head = null;
            size = 0;
        }

        // add element to the front
        public void add(Weet weet) {
            ListElement newElement = new ListElement(weet);

            newElement.setNext(head);
            head = newElement;
            size++;
        }

        // get the size of the list
        public int size() {
            return size;
        }

        // get the head of the list - useful when traversing the list
        public ListElement getHead() {
            return head;
        }
    }

    // class that helps store (String, int) pairs representing trending topics and their frequencies
    class TrendingTopic {
        String name; // name of the topic, without the '#' character
        int timesUsed; // number of times the topic has been used so far

        // simple constructor that initialises the name of the topic and the number of times it was used (once)
        public TrendingTopic(String name) {
            this.name = name;
            timesUsed = 1;
        }

        // get the topic's name
        public String getName() {
            return name;
        }

        // get the number of times the topic has been used so far
        public int getTimesUsed() {
            return timesUsed;
        }

        // increase the number of times the topic has been used so far
        public void increaseTimesUsed() {
            timesUsed++;
        }
    }

    private WeetRedBlackBST weetIdTree; // store weets in a binary search tree ordered by ID
    private DateRedBlackBST weetDateTree; // store weets in a binary search tree ordered by date
    private TrendingRedBlackBST trendingTree; // store trending topics in a binary search tree ordered lexicographically

    // initialise the three data structures
    public WeetStore() {
        weetIdTree = new WeetRedBlackBST();
        weetDateTree = new DateRedBlackBST();
        trendingTree = new TrendingRedBlackBST();
    }

    // add a weet to weetIdTree and weetDateTree
    public boolean addWeet(Weet weet) {
        // use weetSIdTree to check if a weet with the same ID already exists
        if (weetIdTree.getWeetById(weet.getId()) == null) {
            weetIdTree.add(weet);
            weetDateTree.add(weet);

            // use a regex to match trending topic patterns
            Pattern trendingPattern = Pattern.compile("#(\\w+|\\W+)");
            Matcher match = trendingPattern.matcher(weet.getMessage());

            // take all matches and add them to trendingTree, or increment the number of occurences
            while (match.find()) {
                String trendingTopic = match.group(1);
                TrendingTopic result = trendingTree.getTrendingTopic(trendingTopic);
                if (result == null) {
                    trendingTree.add(new TrendingTopic(trendingTopic));
                }
                else {
                    result.increaseTimesUsed();
                }
            }
            return true;
        }

        return false;
    }

    // get the weet with the given ID from weetIdTree, or null if it doesn't exist
    public Weet getWeet(int wid) {
        return weetIdTree.getWeetById(wid);
    }

    // get all weets, sorted by date, starting with the most recent, from weetDateTree
    public Weet[] getWeets() {
        return weetDateTree.getWeetList();
    }

    // get all weets by the user with the given ID, sorted by date, starting with the most recent, from weetDateTree
    public Weet[] getWeetsByUser(User usr) {
        Weet[] result = weetDateTree.getWeetListByUserId(usr.getId());
        Weet[] nullCase = {}; // value to return in case result is null, since Witter crashes if null is returned

        if (result == null) {
            return nullCase;
        }
        return result;
    }

    // get all weets containing the query string, sorted by date, starting with the most recent, from weetDateTree
    public Weet[] getWeetsContaining(String query) {
        // prevent crash if query string is null
        if (query == null) {
            return null;
        }
        return weetDateTree.getWeetListByQueryString(query);
    }

    // get all weets on the given date from weetDateTree
    public Weet[] getWeetsOn(Date dateOn) {
        // prevent crash if date is null
        if (dateOn == null) {
            return null;
        }
        return weetDateTree.getWeetListOnDate(dateOn);
    }

    // get all weets before or on a given date, sorted by date, starting with the most recent, from weetDateTree
    public Weet[] getWeetsBefore(Date dateBefore) {
        // prevent crash if date is null
        if (dateBefore == null) {
            return null;
        }
        return weetDateTree.getWeetListBeforeDate(dateBefore);
    }

    // get the top ten trending topics, sorted by number of occurences, from trendingTree
    public String[] getTrending() {
        TrendingTopic[] trendingArray = trendingTree.getTrendingArray();
        String[] topTrending = new String[10];
        int minimumLength = 10;

        if (trendingArray == null) {
            minimumLength = 0;
        }
        else {
            if (trendingArray.length < 10) {
                minimumLength = trendingArray.length;
            }
            quickSort(trendingArray, 0, trendingArray.length - 1);
        }

        for (int i = 0 ; i < minimumLength; i++) {
            topTrending[i] = "#" + trendingArray[i].getName();
        }
        for (int i = minimumLength; i < 10; i++) {
            topTrending[i] = null;
        }

        return topTrending;
    }

    // quicksort algorithm; taken and adapted from http://www.algolist.net/Algorithms/Sorting/Quicksort
    private void quickSort(TrendingTopic[] list, int left, int right) {
        int index = partition(list, left, right);

        if (left < index - 1) {
            quickSort(list, left, index - 1);
        }
        if (index < right) {
            quickSort(list, index, right);
        }
    }

    private int partition(TrendingTopic[] list, int left, int right) {
        int i = left, j = right;
        TrendingTopic temp;
        TrendingTopic pivot = list[(left + right) / 2];

        while (i <= j) {
            while (list[i].getTimesUsed() > pivot.getTimesUsed()) {
                i++;
            }
            while (list[j].getTimesUsed() < pivot.getTimesUsed()) {
                j--;
            }
            if (i <= j) {
                temp = list[i];
                list[i++] = list[j];
                list[j--] = temp;
            }
        }
        return i;
    }
}
