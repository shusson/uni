package net.datastructures;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * Created by shane on 17/09/2016.
 */
public class ExtendedAVLTree<K, V> extends AVLTree<K, V> {

    /**
     * creates an identical copy of the AVL tree specified by the
     * parameter and returns a reference to the new AVL tree
     *
     * Does a postorder traversal of the tree, on each visit it clones the node.
     *
     * Total Time complexity of this method is O(n)
     *
     * @see postorderClone for more time complexity analysis
     *
     * @param tree the tree to clone
     * @param <K> key
     * @param <V> value
     * @return AVLTree
     */
    public static <K, V> AVLTree<K, V> clone(AVLTree<K, V> tree) throws InvalidPositionException {
        AVLTree<K, V> clone = new AVLTree<>();
        clone.root = postorderClone(tree, tree.root());
        clone.numEntries = tree.numEntries;
        clone.size = tree.size;
        return clone;
    }

    /**
     * Merges two AVL trees, tree1 and tree2, into a new tree.
     *
     * Assumes that both trees have the same type of key K
     *
     * There are three significant parts to this method:
     * Build a sorted list from a tree O(n)
     * @see inorderNodes
     *
     * Merge two sorted lists together O(n+m)
     * @see mergeLists
     *
     * Construct a new tree from the merged list O(n+m)
     * @see constructFromSortedArray
     *
     * Total complexity:
     * Construct a sorted array A of all the entries in tree1: O(n)
     * Construct a sorted array B of all the entries in tree2: O(m)
     * Merge two sorted arrays into one sorted array: O(n+m)
     * Construct an avl tree of all the entries in C: O(n+m)
     *
     * The total time complexity is O(n) + O(m) + O(m+n) + O(m+n) = O(m+n)
     *
     * @param tree1 first tree to merge
     * @param tree2 second tree to merge
     * @return AVLTree
     */
    public static <K, V> AVLTree<K, V> merge(AVLTree<K,V> tree1,
                                             AVLTree<K,V> tree2 ) {
        NodePositionList<AVLNode<K, V>> list1 = new NodePositionList<>();
        NodePositionList<AVLNode<K, V>> list2 = new NodePositionList<>();

        inorderNodes((AVLNode<K, V>) tree1.root(), list1);
        inorderNodes((AVLNode<K, V>) tree2.root(), list2);
        ArrayList<Position<Entry<K, V>>> merged = mergeLists(list1, list2, tree1.C);

        AVLTree<K, V> mergedTree = new AVLTree<>();
        mergedTree.root = constructFromSortedArray(merged, 0, merged.size() - 1);
        mergedTree.numEntries = tree1.numEntries + tree2.numEntries;
        mergedTree.size = tree1.size + tree2.size;

        return mergedTree;
    }

    /**
     * Creates a new window and prints the AVL tree specified by the
     * parameter on the new window. Each internal node is displayed by a circle containing
     * its key and each external node is displayed by a rectangle.
     *
     * Does an inorder traversal to visit all the nodes to do the drawing O(n)
     *
     * @see AVLTreePanel
     *
     * @param tree the tree to display
     */
    public static <K, V> void print(AVLTree<K, V> tree) {
        JFrame frame = new JFrame("AVLTree Demo");
        frame.setSize(1400, 600);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.add(new AVLTreePanel<>(tree));
        frame.setVisible(true);
    }

    /**
     * Clones a tree at a given position using a postorder traversal
     *
     * The non-recursive part:
     *  - copies an existing position in the tree O(1)
     *  - links the copy to it's children O(1)
     *  - sets the height O(1)
     *
     * The recursive part: O(n)
     *  - visits every left child once
     *  - visits every right child once
     *
     * Total time complexity O(n)
     *
     *  To clone an entire tree the call should be postorderClone(tree, tree.root())
     *
     * @param tree the tree to clone
     * @param p the position to clone
     * @return AVLNode
     */
    private static <K, V> AVLNode<K, V> postorderClone(AVLTree<K, V> tree, Position<Entry<K, V>> p) {

        AVLNode<K, V> left = null;
        AVLNode<K, V> right = null;
        if (tree.hasLeft(p)) {
            left = postorderClone(tree, tree.left(p));
        }
        if (tree.hasRight(p)) {
            right = postorderClone(tree, tree.right(p));
        }
        AVLNode<K, V> clonedNode = copy(p);
        if (left != null) {
            clonedNode.setLeft(left);
            left.setParent(clonedNode);
        }
        if (right != null) {
            clonedNode.setRight(right);
            right.setParent(clonedNode);
        }
        if (left != null || right != null) {
            tree.setHeight(clonedNode);
        }
        return clonedNode;
    }

