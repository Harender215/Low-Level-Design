import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

class DoublyLinkedList<K, V> {
    private Node<K, V> head;
    private Node<K, V> tail;

    public DoublyLinkedList() {
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public void moveToFront(Node<K, V> node) {
        remove(node);
        addToFront(node);
    }

    public void addToFront(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public void remove(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public Node<K, V> removeLast() {
        if (tail.prev == head) return null;
        Node<K, V> lru = tail.prev;
        remove(lru);
        return lru;
    }
}

class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> cacheMap;
    private final DoublyLinkedList<K, V> dll;
    private final ReentrantLock lock = new ReentrantLock();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.dll = new DoublyLinkedList<>();
    }

    public V get(K key) {
        lock.lock();
        try {
            if (!cacheMap.containsKey(key)) return null;
            Node<K, V> node = cacheMap.get(key);
            dll.moveToFront(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            if (cacheMap.containsKey(key)) {
                Node<K, V> node = cacheMap.get(key);
                node.value = value;
                dll.moveToFront(node);
            } else {
                if (cacheMap.size() >= capacity) {
                    Node<K, V> lru = dll.removeLast();
                    if (lru != null) cacheMap.remove(lru.key);
                }
                Node<K, V> newNode = new Node<>(key, value);
                dll.addToFront(newNode);
                cacheMap.put(key, newNode);
            }
        } finally {
            lock.unlock();
        }
    }
}


public class Main {
    public static void main(String[] args) {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        System.out.println(cache.get(2)); // B
        cache.put(4, "D"); // 1 is evicted
        System.out.println(cache.get(1)); // null
    }
}
