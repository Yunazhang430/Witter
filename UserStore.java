/**
 * UserStore is implemented using two red black binary search trees (BST), both of them storing users but in
 * different orders. The first tree orders users by their IDs, and it is used for the getUser() method,
 * which is called whenever adding a new user in order to check whether a specific ID is already in use. The second
 * tree stores users ordered by their join date, and is used in all the other UserStore methods, since it
 * allows users to be easily retrieved sorted by date, providing a good time complexity for these operations.
 *
 * The memory complexity is O(n). More precisely, each BST has O(n) memory complexity, so 2n memory is used.
 *
 * The time complexity for addUser() is O(logn), since it is O(logn) for each BST.
 * The time complexity for getUser() is O(logn) as well, due to the fact that users are searched for in the first BST by ID.
 * The time complexity for getUsers() is O(n), since all n users are simply taken from the second tree, already sorted.
 * The time complexity for getUsersContaining() is O(n), since all n users have to be checked, and because they are
 * already sorted by date when taken from the second tree.
 * The time complexity for getUsersJoinedBefore() is O(n), because of the same reasons as above. It should be noted that while
 * the second BST is ordered by date, the date doesn't really function as a key, since there can be more users with the
 * same join date. Thus, a binary search for a specific date would be risky, since some required dates could be omitted.
 * Due to the usage of red black trees, all time complexities are the same, both in worst and in best case.
 *
 * The main advantage of using a red black binary search tree is that it allows for easy retrieval of users sorted by
 * date, removing the need to sort them for every operation. So, even though insertions are done in O(logn), getting
 * users is always done in O(n), which is better than inserting in O(1) and getting users in O(nlogn) (what would
 * happen when using a hash table), since insertions are done only once, but retrievals are done many times.
 * Also, the use of a second tree to store users, while doubling the amount of memory required, has the advantage of
 * allowing quick lookups of users by ID, in O(logn). This is useful not only for the getUser() method, which is done in
 * O(logn) instead of O(n) (as it would be in a BST ordered by date), but also for the addUser() method, which first
 * requires a check to see if a user can be added. This reduces the complexity of addUser() from O(n) to O(logn). While
 * hash tables could have provided a time complexity of O(1) for roughly the same amount of memory used, they would also
 * have a worst case performance of O(n), which could only be avoided using a very large number of buckets. By using a
 * BST instead, it is ensured that only as much memory as needed is used, and the getUser() operation still scales well.
 *
 * @author: 1504815
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;

public class UserStore implements IUserStore {
    /*
     * Red Black Binary Search Tree adapted from http://algs4.cs.princeton.edu/33balanced/RedBlackLiteBST.java.html;
     * used to store users ordered by ID and perform the getUser() operation;
     */
    class UserRedBlackBST {
        // BST helper node data type
        class Node {
            private User user; // user associated with the node; contains the key as well (its ID)
            private Node left, right; // links to left and right subtrees
            private boolean colour; // colour of parent link

            public Node(User user, boolean colour) {
                this.user = user;
                this.colour = colour;
            }
        }

        // constants used for "colouring" the tree nodes
        private static final boolean RED = true;
        private static final boolean BLACK = false;

        private Node root; // root of the BST
        private int nodeCount; // number of users in BST

        // basic constructor
        public UserRedBlackBST() {
            root = null;
            nodeCount = 0;
        }

        // insert a new user in the tree
        public void add(User user) {
            root = insert(root, user);
            root.colour = BLACK;
        }

        /*
         * used in the add method to add a user in the given node's subtree - it is assumed that two users with the same ID
         * will never be added, so the method doesn't check for duplicates (this is done in the addUser() method of UserStore)
         */
        private Node insert(Node h, User user) {
            // if a null node has been reached, add the new user there
            if (h == null) {
                nodeCount++;
                return new Node(user, RED);
            }

            // smaller IDs go in the left subtree, greater IDs go in the right subtree
            if (user.getId() < h.user.getId()) {
                h.left = insert(h.left, user);
            }
            else {
                h.right = insert(h.right, user);
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

        // return the user with the given ID, or null if it doesn't exist
        public User getUserById(int id) {
            return getUserById(root, id);
        }

        // search in the subtree given by node h for a user with the given ID
        public User getUserById(Node h, int id) {
            // if nothing was found, return null
            if (h == null) {
                return null;
            }
            // compare the ID to the current node's ID to determinte what to do
            if (id == h.user.getId()) {
                return h.user;
            }
            else if (id < h.user.getId()) {
                return getUserById(h.left, id);
            }
            return getUserById(h.right, id);
        }
    }

    /*
     * Red Black Binary Search Tree adapted from http://algs4.cs.princeton.edu/33balanced/RedBlackLiteBST.java.html;
     * used to store users ordered by date and perform all operations that require users returned in date order;
     */
    class DateRedBlackBST {
        // BST helper node data type
        class Node {
            private User user; // user associated with the node; contains the key as well (its ID)
            private Node left, right; // links to left and right subtrees
            private boolean colour; // colour of parent link

            public Node(User user, boolean colour) {
                this.user = user;
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

        // insert a new user in the tree
        public void add(User user) {
            root = insert(root, user);
            root.colour = BLACK;
        }

        // used in the add method to add a user in the given node's subtree
        private Node insert(Node h, User user) {
            // if a null node has been reached, add the new user there
            if (h == null) {
                nodeCount++;
                return new Node(user, RED);
            }

            int comparison = user.getDateJoined().compareTo(h.user.getDateJoined());

            // earlier or equal dates go in the left subtree, later dates go in the right subtree
            if (comparison <= 0) {
                h.left = insert(h.left, user);
            }
            else {
                h.right = insert(h.right, user);
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
         * lists of users sorted by date, starting with the most recent. Each method is used by
         * a corresponding method from UserStore. For each of these method pairs, the first one does
         * the initialisations and the array return, while the second recursively goes through all
         * nodes in the tree and adds the users that correspond to the given criteria. Although these
         * methods could have been condensed in fewer methods with more filters, where some filters
         * would be null, depending on the UserStore method called, this approach of using specialised
         * methods ensures better time performance, albeit more similar methods are written.
         */

        // return a list of all users
        public User[] getUserList() {
            User[] userList = new User[nodeCount];
            int[] counter = {0}; // counter used as an array to be modified in subsequent method calls

            getUserList(root, userList, counter);
            if (counter[0] == 0) {
                return null;
            }
            return userList;
        }

        private void getUserList(Node x, User[] userList, int[] counter) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getUserList(x.right, userList, counter); // go to the right subtree
            userList[counter[0]++] = x.user; // add the current node's user
            getUserList(x.left, userList, counter); // go to the left subtree
        }

        // return the list of all users whose names contain the given query string
        public User[] getUserListByQueryString(String query) {
            // the users will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList userList = new LinkedList();

            getUserListByQueryString(root, userList, query);
            if (userList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            User[] userArray = new User[userList.size()];
            ListElement temp = userList.getHead();
            int counter = userList.size();

            while (temp != null) {
                userArray[--counter] = temp.getUser();
                temp = temp.getNext();
            }
            return userArray;
        }

        private void getUserListByQueryString(Node x, LinkedList userList, String query) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getUserListByQueryString(x.right, userList, query); // go to the right subtree
            // if the current user contains the given query string, add it to the list
            if (x.user.getName().contains(query)) {
                userList.add(x.user);
            }
            getUserListByQueryString(x.left, userList, query); // go to the left subtree
        }

        // return the list of all users who joined before the given date
        private User[] getUserListBeforeDate(Date date) {
            // the users will be added in a linked list first, since it is unknown how many of them there will be
            LinkedList userList = new LinkedList();

            getUserListBeforeDate(root, userList, date);
            if (userList.size() == 0) {
                return null;
            }
            // copy the list elements into an array and return it
            User[] userArray = new User[userList.size()];
            ListElement temp = userList.getHead();
            int counter = userList.size();

            while (temp != null) {
                userArray[--counter] = temp.getUser();
                temp = temp.getNext();
            }
            return userArray;
        }

        private void getUserListBeforeDate(Node x, LinkedList userList, Date date) {
            // if the node is null, go back
            if (x == null) {
                return;
            }

            getUserListBeforeDate(x.right, userList, date); // go to the right subtree
            // if the current user has joined before or on the given date, add it to the list
            if (x.user.getDateJoined().compareTo(date) <= 0) {
                userList.add(x.user);
            }
            getUserListBeforeDate(x.left, userList, date); // go to the left subtree
        }
    }

    // this class helps store User objects as elements of a Singly Linked List
    class ListElement {
        private User user; // user value of the list element
        private ListElement next; // link to the next element in the list

        // simple constructor that assigns a value to the list element
        public ListElement(User user) {
            this.user = user;
        }

        // get the list element's user
        public User getUser() {
            return user;
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
        public void add(User user) {
            ListElement newElement = new ListElement(user);

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

    private UserRedBlackBST userIdTree; // store users in a binary search tree ordered by ID
    private DateRedBlackBST userDateTree; // store users in a binary search tree ordered by join date

    // initialise the two trees
    public UserStore() {
        userDateTree = new DateRedBlackBST();
        userIdTree = new UserRedBlackBST();
    }

    // add a new user if its ID isn't already in use, to both trees
    public boolean addUser(User usr) {
        if (userIdTree.getUserById(usr.getId()) == null) {
            userIdTree.add(usr);
            userDateTree.add(usr);
            return true;
        }
        return false;
    }

    // return the User that has the given ID
    public User getUser(int uid) {
        return userIdTree.getUserById(uid);
    }

    // return an array of users sorted descending by join date
    public User[] getUsers() {
        return userDateTree.getUserList();
    }

    // return an array of users whose names contain the given String
    public User[] getUsersContaining(String query) {
        // prevent crash if query string is null
        if (query == null) {
            return null;
        }
        return userDateTree.getUserListByQueryString(query);
    }

    // return an array of users who joined before or on the given Date
    public User[] getUsersJoinedBefore(Date dateBefore) {
        return userDateTree.getUserListBeforeDate(dateBefore);
    }
}
