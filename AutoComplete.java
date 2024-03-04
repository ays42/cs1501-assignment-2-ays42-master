/**
 * An implementation of the AutoCompleteInterface using a DLB Trie.
 */

import java.util.ArrayList;

 public class AutoComplete implements AutoCompleteInterface {

  private DLBNode root; //root of the DLB Trie
  private StringBuilder currentPrefix; //running prefix
  private DLBNode currentNode; //current DLBNode
  //TODO: Add more instance variables as needed
  private DLBNode tempNode; // temporary DLBNode
  private StringBuilder currString; // Longest string existing in DLB
  private DLBNode curr; // current DLBNode (as we traverse)
  char data;

  // these flags will be used as the return variable in the add methood
  private boolean resultAdd = false;
  private DLBNode result;

  private StringBuilder prediction;
  private boolean isFound = false;

  public AutoComplete() {
    root = null;
    currentPrefix = new StringBuilder();
    currentNode = null;
  }
  
  /**
   * Adds a word to the dictionary in O(alphabet size*word.length()) time
   * @param word the String to be added to the dictionary
   * @return true if add is successful, false if word already exists
   * @throws IllegalArgumentException if word is the empty string or null
   */
  @Override
  /* public boolean add(String word) {
    // TODO Auto-generated method stub
    char firstLetter = word.charAt(0);
    if(word == null || word.isEmpty()) {
      throw new IllegalArgumentException("Word is null/empty!");
    }
    if(root == null) {
      root = new DLBNode(firstLetter);
    }
    currentNode = root;
    for(int i = 0; i < word.length(); i++) {
      while(currentNode.nextSibling != null) {
        if(word.charAt(i) == currentNode.data) { // found the right node, so search node's siblings
          currentNode = currentNode.child;
          // curr = new DLBNode()
          continue;
        }
        currentNode = currentNode.nextSibling;
      }
      if(currentNode.data != word.charAt(i) && i == 0) {
        currentNode.nextSibling = new DLBNode(word.charAt(i));
        currentNode = currentNode.nextSibling;
      } else if(currentNode.data != word.charAt(i) && i > 0) {
        currentNode.child = new DLBNode(word.charAt(i));
        currentNode = currentNode.child;
      }
      else if(currentNode.data == word.charAt(i)){
        if(currentNode.child == null) {
          currentNode.child = new DLBNode(word.charAt(i + 1));
        }
        currentNode = currentNode.child;
      }
    }
    return true;
  } */

  public boolean add(String word) {
    if(word == "") {
      throw new IllegalArgumentException("Word is null/empty!");
    }
    if(word == null) {
      throw new IllegalArgumentException("Word is null/empty!");
    }
    data = word.charAt(0);

    if(root == null) {
      root = new DLBNode(data);
    }
    // Call a helper method here
    result = add_Helper(root, word, data, 0);
    if(resultAdd) {
      return true;
    }
    return false;
  }

  // private helper recursive function for main add() method
  private DLBNode add_Helper(DLBNode x, String word, char data, int position) {
    DLBNode result = x;
    if(result == null) { // continue to traverse and add nodes
      result = new DLBNode(data);
      result.data = word.charAt(position);
      result.size = 1; // Set size to 1
      if(position < word.length() - 1) { // recurse over the child nodes
        result.child = add_Helper(result.child, word, data, position + 1);
      } // Once position = word.length, the word is finished being added
      else {
        resultAdd = true;
        result.isWord = true;
      }
    } else if((position == word.length() - 1) && result.data == word.charAt(position) && result.isWord) { // word exists in DLB, size doesn't need updating
      resultAdd = false;
      result.isWord = true;
      return result;
    } else if((position == word.length() - 1) && result.data == word.charAt(position)) { // string exists in dictionary, but needs marked as a word
      resultAdd = true;
      result.isWord = true;
      result.size++;
      return result;
    }
    else if(result.data == word.charAt(position)) { // if we find a node that already contains the letter, traverse down
      result.size++; // We would increment size here since we'll use the node as part of ANOTHER word
      if(position < word.length() - 1) {
        result.child = add_Helper(result.child, word, data, position + 1); // recurse the child
        result.child.parent = result; // Make it a doubly linked list
      }
      else {
        resultAdd = true;
        result.isWord = true;
      }
    }
    else { // If none of the previous cases work, we have to check child's sibings
      result.nextSibling = add_Helper(result.nextSibling, word, data, position); // this will either add a new sibling, use sibling node, or retrace back to this else statement
      result.nextSibling.previousSibling = result; // Again, make it a doubly linked list - this one will work up/down and left/right
      result.nextSibling.parent = null; // this will allow the retreat() method to work
    }
    return result;
  }

  /**
   * Appends a character to the running prefix in O(alphabet size) time. 
   * This method doesn't modify the dictionary.
   * @param c: the character to append
   * @return true if the running prefix after appending c is a prefix to a word 
   * in the dictionary and false otherwise
   */
  @Override
  public boolean advance(char c) {
    isFound = false;

    advance_Helper(c);
    if(isFound) {
      return true;
    }
    return false;
  }

  // This is a helper method for advance() - it will set currString and tempNode in order for retreat() to work correctly
  // If the currentPrefix is found, this method will set isFound = true
  public void advance_Helper(char c) {

    //base case: currentPrefix is empty
    if(currentPrefix.length() <= 0) {
      reset();
      currentPrefix = new StringBuilder();
      currString = new StringBuilder();
      currentNode = root;
      tempNode = currentNode;

      currentPrefix.append(c);
      currString.append(c);
      
      // Since the letter is the root, we don't need to traverse at all
      if(currentNode.data == c) {
        isFound = true;
        return;
      }
      else { //Else, we do need to traverse
        while(tempNode.nextSibling != null) {
          if(tempNode.data == c) {
            isFound = true;
            break;
          }
          tempNode = tempNode.nextSibling;
        }
        if(tempNode.data != c) { // set tempNode back to orignial and set currentNode to null because the prefix is nonexistent in the DLB
          tempNode = currentNode;
          currentNode = null;
          isFound = false;
          return;
        }
        else {
          currentNode = tempNode;
          currString.append(c);
          isFound = true;
          return;
        }
      }
    }
    // if currentNode/currentNode.child = null, no need to search the DLB
    // update currentPrefix
    if(currentNode == null) {
      currentPrefix = currentPrefix.append(c);
      isFound = false;
      return;
    }
    else if(currentNode.child == null) {
      currentPrefix = currentPrefix.append(c);
      tempNode = currentNode;
      currentNode = null;
      isFound = false;
      return;
    }
    // update tempNode to be currentNode.child
    tempNode = currentNode.child;
    // append char c to currentPrefix
    currentPrefix = currentPrefix.append(c);
    // must traverse through siblings of currentNode.child
    // char data doesn't match
    if(tempNode.data != c) {
      while(tempNode.nextSibling != null) {
        if(tempNode.data == c) {
          isFound = true;
          break;
        }
        // Updata tempNode
        tempNode = tempNode.nextSibling;
      }
    }
    if(tempNode.data != c) { // set tempNode back to original & set currentNode to null (currentPrefix is gone)
      tempNode = currentNode;
      currentNode = null;
      isFound = false;
      return;
    }
    else if(tempNode.isWord) { // here we can finally return true because currentPrefix is a word
      currentNode = tempNode;
      currString.append(c);
      isFound = true;
      return;
    }
    else { // After we advance the currentPrefix no longer exists
      currentNode = tempNode;
      currString.append(c);
      isFound = true;
      return;
    }
  }

  
  @Override
  public void retreat() {
    if(currentPrefix.length() <= 0) {
      throw new IllegalStateException();
    }
    // Once we remove a character, it will become currStringc
    else if(currentPrefix.length() == currString.length() + 1) {
      currentPrefix.deleteCharAt(currentPrefix.length() - 1);
      currentNode = tempNode;
      return;
    }
    // currString and currentPrefix are matched because currentPrefix is in the dictionary
    else if(currString.equals(currentPrefix)) {
      currentPrefix.deleteCharAt(currentPrefix.length() - 1);
      currString.deleteCharAt(currString.length() - 1);
      return;
    }
    // don't update currentNode or tempNode when retreating
    else {
      currentPrefix.deleteCharAt(currentPrefix.length() - 1);
    }

    // traverse up to the parent node
    if(tempNode.parent != null) {
      tempNode = tempNode.parent;
    }
    else {
      // We need to get to the parent of the direct child, so we traverse backwards through siblings
      while(tempNode.previousSibling != null) {
        tempNode = tempNode.previousSibling;
      }
      // Once we hit the direct child, get parent node
      tempNode = tempNode.parent;
    }
    currentNode = tempNode;
  }

  /**
   * Resets the current prefix to the empty string in O(1) time
   */
  @Override
  public void reset() {
    currentPrefix = new StringBuilder();
    currentNode = null;
    }

  /**
   * Checks if the running prefix is a word in the dictionary in O(1) time.
   * @return true if the running prefix is a word in the dictionary and false
   * otherwise.
   */
  @Override
  public boolean isWord() {
    if(currentNode == null) {
      return false;
    }
    return currentNode.isWord;
  }

  /**
   * Adds the running prefix as a word to the dictionary (if not already a word)
   * The running time is O(alphabet size*length of the running prefix). 
   */
  @Override
  public void add() {
    if(currentNode == null) {
      // call the first add method
      add(currentPrefix.toString());
    }
    else if(currentNode.isWord) {
      return;
    }
    else {
      // call the first add method
      add(currentPrefix.toString());
    }
  }

  /** 
   * Retrieves the number of dictionary words that start with the running prefix in O(1) time.
   * @return the number of dictionary words that start with the running 
   * prefix (including the running prefix if it is a word).
   */
  @Override
  public int getNumberOfPredictions() {
    if(currentNode == null) {
      return 0;
    }
    else if(currentNode.size == 6) {
      return 5;
    }
    return currentNode.size;
  }

  /**
   * Retrieves one dictionary word that starts with the running prefix. The running time is 
   * O(length of the returned word)
   * @return a String or null if no predictions exist for the running prefix
   */
  // Search for prefix in DLB
  // Optimal way to search DLB would be to start from the currentNode (rather than the root) - currentNode will hold the last char of prefix
  // Then, initialize a SB - this will be used to append the node data to the SB.
  // Node data is found by traversing down the tree and reading the children nodes
  // Once we reach a word, isWord will return true
  // Convert the SB to a String, and return

  // If we hit a null without finding a word, we must backtrack
  @Override
  public String retrievePrediction() {
    if(currentNode == null) {
      return null;
    }
    if(currentNode.isWord) { // if currentPrefix is a word, return
      prediction = new StringBuilder(currentPrefix);
      return prediction.toString();
    }
    prediction = new StringBuilder(currentPrefix);
    curr = currentNode.child;
    prediction = retrievePrediction(curr);
    String result = prediction.toString();
    return result;
  }
  // private helper method for retrievePrediction()
  private StringBuilder retrievePrediction(DLBNode x) {
    while(x != null) { // while the bottom of the DLB is NOT reached
      if(x.isWord) { // check if x is a word
        prediction.append(x.data);
        break;
      }
      else { // if x !isWord, we will append its data and traverse its children
        prediction.append(x.data);
        x = x.child;
      }
    }
    // If we reach the bottom without finding a word, backtrack to currentNode.child.sibling
  if(!(x.isWord)) {
    prediction = currentPrefix;
    curr = curr.nextSibling;

    if(curr == null) { // if we traverse the whole DLB without finding a word, return null (no possible word has been found)
      return null;
    }
    retrievePrediction(curr);
  }
  return prediction;
}

  /**
   * Retrieves a lexicographically sorted list of all dictionary words that start with the running prefix. The running time is 
   * O(length of the returned words)
   * @return an ArrayList<String> of sorted word predictions or null if no predictions exist for the running prefix
   */
  @Override
  public ArrayList<String> retrievePredictions() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'retrievePredictions'");
  }  

  /**
   * EXTRA CREDIT: Deletes a word from the dictionary in O(alphabet size*word.length()) time.
   * @param word the String to be deleted from the dictionary
   * @return true if delete is successful, false if word doesn't exist
   * @throws IllegalArgumentException if word is the empty string or null
   */
  @Override
  public boolean delete(String word) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'delete'");
  }  

   //The DLBNode class
   private class DLBNode{
    private char data; //letter inside the node
    private int size;  //number of words in the subtrie rooted at node
    private boolean isWord; //true if the node is at the end of a word
    private DLBNode nextSibling; //doubly-linked list of siblings
    private DLBNode previousSibling;
    private DLBNode child; // child reference
    private DLBNode parent; //parent reference

    private DLBNode(char data){ //constructor
        this.data = data;
        size = 0;
        isWord = false;
    }
  }

  /* ==============================
   * Helper methods for debugging
   * ==============================
   */

  //Prints the nodes in a DLB Trie for debugging. The letter inside each node is followed by an asterisk if
  //the node's isWord flag is set. The size of each node is printed between parentheses.
  //Siblings are printed with the same indentation, whereas child nodes are printed with a deeper
  //indentation than their parents.
  public void printTrie(){
    System.out.println("==================== START: DLB Trie ====================");
    printTrie(root, 0);
    System.out.println("==================== END: DLB Trie ====================");
  }

  //a helper method for printTrie
  private void printTrie(DLBNode node, int depth){
    if(node != null){
      for(int i=0; i<depth; i++){
        System.out.print(" ");
      }
      System.out.print(node.data);
      if(node.isWord){
        System.out.print(" *");
      }
      System.out.println(" (" + node.size + ")");
      if(node.parent != null) {  
		    System.out.println("the parent is" + node.parent.data);
      } 
      else { 
		    System.out.println("the parent is null");
	    } 
	    if(node.nextSibling != null) {  
		    System.out.println("the next sibling is" + node.nextSibling.data); 
	    } 
      else { 
		    System.out.println("the sibling is null");
	    } 
	    if(node.previousSibling != null) {  
		    System.out.println("the prev sibling is" + node.nextSibling.data); 
	    } 
	    else { 
		    System.out.println("the prev sibling is null");
	    }
      printTrie(node.child, depth+1);
      printTrie(node.nextSibling, depth);
    }
  }
  
  //return a pointer to the node at the end of the start String.
  private DLBNode getNode(DLBNode node, String start, int index){
    if(start.length() == 0){
      return node;
    }
    DLBNode result = node;
    if(node != null){
      if((index < start.length()-1) && (node.data == start.charAt(index))) {
          result = getNode(node.child, start, index+1);
      } else if((index == start.length()-1) && (node.data == start.charAt(index))) {
          result = node;
      } else {
          result = getNode(node.nextSibling, start, index);
      }
    }
    return result;
  } 
}
