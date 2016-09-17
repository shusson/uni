package net.datastructures;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.Comparator;


class TestPanel<K, V> extends JPanel {

    private AVLTree<K,V> t;

    TestPanel(AVLTree<K, V> tree) {
        super();
        t = tree;
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        layout(t, t.root(), 10, 10, g);
    }

    private int layout (AVLTree<K, V> t, Position<Entry<K, V>> p, int d, int x, Graphics g) {
        if (t.hasLeft(p)) {
            x = layout(t, t.left(p), d + 30, x, g);
        }
        x += 30;
        if (t.isInternal(p)) {
            g.drawOval(x, d, 20, 20);
        }
        if (t.isExternal(p)) {
            g.drawRect(x + 5, d, 10, 10);
        }
        if (t.hasRight(p)) {
            x = layout(t, t.right(p), d + 30, x, g);
        }
        return x;
    }


    public void draw(Graphics g) {
        Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        g.setColor(c);
        g.fillRect((int) (Math.random() * 400), (int) (Math.random() * 300), (int) (Math.random() * 40), (int) (Math.random() * 40));
    }
}

/**
 * Created by shane on 17/09/2016.
 */
public class ExtendedAVLTree<K, V> extends AVLTree<K, V> {

    /**
     * Creates a new window and prints the AVL tree specified by the
     * parameter on the new window. Each internal node is displayed by a circle containing
     * its key and each external node is displayed by a rectangle.
     * @param tree
     * @param <K>
     * @param <V>
     */
    public static <K, V> void print(AVLTree<K, V> tree) {
        JFrame frame = new JFrame("FrameDemo");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.add(new TestPanel<>(tree));
        frame.setVisible(true);
    }

    /**
     * creates an identical copy of the AVL tree specified by the
     * parameter and returns a reference to the new AVL tree
     *
     * Time complexity of this method is O(n)
     * @param tree the tree to clone
     * @param <K> key
     * @param <V> value
     * @return AVLTree
     */
    public static <K, V> AVLTree<K, V> clone(AVLTree<K, V> tree) throws InvalidPositionException {
        AVLTree<K, V> clone = new AVLTree<>();
        clone.root = postorderClone((AVLNode<K, V>)tree.root());
        return clone;
    }

    /**
     *
     * @param v
     */
    private static <K, V> AVLNode<K, V> postorderClone(AVLNode<K, V> v) {

        AVLNode<K, V> left = null;
        AVLNode<K, V> right = null;
        if (v.getLeft() != null) {
            left = postorderClone((AVLNode<K, V>) v.getLeft());
        }
        if (v.getRight() != null) {
            right = postorderClone((AVLNode<K, V>) v.getRight());
        }

        AVLNode<K, V> clonedNode = copy(v);
        if (left != null) {
            clonedNode.setLeft(left);
            left.setParent(clonedNode);
        }
        if (right != null) {
            clonedNode.setRight(right);
            right.setParent(clonedNode);
        }

        return clonedNode;
    }

    /**
     * Merges two AVL trees, tree1 and tree2, into a new tree.
     *
     * Assumes that both trees have the same type of key K
     * @param tree1
     * @param tree2
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> AVLTree<K, V> merge(AVLTree<K,V> tree1,
                                             AVLTree<K,V> tree2 ) {
        NodePositionList<AVLNode<K, V>> list1 = new NodePositionList<>();
        NodePositionList<AVLNode<K, V>> list2 = new NodePositionList<>();

        inorderNodes((AVLNode<K, V>) tree1.root(), list1);
        inorderNodes((AVLNode<K, V>) tree2.root(), list2);
        Position<Entry<K, V>>[] merged = merge(list1, list2, tree1.C);

        AVLTree<K, V> mergedTree = new AVLTree<>();
        mergedTree.root = new AVLNode<>();
        constructFromSortedArray(merged, 0, merged.length - 1);
        return mergedTree;
    }

    /**
     * Creates an inorder list of non-terminal nodes
     * @param v node
     * @param nodes list
     * @throws InvalidPositionException
     */
    private static <K, V> void inorderNodes(AVLNode<K, V> v, NodePositionList<AVLNode<K, V>> nodes) {
        if (v.getLeft() != null) {
            inorderNodes((AVLNode<K, V>) v.getLeft(), nodes);  // recurse on left child
        }
        if (v.element() != null) {
            nodes.addLast(v);
        }
        if (v.getRight() != null) {
            inorderNodes((AVLNode<K, V>) v.getRight(), nodes); // recurse on right child
        }
    }

    /**
     *
     * @param merged
     * @param start
     * @param end
     * @param <K>
     * @param <V>
     * @return
     */
    private static <K, V> AVLNode<K, V> constructFromSortedArray(Position<Entry<K, V>>[] merged, int start, int end) {
        int length = end - start;
        if (length == 0) {
            return null;
        } else if (length == 1) {
            AVLNode<K, V> v = (AVLNode<K, V>) merged[start];
            return copy(v);
        } else {
            int middle = length/2;
            AVLNode<K, V> left = null;
            AVLNode<K, V> right = null;
            AVLNode<K, V> v = (AVLNode<K, V>) merged[middle];
            AVLNode<K, V> node = copy(v);
            if ((middle - 1) > 0) {
                left = constructFromSortedArray(merged, 0, middle - 1);
                node.setLeft(left);
                left.setParent(node);
            }
            if (((middle + 1) - end) > 0) {
                right = constructFromSortedArray(merged, middle + 1, end - 1);
                node.setRight(right);
                right.setParent(node);
            }
            return node;
        }
    }

    /**
     * Merges two sorted NodePositionList and returns a sorted array Position<Entry<K, V>>[].
     *
     * @see Sort::merge based on this method
     **/
    private static <K, V> Position<Entry<K, V>>[] merge(NodePositionList<AVLNode<K, V>> a,
                                                        NodePositionList<AVLNode<K, V>> b,
                                                        Comparator<K> c)
    {
        int totalSize = a.size() + b.size();
        Position<Entry<K, V>>[] merged = (Position<Entry<K, V>>[]) Array.newInstance(Position.class, totalSize);
        int index = 0;
        while (!a.isEmpty() && !b.isEmpty()) {
            if (c.compare(a.first().element().element().getKey(), b.first().element().element().getKey()) <= 0) {
                merged[index] = a.remove(a.first());
            } else {
                merged[index] = b.remove(b.first());
            }
            ++index;
        }

        while(!a.isEmpty()) {
            merged[index] = a.remove(a.first());
            ++index;
        }

        while(!b.isEmpty()) {
            merged[index] = b.remove(b.first());
            ++index;
        }
        return merged;
    }

    private static <K, V> AVLNode<K, V> copy(AVLNode<K, V> v) {
        AVLNode<K, V> node = new AVLNode<>();
        Entry<K, V> e = v.element();
        if (e != null) {
            K key = e.getKey();
            V value = e.getValue();
            Entry<K,V> newEntry = new BSTEntry<>(key, value, null);
            ((BSTEntry<K,V>) newEntry).pos = node;
            node.setElement(newEntry);
        }
        node.setHeight(v.getHeight());
        return node;
    }

}
