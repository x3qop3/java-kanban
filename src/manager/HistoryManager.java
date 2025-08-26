package manager;

import taskobject.Task;

import java.util.List;


public interface HistoryManager {

    void add(Task task);

    List<Task> getHistList();

    void removeView(int id);

    void clearMap();

    void clearList();
}