    /**
     * Constructs and returns an AVLNode, that can be used as the root of an AVLTree, from a sorted ArrayList.
     *
     * The non recursive part:
     *  - create a new node from the middle of the array passed in O(1)
     *  - link child and parent nodes together O(1)
     *
     * The recursive part: O(n)
     *  - divides the array by half and then recursively calls itself on each half until
     *    the array is divided into single elements O(n)
     *
     * Total time complexity O(n)
     *
     * @param sortedArray the array to build the node from
     * @param start starting position
     * @param end ending position
     * @return AVLNode
     */
    private static <K, V> AVLNode<K, V> constructFromSortedArray(
            ArrayList<Position<Entry<K, V>>> sortedArray,
            int start,
            int end)
    {
        if (start > end) {
            return null;
        } else {
            int middle = (start + end)/2;
            AVLNode<K, V> node = copy(sortedArray.get(middle));
            AVLNode<K, V> leftTerminal = new AVLNode<>();
            leftTerminal.setParent(node);
            AVLNode<K, V> rightTerminal = new AVLNode<>();
            rightTerminal.setParent(node);
            node.setHeight(1);
            node.setLeft(leftTerminal);
            node.setRight(rightTerminal);

            AVLNode<K, V> left = constructFromSortedArray(sortedArray, start, middle - 1);
            if (left != null) {
                node.setLeft(left);
                left.setParent(node);
                node.setHeight(Math.max(left.getHeight() + 1, node.getHeight()));
            }

            AVLNode<K, V> right = constructFromSortedArray(sortedArray, middle + 1, end);
            if (right != null) {
                node.setRight(right);
                right.setParent(node);
                node.setHeight(Math.max(right.getHeight() + 1, node.getHeight()));
            }

            return node;
        }
    }

    /**
     * Creates an inorder list of internal nodes
     * Inorder traversal O(n) - assuming the visit is O(1) which it is - The visit checks the element and calls addLast
     * which are both O(1)
     *
     * @param v node
     * @param nodes list
     */
    private static <K, V> void inorderNodes(AVLNode<K, V> v, NodePositionList<AVLNode<K, V>> nodes) {
        if (v.getLeft() != null) {
            inorderNodes((AVLNode<K, V>) v.getLeft(), nodes);
        }
        if (v.element() != null) {
            nodes.addLast(v);
        }
        if (v.getRight() != null) {
            inorderNodes((AVLNode<K, V>) v.getRight(), nodes);
        }
    }


    /**
     * Merges two sorted NodePositionList and returns a sorted ArrayList.
     *
     * Time complexity analysis:
     *
     * significant method calls:
     *  - merged.add() in general it is 'amortized constant time' but since we set the capacity on construction O(1)
     *
     * 3 Consectutive Loops:
     *  1) Loops until either a or b is empty O(m+n)
     *  2) Loops until a is empty O(m)
     *  3) Loops until b is empty O(n)
     *
     * Total time complexity is O(m) + O(n) + O(m+n) = O(m+n)
     *
     * @see net.datastructures.Sort::merge based on this method
     **/
    private static <K, V> ArrayList<Position<Entry<K, V>>> mergeLists(NodePositionList<AVLNode<K, V>> a,
                                                        NodePositionList<AVLNode<K, V>> b,
                                                        Comparator<K> c)
    {
        int totalSize = a.size() + b.size();
        ArrayList<Position<Entry<K, V>>> merged = new ArrayList<>(totalSize);
        while (!a.isEmpty() && !b.isEmpty()) {
            if (c.compare(a.first().element().element().getKey(), b.first().element().element().getKey()) <= 0) {
                merged.add(a.remove(a.first()));
            } else {
                merged.add(b.remove(b.first()));
            }
        }

        while(!a.isEmpty()) {
            merged.add(a.remove(a.first()));
        }

        while(!b.isEmpty()) {
            merged.add(b.remove(b.first()));
        }
        return merged;
    }

    /**
     * Creates a new AVLNode from a position
     * All calls are primitive hence the Big O is O(1)
     * @param p position to copy
     * @return AVLNode
     */
    private static <K, V> AVLNode<K, V> copy(Position<Entry<K, V>> p) {
        AVLNode<K, V> node = new AVLNode<>();
        Entry<K, V> e = p.element();
        if (e != null) {
            K key = e.getKey();
            V value = e.getValue();
            Entry<K,V> newEntry = new BSTEntry<>(key, value, null);
            ((BSTEntry<K,V>) newEntry).pos = node;
            node.setElement(newEntry);
        }
        return node;
    }

}

/**
 * Draws a component that represents the AVLTree that is passed in during construction
 */
class AVLTreePanel<K, V> extends JPanel {

    private static final int SPACE_BETWEEN = 40;
    private static final int INTERNAL_WIDTH = SPACE_BETWEEN;
    private static final int EXTERNAL_WIDTH = SPACE_BETWEEN/4;
    private AVLTree<K,V> t;

    AVLTreePanel(AVLTree<K, V> tree) {
        super();
        t = tree;
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        layout(t, t.root(), 10, 10, g);
    }

    /**
     * Draws the tree by traversing the tree inorder and drawing each node each visit
     *
     * O(n) - assuming all draw operations are constant time
     *
     * @param tree tree to draw
     * @param p position
     * @param y height
     * @param x width
     * @param g graphics
     * @return the next x coordinate
     */
    private int layout (AVLTree<K, V> tree, Position<Entry<K, V>> p, int y, int x, Graphics g) {

        if (tree.hasLeft(p)) {
            x = layout(tree, tree.left(p), y + SPACE_BETWEEN, x, g);
        }
        x += SPACE_BETWEEN;
        if (tree.isInternal(p)) {
            g.drawString(p.element().getKey().toString(), x, y + (INTERNAL_WIDTH/2));
            g.drawOval(x - (INTERNAL_WIDTH/4), y, INTERNAL_WIDTH, INTERNAL_WIDTH);
            // g.drawString(String.valueOf(tree.height(p)), x, y);
        }
        if (tree.isExternal(p)) {
            g.drawRect(x + EXTERNAL_WIDTH/2, y, EXTERNAL_WIDTH, EXTERNAL_WIDTH);
        }
        if (tree.hasRight(p)) {
            x = layout(tree, tree.right(p), y + SPACE_BETWEEN, x, g);
        }
        return x;
    }
}