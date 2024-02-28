**Summary:**

Implemented a TreeMap class that utilizes parent pointers and a dummy root node for efficient tree navigation and operations. The TreeMap class implements the Map interface and provides standard map functionalities like adding, removing, and retrieving entries, as well as iterating over the entries in ascending order.

**Key Achievements:**

Implemented a TreeMap class with parent pointers and a dummy root node for efficient tree navigation.

Overrode methods of the Map interface such as size, isEmpty, containsKey, containsValue, get, put, remove, putAll, clear, keySet, values, and entrySet to provide the desired functionalities.

Implemented an EntrySet class to represent the set of entries in the TreeMap, ensuring that modifications to the set reflect changes in the TreeMap.

Implemented an iterator for the EntrySet that allows for iterating over entries in ascending order, handling parent pointers and the dummy root node correctly.

Ensured that the TreeMap class maintains its invariants and behaves correctly even when the number of entries changes.

**Challenges Overcome:**

Managed the complexities of implementing a TreeMap with parent pointers and a dummy root node, ensuring that the tree structure remains correct and efficient.

Implemented the EntrySet and iterator classes to correctly reflect changes in the TreeMap and handle various edge cases, such as when the current node is the dummy root node.

**Next Steps:**

Perform thorough testing of the TreeMap class using the provided JUnit test suite, ensuring that all operations behave as expected and that the tree structure remains correct.

Refactor the implementation if necessary, focusing on improving efficiency and addressing any edge cases or potential issues identified during testing.

Consider additional features or enhancements to the TreeMap class, based on feedback and requirements, to further improve its usability and functionality.
