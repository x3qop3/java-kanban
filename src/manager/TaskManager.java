package manager;

import taskobject.EpicTask;
import taskobject.SubTask;
import taskobject.Task;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TaskManager {

    Task getTask(int id);

    SubTask getSubTask(int id);

    EpicTask getEpicTask(int id);

    List<Task> getHistory();

    void clearHistory();

    List<Task> getAllTask();

    List<EpicTask> getAllEpicTask();

    List<SubTask> getAllSubTask();

    List<SubTask> getAllSubTasksByEpicId(int epicId);

    List<Task> getAllTaskAllType();

    Set<Task> getPrioritizedTasks();

    Map<Integer, SubTask> getSubMap();

    Map<Integer, Task> getTaskMap();

    Map<Integer, EpicTask> getEpicMap();

    void createTask(Task task);

    void removeAllTask();

    void removeAllEpic();

    void removeAllSub();

    void removeTask(int id);

    void removeEpic(int id);

    void removeSub(int id);

    void removeAllTasksAllType();

    void removeHistoryItem(int id);

    void updateTask(Task task);

    void updateEpicTask(EpicTask epicTask);

    void updateSubTask(SubTask subTask);

    boolean isTaskCross(Task taskA, Task taskB);

    boolean hasCross(Task task);


}
