package manager;

import taskobject.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final HandLinkedList<Task> histList = new HandLinkedList<>();
    private final Map<Integer, HandLinkedList.Node<Task>> histMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (histMap.containsKey(task.getId())) {
            histList.removeNode(histMap.get(task.getId()));
            histMap.remove(task.getId());
        }


        Task copyTask = new Task(task.getTitle(), task.getDescription(), task.getStatus());
        copyTask.setId(task.getId());
        histList.linkLast(copyTask);
        histMap.put(task.getId(), histList.getTail());
    }


    @Override
    public List<Task> getHistList() {
        return new ArrayList<>(histList.getTasks());
    }

    @Override
    public void removeView(int id) {
        if (histMap.containsKey(id)) {
            histList.removeNode(histMap.get(id));
        }
        histMap.remove(id);
    }

    @Override
    public void clearMap() {
        histMap.clear();
    }

    @Override
    public void clearList() {
        histList.clear();
    }


    public static class HandLinkedList<T> {

        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        public int getSize() {
            return size;
        }

        public void linkLast(T t) {
            final Node<T> oldTail = tail;
            final Node<T> newTask = new Node<>(oldTail, t, null);
            tail = newTask;
            if (oldTail == null) head = newTask;
            else oldTail.next = newTask;
            size++;
        }

        public Node<T> getTail() {
            return tail;
        }

        public ArrayList<Task> getTasks() {
            ArrayList<Task> tasks = new ArrayList<>();
            if (head != null) {
                Node<T> node = head;
                while (node != null) {
                    tasks.add((Task) node.data);
                    node = node.next;
                }
            }
            return tasks;
        }

        public void removeNode(Node<T> node) {
            if (node.prev == null && node.next == null) {
                tail = null;
                head = null;
            } else if (node.prev != null && node.next == null) {
                node.prev.next = null;
                tail = node.prev;
            } else if (node.prev == null && node.next != null) {
                node.next.prev = null;
                head = node.next;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
            size--;

        }

        public void clear() {
            Node<T> current = head;
            while (current != null) {
                Node<T> next = current.next;
                current.prev = null;
                current.next = null;
                current.data = null;
                current = next;
            }
            head = null;
            tail = null;
            size = 0;
        }

        static class Node<T> {
            public T data;
            public Node<T> prev;
            public Node<T> next;

            public Node(Node<T> prev, T data, Node<T> next) {
                this.data = data;
                this.next = next;
                this.prev = prev;
            }
        }


    }


}
