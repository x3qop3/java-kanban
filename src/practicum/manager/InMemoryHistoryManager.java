package practicum.manager;

import practicum.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }

    private final Map<Integer, Node> taskIdToNode = new HashMap<>();


    private Node head;
    private Node tail;


    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        Node newNode = linkLast(task);
        taskIdToNode.put(task.getId(), newNode);
    }


    @Override
    public void remove(int id) {
        Node node = taskIdToNode.get(id);
        if (node != null) {
            removeNode(node);
            taskIdToNode.remove(id);
        }
    }


    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            head = newNode;
        } else {

            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        return newNode;
    }


    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }
}