package edu.uwm.cs351.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import junit.framework.TestCase;

//Jiahui Yang


public class TreeMap<K,V>  extends AbstractMap<K,V> {
	// Here is the data structure to use.
	private static class Node<K,V> extends DefaultEntry<K,V> {
		Node<K,V> left, right;
		Node<K,V> parent;
		Node(K k, V v) {
			super(k,v);
			parent = left = right = null;
		}
	}
	//declare/initialize fields
	private Comparator<K> comparator;
	private Node<K,V> dummy;
	private int numItems = 0;
	private int version = 0;
	
	
	/// Invariant checks:
	
	private static Consumer<String> reporter = (s) -> { System.err.println("Invariant error: " + s); };
	
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}
	
	/**
	 * Return whether nodes in the subtree rooted at the given node have correct parent
	 * and have keys that are never null and are correctly sorted and are all in the range 
	 * between the lower and upper (both exclusive).
	 * If either bound is null, then that means that there is no limit at this side.
	 * The first problem is found will be reported.
	 * @param node root of subtree to examine
	 * @param p parent of subtree to examine
	 * @param lower value that all nodes must be greater than.  If null, then
	 * there is no lower bound.
	 * @param upper value that all nodes must be less than. If null,
	 * then there is no upper bound.
	 * @return whether the subtree is fine. If false is 
	 * returned, there is a problem, which has already been reported.
	 */
	private boolean checkInRange(Node<K,V> node, Node<K, V> p, K lower, K upper) {
		//if node is null then it is in range because null is unbound
		if (node == null) return true;
		//return a report if nodes data is null
		if(node.parent != p) return report("wrong parent");
		if (node.key == null) return report("Node data is null");
		//if nodes left and right nodes don't point back to the proper node, return report
		if (node.left != null && node.left.parent != node) return report("wrong parent");
		if (node.right != null && node.right.parent != node) return report("wrong parent");
		if (node.left != null&& comparator.compare(node.key, node.left.key)<=0) return report("comparator wrong");
		//if lo isn't null and nodes data is before the given range, return report
		if (lower != null && (comparator.compare(lower, node.key)>0)) return report("Node before range");
		//if hi isn't null and nodes data is after the given range or exactly the hi, then report
		if (upper != null && (comparator.compare(upper, node.key)<=0)) return report("Node after range");
		//recursive call on itself with different node being passed in each time going through the left and right of tree
		return checkInRange(node.left, node, lower, node.key) && checkInRange(node.right, node, node.key, upper);//  
		//  
	}
	
	/**
	 * Return the number of nodes in a binary tree.
	 * @param r binary (search) tree, may be null but must not have cycles
	 * @return number of nodes in this tree
	 */
	private int countNodes(Node<K,V> r) {
		if (r == null) return 0;
		return 1 + countNodes(r.left) + countNodes(r.right);
	}
	
	/**
	 * Check the invariant, printing a message if not satisfied.
	 * @return whether invariant is correct
	 */
	private boolean wellFormed() {
		//1
		if (comparator == null) return report("comparator is null");
		//2
		if (dummy == null) return report("dummy is null");
		//3
		if (dummy.key != null || dummy.right != null || dummy.parent != null) return report("dummy key, right subtree, and parent are not null");
		//4, 5
		if (!checkInRange(dummy.left, dummy, null, null)) return false;
		//6
		if (countNodes(dummy.left) != numItems) return report("number of nodes not equal to manyItems");
		//  :
		// 1. check that comparator is not null
		// 2. check that dummy is not null
		// 3. check that dummy's key, right subtree and parent are null
		// 4. check that all (non-dummy) nodes are in range
		// 5. check that all nodes have correct parents
		// 6. check that number of items matches number of (non-dummy) nodes
		// "checkInRange" will help with 4,5
		return true;
	}
	
	
	/// constructors
	
	private TreeMap(boolean ignored) { } // do not change this.
	
	public TreeMap() {
		this(null);
		assert wellFormed() : "invariant broken after constructor()";
	}

	@SuppressWarnings("unchecked")
	public TreeMap(Comparator<K> c) {
		//if c is null create set comparator to ascending with lambda syntax
		if (c == null) comparator = (s1,s2) -> ((Comparable<K>)s1).compareTo(s2);
		//else set comparator to the argument passed
		else comparator = c;
		//create dummy node
		dummy = new Node<K, V>(null, null);
		//  
		// Update the parameter comparator if necessary
		// Create the dummy node.
		assert wellFormed() : "invariant broken after constructor(Comparator)";
	}

	@SuppressWarnings("unchecked")
	private K asKey(Object x) {
		if (dummy.left == null || x == null) return null;
		try {
			comparator.compare(dummy.left.key,(K)x);
			comparator.compare((K)x,dummy.left.key);
			return (K)x;
		} catch (ClassCastException ex) {
			return null;
		}
	}
	//find key's Node using recursion calling upon itself changing variables
	private Node<K, V> findKeyHelper(Node<K, V> r, K o){
		if (r == null || o == null) return null;
		if (r.key.equals(o)) return r;
		else if (comparator.compare(r.key, o) < 0) return findKeyHelper(r.right, o);
		else return findKeyHelper(r.left, o);
		
	}
	/**
	 * Find the node for a given key.  Return null if the key isn't present
	 * in the tree.  This helper method assumes that the tree is well formed,
	 * but doesn't check that.
	 * @param o object treated as a key.
	 * @return node whose data is equal to o, 
	 * or null if no nodes in the tree have this property.
	 */
	
	//uses findKey's helper to find the Node
	private Node<K, V> findKey(Object o){
		return findKeyHelper(dummy.left, asKey(o));
		
		 //   (non-recursive is fine)
	}

	//  : many methods to override here:
	// size, containsKey(Object), get(Object), clear(), put(K, V), remove(Object)
	// make sure to use @Override and assert wellformedness
	// plus any private helper methods.
	// Our solution has getNode(Object)
	// Increase version if data structure if modified.
	//https://condor.depaul.edu/glancast/403class/docs/lecSep19.html#slide4
	private Node<K, V> doPut(Node<K, V> r, K k, V v) {
		//base case
		if (r == null) {
			r = new Node<K, V>(k, v);
			r.parent = dummy;
			return r;
		}
		//node of element should go BEFORE r
		else if (comparator.compare(k, r.key)<0) {
			r.left = doPut(r.left, k, v);
			r.left.parent = r;
		}
		//node of element should go AFTER r
		else {
			r.right = doPut(r.right, k, v);
			r.right.parent = r;
		}

		return r;
	}
	@Override //implementation
	public V put(K key, V value) {
		assert wellFormed(): "invariant broken in put";
		//if key is null throw exception
		if (key == null) throw new NullPointerException();
		//get the old value before updating the value
		V temp = get(key);
		
		if (containsKey(key)) {
			//if the entry is already in the Binary Search Tree, create a new node with the same entry data
			Node<K, V> node = findKey(key);
			//update the node we just created value
			node.value = value;
		}
		else {
			//if entry is not in the BST, call the helper which does the work for adding
			dummy.left = doPut(dummy.left, key, value);
			//increment version
			version++;
			//increment numItems;
			++numItems;
		}
		assert wellFormed(): "invariant broken by put";
		// returns null or the old value
		return temp;
	}
	//got from lab 9 advance
	private Node<K, V> getNext(Node<K, V> r){
		if (r.right != null) {
			if (r.right.left == null) r=r.right;
			else r = r.right;

			while(r.left!=null) {
				r = r.left;
			}
		}
		else {
			if (r.parent !=null) {
				if (r.parent.right == r) {
					Node<K, V> temp = r;
					while(r!=null) {
						if (r.parent == null) {
							r = null;
							break;
						}
						r = r.parent;
						if(temp == r.left) break;
						temp = r;
					}
				}
				else {
					r = r.parent;
				}
			}
			else {
				r = null;
			}
		}
		return r;

	}
	
	//https://condor.depaul.edu/glancast/403class/docs/lecSep19.html#slide4
	//helper method that uses recursion to get the desired value
	private V doGet(Node<K, V> r, K k){
		if (r == null || k==null) return null;
		if (comparator.compare(k, r.key)<0) return doGet(r.left, k);
		else if (comparator.compare(k, r.key)>0) return doGet(r.right, k);
		else return r.value;
	}
	@Override //efficiency
	public V get(Object key) {
		assert wellFormed(): "invariant broken in get";
		//uses get helper to do all the work finding the value associated with the object in BST
		return doGet(dummy.left, asKey(key));
	}
	
	@Override //efficiency
	public boolean containsKey(Object key) {
		assert wellFormed(): "invariant broken in containsKey";
		//return whether or not findKey finds the object or not
		return findKey(key) != null;
	}
	@Override //implementation
	public int size() {
		assert wellFormed(): "invariant broken in size";
		//return numItems variable, should be accurate because of wellFormed
		return numItems;
	}
	private Node<K, V> firstInTree(Node<K, V> r) {
		//base case if r is null then return null
		if (r == null) return null;
		//if there is a left subtree return the node most left
		if (r.left != null) return firstInTree(r.left);
		//return r 
		return r;
		//  : non-recursive is fine
	}
	//uses recursion on itself updating arguments
	private Node<K, V> doRemove(Node<K, V> r, K target){
		if (r.key.equals(target)) {
			if (r.left == null) return r.right;
			if (r.right == null) return r.left;
			Node<K, V> t = firstInTree(r.right);
			t.right = doRemove(r.right, t.key);
			if(t.right!=null) t.right.parent = t;
			t.left = r.left;
			if(t.left!=null)t.left.parent = t;
			r = t;
		}
		else if (comparator.compare(target, r.key) < 0) {
			r.left = doRemove(r.left, target);
			if (r.left!=null)r.left.parent = r;
		}
		else {
			r.right = doRemove(r.right, target);
			if (r.right!=null)r.right.parent = r;
		}
		return r;
	}

    
	@Override //implementation
	public V remove(Object key) {
		//check invariant
		assert wellFormed(): "invariant broken in remove";
		//if the object is not in the BST, return null
		if (!containsKey(key)) return null;
		//save old value in a temporary variable
		V temp = get(key);		
		//call remove helper to actually remove the node
		dummy.left = doRemove(dummy.left, asKey(key));
		//set parents
		if (dummy.left != null) dummy.left.parent = dummy;
		//update numItems and version
		--numItems;
		++version;
		assert wellFormed(): "invariant broken by remove";
		return temp;
	}
	@Override //implementation
	public void clear() {
		assert wellFormed() : "Invariant broken at start of clear";
		//if BST is empty return
		if (numItems == 0) return;
		//set numItems to 0 and cut off all nodes from dummy
        numItems = 0;
        dummy.left = null;
        //update version
        version++;
        assert wellFormed() : "Invariant broken at end of clear";
    }
	private volatile Set<Entry<K,V>> entrySet;
	
	@Override //required
	public Set<Entry<K, V>> entrySet() {
		assert wellFormed() : "invariant broken at beginning of entrySet";
		//create a new entry set
		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}

	/**
	 * The set for this map, backed by the map.
	 * By "backed: we mean that this set doesn't have its own data structure:
	 * it uses the data structure of the map.
	 */
	private class EntrySet extends AbstractSet<Entry<K,V>> {
		// Do NOT add any fields! 
		
		@Override //required
		public int size() {
			//  : Easy: delegate to TreeMap.size()
			assert wellFormed() : "Invariant broken at start of EntrySet.size";
			//delegate
			return TreeMap.this.size();
		}

		@Override //required
		//create a new iterator
		public Iterator<Entry<K, V>> iterator() {
			assert wellFormed() : "Invariant broken at start of iterator";
			return new MyIterator();
		}
		//contains helper that uses recursion on itself that changes argument to find the node with the key informatioj
		private Node<K, V> containsH(Node<K, V> r, K e) {
			if (r == null || e == null) return null;
			if (r.key.equals(e)) return r;
			if (comparator.compare(r.key, e)<0) return containsH(r.right, e);
			return containsH(r.left, e);
			
		}

		@Override //efficiency
		public boolean contains(Object o) {
			assert wellFormed() : "Invariant broken at start of EntrySet.contains";
			//if object is null return false
			if (o == null) return false;
			//if object not an Entry return false
			if (!(o instanceof Entry<?, ?>)) return false;
			//cast o into an Entry
			Entry<?, ?> k = (Entry<?, ?>) o;
			//set a Node variable to the node found using the contains helper
			Node<K, V> n = containsH(dummy.left, asKey(k.getKey()));
			assert wellFormed() : "Invariant broken at end of EntrySet.contains";
			//check if keys value and key is the same as n's value and key
			return k.equals(n);
			//   if o is not an entry (instanceof Entry<?,?>), return false
			// Otherwise, check the entry for this entry's key.
			// If there is no such entry return false;
			// Otherwise return whether the entries match (use the equals method of the Node class). 
			// N.B. You can't check whether the key is of the correct type
			// because K is a generic type parameter.  So you must handle any
			// Object similarly to how "get" does.
		}
		@Override //efficiency
		public boolean remove(Object x) {
			//check wellFormed
			assert wellFormed() : "Invariant broken at start of EntrySet.remove";
			//if object is null return false
			if (x == null) return false;
			//if the BST doesn't have the object return false
			if (!contains(x)) return false;
			//cast x as an Entry
			Entry <?, ?> x1 = (Entry<?, ?>) x;
			//delegates work to the TreeMap remove passing the entries key
			TreeMap.this.remove(x1.getKey());
			assert wellFormed() : "Invariant broken at end of EntrySet.remove";
			return true;
			//  : if the tree doesn't contain x, return false
			// otherwise do a TreeMap remove.
			// make sure that the invariant is true before returning.
		}
		@Override //efficiency
		public void clear() {
			//  : Easy: delegate to the TreeMap.clear()
			assert wellFormed() : "Invariant broken at start of EntrySet.clear";
			//delegate
			TreeMap.this.clear();
		}
	}

	
	/**
	 * Iterator over the map.
	 * We use parent pointers.
	 * current points to node (if any) that can be removed.
	 * next points to dummy indicating no more next.
	 */
	private class MyIterator implements Iterator<Entry<K,V>> {
		
		Node<K, V> current, next;
		int colVersion = version;
		
		boolean wellFormed() {

			
			// (1) check the outer wellFormed()
			if (!TreeMap.this.wellFormed()) return false;
			//2
			if (version != colVersion) return true; 
			//2a
			if (current == dummy) return report("current is dummy");
			//2b
			if (next == null ) return report("next is null");
			//2b + 2c
			if (current != null && current.parent == null) return report("not in tree");
			//2c
			if (current != null && next != getNext(current)) return report("next is not in right position");
			// (2) If version matches, do the remaining checks:
			//     (a) current should either be null or a non-dummy node in the tree
			//     (b) next should never be null and should be in the tree (maybe dummy).
			//     (c) if current is not null, make sure it is the last node before where next is.
			return true;
		}
		
		
		
		
		
		
		
		
		MyIterator(boolean ignored) {} // do not change this
		
		MyIterator() {
			//  : initialize next to the leftmost node
			next = firstInTree(dummy);
			assert wellFormed() : "invariant broken after iterator constructor";
		}
		
		public void checkVersion() {
			assert wellFormed() : "Invariant broken at start of checkVersion";
			if (version != colVersion) throw new ConcurrentModificationException("stale iterator");
		}
		@Override //required
		public boolean hasNext() {
			assert wellFormed() : "invariant broken before hasNext()";
			checkVersion();
			assert wellFormed() : "Invariant broken at end of hasNext()";
			//if next is dummy has no next
			return next != dummy;
			//  : easy!
		}
		@Override //required
		public Entry<K, V> next() {
			assert wellFormed() : "invariant broken at start of next()";
			if (!hasNext()) throw new NoSuchElementException();
			//current is the next node
			current = next;
			//next is the node after next
			next = getNext(current);
			//  
			// We don't use (non-existent)nextInTree: 
			// but rather parent pointers in the second case.
			assert wellFormed() : "invariant broken at end of next()";
			return current;
		}
		@Override //implementation
		public void remove() {
			//invariant check
			assert wellFormed() : "invariant broken at start of iterator.remove()";
			//version check
			checkVersion();
			//if current is null we cannot remove throw exception
			if (current == null) throw new IllegalStateException();
			//if there is a node in the BST that needs to be removed update version
			if (containsKey(current.key))++colVersion;
			//TreeMap remove does the work
			TreeMap.this.remove(current.key);
			//update current
			current = null;
			//  : check that there is something to remove.
			// Use the remove method from TreeMap to remove it.
			// (See handout for details.)
			// After removal, record that there is nothing to remove any more.
			// Handle versions.
			assert wellFormed() : "invariant broken at end of iterator.remove()";
			
		}
		
	}
	
	
	/// Junit test case of private internal structure.
	// Do not change this nested class.
	
	public static class TestSuite extends TestCase {
		
		protected Consumer<String> getReporter() {
			return reporter;
		}
		
		protected void setReporter(Consumer<String> c) {
			reporter = c;
		}

		protected static class Node<K,V> extends TreeMap.Node<K, V> {
			public Node(K k, V v) {
				super(k,v);
			}
			
			public void setLeft(Node<K,V> n) {
				this.left = n;
			}
			
			public void setRight(Node<K,V> n) {
				this.right = n;
			}
			
			public void setParent(Node<K,V> n) {
				this.parent = n;
			}
		}
		
		protected class MyIterator extends TreeMap<Integer,String>.MyIterator {
			public MyIterator() {
				tree.super(false);
			}
			
			public void setCurrent(Node<Integer,String> c) {
				this.current = c;
			}
			public void setNext(Node<Integer,String> nc) {
				this.next = nc;
			}
			public void setColVersion(int cv) {
				this.colVersion = cv;
			}
			
			@Override // make visible
			public boolean wellFormed() {
				return super.wellFormed();
			}
		}
		
		protected TreeMap<Integer,String> tree;
		
		@Override // implementation
		protected void setUp() {
			tree = new TreeMap<>(false);
		}

		protected boolean wellFormed() {
			return tree.wellFormed();
		}
		
		protected void setDummy(Node<Integer,String> d) {
			tree.dummy = d;
		}
		
		protected void setNumItems(int ni) {
			tree.numItems = ni;
		}
		
		protected void setComparator(Comparator<Integer> c) {
			tree.comparator = c;
		}
		
		protected void setVersion(int v) {
			tree.version = v;
		}

		protected Node<Integer,String> findKey(Object key) {
			return (Node<Integer,String>)tree.findKey(key);
		}
	}
}
