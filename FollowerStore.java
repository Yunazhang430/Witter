/**
 * FollowerStore is implemented using graphs based on hash tables of adjacency lists and sets based on hash tables.
 * The hash maps use the users' IDs as keys. The hashing is done using the remainder of the user's ID divided by
 * the table's size. Two directed graphs are used, one where an edge between A and B means that B is A's follower,
 * and another one where an edge between A and B means that B is followed by A. While these relationships could
 * have easily been represented using a single graph, the advantage of this approach, while taking twice as much
 * memory, is that several methods, such as getFollows(), have their time complexities reduced from quadratic to
 * linear. Also, sets based on hash tables are used for the getMutualFollows() and getMutualFollowers() operations,
 * since they provide a better time complexity (linear instead of quadratic).
 *
 * The worst-case memory complexity is O(n^2), due to the usage of the graph structures. More precisely, 2n^2 memory is
 * used for the graphs in the worst case, and 2n for the sets. However, on average, since using adjacency lists for the
 * graphs, the memory complexity is much better!
 *
 * The average time complexity for addFollower() is O(1), due to the usage of a hash table to find the node where the edge must
 * be added. Worst case O(n).
 * The average time complexity for getFollowers() is O(k*logk), where k is the number of followers that must be retrieved (also,
 * same remark as above). This is because the array also has to be sorted. Worst case O(n*logn);
 * The average time complexity for getFollows() is O(k*logk) (same reasons as above). Worst case O(n*logn);
 * The average time complexity for isAFollower() is O(k) (accessing the node in O(1) and searching for the required follower
 * in O(k)). Worst case O(n);
 * The time complexity for getNumFollowers() is O(1), since the numbers of followers for all nodes are stored in the hash tables.
 * The time complexity for getMutualFollowers() is O(n + m + k*logk), where n is the number of followers of the first user,
 * m is the number of followers of the second user and k is the number of common followers of the two users. This is because
 * the intersection has a complexity of O(n + m), and the sorting of the resulting set is done in O(k*logk);
 * The time complexity for getMutualFollows() is O(n + m + k*logk) (same reasons as above).
 * The time complexity for getTopUsers() is O(nlogn), since information about all n users must be retrived and sorted.
 *
 * The main advantage of using a graph to represent the following relations is that insertions and retrievals become
 * trivial: locate the node and add to the corresponding list a new element, or retrieve the entire list. Using a hash
 * map for the list of nodes instead of a linked list has the advantage of allowing access to a specific node in O(1)
 * instead of O(n) on average, with little effect on the memory used. The usage of two graphs instead of one, while costly,
 * allows for better time complexity of both relationships operations. If, for example, a single graph were to be used,
 * where an edge between A and B meant that A is followed by B, then getFollows() and getMutualFollows() would require
 * searching through the entire graph, having quadratic complexity. The usage of hash map sets for the getMutualFollows()
 * and getMutualFollowers() methods gives a linear complexity for searching, instead of the quadratic complexity that
 * would have been required by the usage of lists.
 *
 * @author: 1504815
*/

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {
    // this class allows the creation of pairs of two (not necessarily different) types of objects
    class Pair<F, S> {
        private F first;
        private S second;

        // basic constructor that initialises the components
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        // retrieve the first object
        public F getFirst() {
            return first;
        }

        // retrieve the second object
        public S getSecond() {
            return second;
        }

        // set the value of the first object
        public void setFirst(F newFirst) {
            this.first = newFirst;
        }

        // set the value of the second object
        public void setSecond(S newSecond) {
            this.second = newSecond;
        }
    }

    // this class helps store objects as elements of a Singly Linked List
    class ListElement<E> {
        private E value; // object value of the list element
        private ListElement<E> next; // link to the next element in the list

        // simple constructor that assigns a value to the list element
        public ListElement(E value) {
            this.value = value;
        }

        // get the list element's value
        public E getValue() {
            return value;
        }

        // get the next list element
        public ListElement<E> getNext() {
            return next;
        }

        // set the next list element
        public void setNext(ListElement<E> next) {
            this.next = next;
        }
    }

    // Singly Linked List of ListElement objects in which elements are added at the front
    class LinkedList<E> {
        private ListElement<E> head; // head of the list
        private int size; // size of the list

        // empty list constructor
        public LinkedList() {
            head = null;
            size = 0;
        }

        // add element to the front
        public void add(E value) {
            ListElement<E> newElement = new ListElement<>(value);

            newElement.setNext(head);
            head = newElement;
            size++;
        }

        // get the size of the list
        public int size() {
            return size;
        }

        // get the head of the list - useful when traversing the list
        public ListElement<E> getHead() {
            return head;
        }
    }

    /*
     * Hash map that stores relationship lists for every user to allow easy access to them;
     * effectively a graph whose nodes (and subsequently their adjacency lists) are accessed through a hash table
     */
    class idHashMap {
        // array of lists (buckets) that contain IDs, along with their relationships (as a list) and relationship count
        private LinkedList<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>>[] table;

        /*
         * Simple constructor that initialises the table with 997 buckets (prime number);
         * given a large number of users, say 100,000, this table size ensures that, on
         * average, each bucket will contain around 100 elements, reasonable enough to
         * allow quick access.
         */
        public idHashMap() {
            table = new LinkedList[997];
            initialiseTable();
        }

        // initalise each table entry with an empty linked list
        private void initialiseTable() {
            for(int i = 0; i < table.length; i++) {
                table[i] = new LinkedList<>();
            }
        }

        // return the head of the list of relationships of the user with the given ID
        public ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> getNode(int id) {
            int location = id % table.length; // get the ID's location in the hash table
            ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> temp = table[location].getHead();

            // go through the list until the ID is found
            while (temp != null) {
                if (temp.getValue().getFirst().getFirst() == id) {
                    return temp;
                }
                temp = temp.getNext();
            }

            // if this point is reached, then the ID has not been found
            return null;
        }

        // return the relationship between the two IDs, or null if it doesn't exist
        public ListElement<Pair<Integer, Date>> getRelationship(int id1, int id2) {
            ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> relationshipList = getNode(id1);

            if (relationshipList == null) {
                return null;
            }

            ListElement<Pair<Integer, Date>> temp = relationshipList.getValue().getSecond().getHead();

            while (temp != null) {
                if (temp.getValue().getFirst() == id2) {
                    return temp;
                }
                temp = temp.getNext();
            }

            return null;
        }

        // return an array of all IDs
        public int[] getIdArray() {
            LinkedList<Integer> idList = new LinkedList<>();
            int[] idArray;

            // store all IDs in a list
            for (int i = 0; i < table.length; i++) {
                ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> temp = table[i].getHead();

                while (temp != null) {
                    idList.add(temp.getValue().getFirst().getFirst());
                    temp = temp.getNext();
                }
            }

            if (idList.size() == 0) {
                return null;
            }

            // convert to array
            idArray = new int[idList.size()];

            ListElement<Integer> temp = idList.getHead();
            int i = 0;

            while (temp != null) {
                idArray[i++] = temp.getValue();
                temp = temp.getNext();
            }

            return idArray;
        }

        // add a node to the list; the node's position is determined through its user ID
        public void addNode(int id) {
            int location = id % table.length;
            Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>> value = new Pair<>(new Pair<>(id, 0), new LinkedList<Pair<Integer, Date>>());

            table[location].add(value);
        }

        // add the pair (id2, relationshipDate) to the list corresponding to the given ID
        public void addRelationship(int id1, int id2, Date relationshipDate) {
            int location = id1 % table.length;
            ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> relationshipList = getNode(id1);
            Pair<Integer, Date> value = new Pair<>(id2, relationshipDate);

            relationshipList.getValue().getSecond().add(value);
            relationshipList.getValue().getFirst().setSecond(relationshipList.getValue().getFirst().getSecond() + 1);
        }
    }

    // hash map used as a set to store users for getting mutual followers/follows
    class UserSet {
        private LinkedList<Pair<Integer, Date>>[] table;
        private int size;

        // typical hash table constructor
        public UserSet() {
            table = new LinkedList[997];
            size = 0;
            initialiseTable();
        }

        // initalise each table entry with an empty linked list
        private void initialiseTable() {
            for(int i = 0; i < table.length; i++) {
                table[i] = new LinkedList();
            }
        }

        // get the number of elements in the hash table
        public int size() {
            return size;
        }

        // return the element that has the provided ID, or null if no such element exists
        public Pair<Integer, Date> getElementById(int id) {
            int location = id % table.length;
            ListElement<Pair<Integer, Date>> temp = table[location].getHead();

            while (temp != null) {
                if (temp.getValue().getFirst() == id) {
                    return temp.getValue();
                }
                temp = temp.getNext();
            }

            return null;
        }

        // add a new (ID, date) pair to the set; if one with the same ID already exists, overwrite only if the new date is greater
        public void add(int id, Date date) {
            int location = id % table.length;
            Pair<Integer, Date> testExistence = getElementById(id);

            if (testExistence == null) {
                table[location].add(new Pair<>(id, date));
                size++;
            }
            else if (date.compareTo(testExistence.getSecond()) > 0) {
                testExistence.setSecond(date);
            }
        }

        // return an array of all elements, or null if there are no elements
        public Pair<Integer, Date>[] getElementArray() {
            if (size == 0) {
                return null;
            }

            Pair<Integer, Date>[] elementArray = new Pair[size];
            int count = 0;

            for (int i = 0; i < table.length; i++) {
                ListElement<Pair<Integer, Date>> temp = table[i].getHead();

                while (temp != null) {
                    elementArray[count++] = temp.getValue();
                    temp = temp.getNext();
                }
            }

            return elementArray;
        }
    }

    idHashMap followerGraph; // directed graph whose edges point from users to their followers
    idHashMap followGraph; // directed graph whose edges point from users to those they follow

    // initialise the two graphs
    public FollowerStore() {
        followerGraph = new idHashMap();
        followGraph = new idHashMap();
    }

    // add user with ID uid2 as a follower to user with ID uid1
    public boolean addFollower(int uid1, int uid2, Date followDate) {
        boolean status = false;

        // if this user has no node in followGraph, create one
        if (followGraph.getNode(uid1) == null) {
            followGraph.addNode(uid1);
        }
        // if there is no relationship between the two, add one
        if (followGraph.getRelationship(uid1, uid2) == null) {
            followGraph.addRelationship(uid1, uid2, followDate);
            // do the same for the other graph
            if (followerGraph.getNode(uid2) == null) {
                followerGraph.addNode(uid2);
            }
            followerGraph.addRelationship(uid2, uid1, followDate);
            status = true;
        }

        return status;
    }

    // return a list of all the followers of the user with ID uid
    public int[] getFollowers(int uid) {
        ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> node = followerGraph.getNode(uid);

        // if the node doesn't exist, then there is nothing to return
        if (node == null) {
            return null;
        }
        
        ListElement<Pair<Integer, Date>> temp = node.getValue().getSecond().getHead();
        int followerCount = node.getValue().getFirst().getSecond();

        if (followerCount == 0) {
            return null;
        }

        Pair<Integer, Date>[] followerArray = new Pair[followerCount];
        int[] idArray = new int[followerCount];
        int i = 0;

        // store the list into an array
        while (temp != null) {
            followerArray[i++] = temp.getValue();
            temp = temp.getNext();
        }

        // sort the array
        quickSort(followerArray, 0, followerCount - 1);

        // keep the IDs only
        for (i = 0; i < followerCount; i++) {
            idArray[i] = followerArray[i].getFirst();
        }

        return idArray;
    }

    // return a list of all those followed by the user with ID uid
    public int[] getFollows(int uid) {
        ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> node = followGraph.getNode(uid);

        // if the node doesn't exist, then there is nothing to return
        if (node == null) {
            return null;
        }

        ListElement<Pair<Integer, Date>> temp = node.getValue().getSecond().getHead();
        int followCount = node.getValue().getFirst().getSecond();

        if (followCount == 0) {
            return null;
        }

        Pair<Integer, Date>[] followArray = new Pair[followCount];
        int[] idArray = new int[followCount];
        int i = 0;

        // store the list into an array
        while (temp != null) {
            followArray[i++] = temp.getValue();
            temp = temp.getNext();
        }

        // sort the array
        quickSort(followArray, 0, followCount - 1);

        // keep the IDs only
        for (i = 0; i < followCount; i++) {
            idArray[i] = followArray[i].getFirst();
        }

        return idArray;
    }

    // check if a user is the follower of the other
    public boolean isAFollower(int uidFollower, int uidFollows) {
        ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> node = followerGraph.getNode(uidFollows);
        ListElement<Pair<Integer, Date>> temp = node.getValue().getSecond().getHead();

        while (temp != null) {
            if (temp.getValue().getFirst() == uidFollower) {
                return true;
            }
            temp = temp.getNext();
        }

        return false;
    }

    // get the number of followers of a given user
    public int getNumFollowers(int uid) {
        return followerGraph.getNode(uid).getValue().getFirst().getSecond();
    }

    // get the mutual followers of the two users provided
    public int[] getMutualFollowers(int uid1, int uid2) {
        UserSet mutualFollowers = new UserSet();
        UserSet firstUserFollowers = new UserSet();
        ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> node;
        ListElement<Pair<Integer, Date>> temp;
        Pair<Integer, Date>[] mutualFollowersList;
        int[] mutualFollowersIds;

        // add the first user's followers to a set
        node = followerGraph.getNode(uid1);
        temp = node.getValue().getSecond().getHead();

        while (temp != null) {
            firstUserFollowers.add(temp.getValue().getFirst(), temp.getValue().getSecond());
            temp = temp.getNext();
        }

        // add only the common followers to the intersection set
        node = followerGraph.getNode(uid2);
        temp = node.getValue().getSecond().getHead();

        while (temp != null) {
            if (firstUserFollowers.getElementById(temp.getValue().getFirst()) != null) {
                mutualFollowers.add(temp.getValue().getFirst(), temp.getValue().getSecond());
                temp = temp.getNext();
            }
        }

        mutualFollowersList = mutualFollowers.getElementArray();

        if (mutualFollowersList.length == 0) {
            return null;
        }

        mutualFollowersIds = new int[mutualFollowersList.length];
        quickSort(mutualFollowersList, 0, mutualFollowersList.length - 1);

        for (int i = 0; i < mutualFollowersList.length; i++) {
            mutualFollowersIds[i] = mutualFollowersList[i].getFirst();
        }

        return mutualFollowersIds;
    }

    // get the mutual follows of the two users provided
    public int[] getMutualFollows(int uid1, int uid2) {
        UserSet mutualFollows = new UserSet();
        UserSet firstUserFollows = new UserSet();
        ListElement<Pair<Pair<Integer, Integer>, LinkedList<Pair<Integer, Date>>>> node;
        ListElement<Pair<Integer, Date>> temp;
        Pair<Integer, Date>[] mutualFollowsList;
        int[] mutualFollowsIds;

        // add the first user's follows to a set
        node = followGraph.getNode(uid1);
        temp = node.getValue().getSecond().getHead();

        while (temp != null) {
            firstUserFollows.add(temp.getValue().getFirst(), temp.getValue().getSecond());
            temp = temp.getNext();
        }

        // add only the common follows to the intersection set
        node = followGraph.getNode(uid2);
        temp = node.getValue().getSecond().getHead();

        while (temp != null) {
            if (firstUserFollows.getElementById(temp.getValue().getFirst()) != null) {
                mutualFollows.add(temp.getValue().getFirst(), temp.getValue().getSecond());
                temp = temp.getNext();
            }
        }

        mutualFollowsList = mutualFollows.getElementArray();

        if (mutualFollowsList.length == 0) {
            return null;
        }

        mutualFollowsIds = new int[mutualFollowsList.length];
        quickSort(mutualFollowsList, 0, mutualFollowsList.length - 1);

        for (int i = 0; i < mutualFollowsList.length; i++) {
            mutualFollowsIds[i] = mutualFollowsList[i].getFirst();
        }

        return mutualFollowsIds;
    }

    // get the users sorted by how many followers they have
    public int[] getTopUsers() {
        int[] topUsers = followerGraph.getIdArray();
        quickSort(topUsers, 0, topUsers.length - 1);

        return topUsers;
    }

    // quicksort algorithm; taken and adapted from http://www.algolist.net/Algorithms/Sorting/Quicksort
    private void quickSort(Pair<Integer, Date>[] list, int left, int right) {
        int index = partition(list, left, right);

        if (left < index - 1) {
            quickSort(list, left, index - 1);
        }
        if (index < right) {
            quickSort(list, index, right);
        }
    }

    private int partition(Pair<Integer, Date>[] list, int left, int right) {
        int i = left, j = right;
        Pair<Integer, Date> temp;
        Pair<Integer, Date> pivot = list[(left + right) / 2];

        while (i <= j) {
            while (list[i].getSecond().compareTo(pivot.getSecond()) > 0) {
                i++;
            }
            while (list[j].getSecond().compareTo(pivot.getSecond()) < 0) {
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

    private void quickSort(int[] list, int left, int right) {
        int index = partition(list, left, right);

        if (left < index - 1) {
            quickSort(list, left, index - 1);
        }
        if (index < right) {
            quickSort(list, index, right);
        }
    }

    private int partition(int[] list, int left, int right) {
        int i = left, j = right;
        int temp;
        int pivot = list[(left + right) / 2];

        while (i <= j) {
            while (list[i] > pivot) {
                i++;
            }
            while (list[j] < pivot) {
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
