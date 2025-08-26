package manager;

import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;
import taskobject.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, EpicTask> epicMap = new HashMap<>();
    private final Map<Integer, SubTask> subMap = new HashMap<>();
    private final Set<Task> prioritySet = new TreeSet<>(Comparator
            .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Task::getId));
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idNumber = 0;

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistList());
    }

    public Map<Integer, Task> getTaskMap() {
        return taskMap;
    }

    public Map<Integer, EpicTask> getEpicMap() {
        return epicMap;
    }

    public void removeHistoryItem(int id) {
        historyManager.removeView(id);
    }

    @Override
    public void clearHistory() {
        historyManager.clearList();
    }

    public Map<Integer, SubTask> getSubMap() {
        return subMap;
    }

    private int assignId() { //метод для присвоения Ид
        idNumber++;
        return idNumber;
    }

    private void resetId() {
        idNumber = 0;
    }


    @Override
    public Task getTask(int id) { //метод для получения обычной задачи

        if (taskMap.containsKey(id)) {
            historyManager.add(taskMap.get(id));
            return taskMap.get(id);
        } else return null;

    }

    @Override
    public EpicTask getEpicTask(int id) {
        if (epicMap.containsKey(id)) {
            historyManager.add(epicMap.get(id));
            return epicMap.get(id);
        } else return null;
    }

    @Override
    public SubTask getSubTask(int id) {
        if (subMap.containsKey(id)) {
            historyManager.add(subMap.get(id));
            return subMap.get(id);
        } else return null;
    }

    @Override
    public List<Task> getAllTask() {  //метод для получения всех обычных задач
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<EpicTask> getAllEpicTask() { //метод для получения всех эпиков
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTask() { //метод для получения всех подзадачи из всех эпиков
        return new ArrayList<>(subMap.values());
    }

    @Override
    public List<Task> getAllTaskAllType() { //получения всех задач всех типов
        return Stream.of(taskMap, epicMap, subMap)
                .flatMap(map -> map.values().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritySet;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicId(int epicId) { //метод для получения списка подзадач из эпика

        return epicMap.get(epicId).getSubTasksId().stream()
                .map(subMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public void createTask(Task task) { //метод для создания любой задачи
        task.setId(assignId());

        if (task instanceof EpicTask epic) {
            epicMap.put(epic.getId(), epic);
            return;
        }

        if (!hasCross(task)) {
            if (task instanceof SubTask subTask) {
                if (epicMap.containsKey(subTask.getEpicId())) {
                    getSubMap().put(subTask.getId(), subTask); //сначала кладем подзадачу в общее хранилище подзадач

                    epicMap.get(subTask.getEpicId()).setSubTasksId(subTask.getId());
                    setEpicStatus(epicMap.get(subTask.getEpicId()));//кладем подзадачу в соответствующий эпик

                    if (subTask.getStartTime() != null) {
                        prioritySet.add(subTask);
                        setEpicTime(epicMap.get(subTask.getEpicId()));
                    }
                }
            } else {
                taskMap.put(task.getId(), task);
                if (task.getStartTime() != null) prioritySet.add(task);
            }
        } else {
            throw new IllegalArgumentException("Время выполнения пересекается с другой задачей");
        }


    }


    @Override
    public void removeAllTask() {                                                     //метод для удаления всех задач
        taskMap.keySet().forEach(historyManager::removeView);
        taskMap.values().forEach(prioritySet::remove);
        taskMap.clear();
    }

    @Override
    public void removeAllEpic() {                                                   //метод для удаления всех эпик задач
        epicMap.keySet().forEach(historyManager::removeView);
        subMap.keySet().forEach(historyManager::removeView);
        subMap.values().forEach(prioritySet::remove);
        epicMap.clear();
        subMap.clear();
    }

    @Override
    public void removeAllSub() { //метод для удаления всех подзадач

        subMap.keySet().forEach(historyManager::removeView);
        subMap.values().forEach(prioritySet::remove);
        epicMap.values().forEach(epic -> {
                    epic.getSubTasksId().clear();
                    setEpicStatus(epic);
                    setEpicTime(epic);
                }
        );
        subMap.clear();
    }

    public void removeAllTasksAllType() {
        historyManager.clearMap();
        historyManager.clearList();
        prioritySet.clear();
        taskMap.clear();
        epicMap.clear();
        subMap.clear();
        resetId();
    }

    @Override
    public void removeTask(int id) { //метод для удаления по Ид
        prioritySet.remove(taskMap.get(id));
        taskMap.remove(id);
        historyManager.removeView(id);
    }

    @Override
    public void removeEpic(int id) {
        epicMap.get(id).getSubTasksId().forEach(i -> {
            prioritySet.remove(subMap.get(i));
            subMap.remove(i);
            historyManager.removeView(i);
        });
        epicMap.remove(id);
        historyManager.removeView(id);

    }

    @Override
    public void removeSub(int id) {
        if (subMap.containsKey(id)) {
            epicMap.get(subMap.get(id).getEpicId()).removeSubId(id);

            setEpicStatus(epicMap.get(subMap.get(id).getEpicId()));
            setEpicTime(epicMap.get(subMap.get(id).getEpicId()));

            prioritySet.remove(subMap.get(id));
            subMap.remove(id);
        }
        historyManager.removeView(id);
    } //данный метод при передаче айди удаляет подзадачу как из эпика, так и из хранилища со всеми подзадачами


    @Override
    public void updateTask(Task task) { //метод для обновления обычной задачи
        if (!hasCross(task)) {
            if (taskMap.containsKey(task.getId())) {
                prioritySet.remove(taskMap.get(task.getId()));
                taskMap.put(task.getId(), task);
                if (task.getStartTime() != null) prioritySet.add(task);
            }
        } else {
            throw new IllegalArgumentException("Время выполнения пересекается с другой задачей");
        }
    }


    public void updateEpicTask(EpicTask epicTask) {

        if (epicMap.containsKey(epicTask.getId())) {
            epicMap.get(epicTask.getId()).setTitle(epicTask.getTitle());
            epicMap.get(epicTask.getId()).setDescription(epicTask.getDescription());
        }


    }


    public void updateSubTask(SubTask subTask) { //метод для обновления подзадачи
        if (!hasCross(subTask)) {
            if (subMap.containsKey(subTask.getId()) && epicMap.containsKey(subTask.getEpicId()) &&
                    subMap.get(subTask.getId()).getEpicId() == subTask.getEpicId()) {
                prioritySet.remove(subMap.get(subTask.getId()));
                subMap.put(subTask.getId(), subTask);
                epicMap.get(subTask.getEpicId()).setSubTasksId(subTask.getId());
                setEpicStatus(epicMap.get(subTask.getEpicId()));
                setEpicTime(epicMap.get(subTask.getEpicId()));
                if (subTask.getStartTime() != null) prioritySet.add(subTask);

            }
        } else {
            throw new IllegalArgumentException("Время выполнения пересекается с другой задачей");
        }
    }

    public void setEpicStatus(EpicTask epicTask) { //метод для обновления статуса эпика
        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasProgress = false;

        for (int i : epicTask.getSubTasksId()) {

            if (subMap.get(i).getStatus() == Status.NEW) {
                hasNew = true;
            } else if (subMap.get(i).getStatus() == Status.DONE) {
                hasDone = true;
            } else if (subMap.get(i).getStatus() == Status.IN_PROGRESS) {
                hasProgress = true;
            }

        }
        if (hasNew && hasDone || hasProgress) {
            epicTask.setStatus(Status.IN_PROGRESS);
        } else {
            epicTask.setStatus(hasDone ? Status.DONE : Status.NEW);
        }
    }

    public void setEpicTime(EpicTask epicTask) {
        Optional<LocalDateTime> startTime = epicTask.getSubTasksId().stream()
                .map(subMap::get)
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        epicTask.setStartTime(startTime.orElse(null));


        long minutes = epicTask.getSubTasksId().stream()
                .map(subMap::get)
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        Duration duration = Duration.ofMinutes(minutes);
        epicTask.setDuration(duration);

        Optional<LocalDateTime> endTime = epicTask.getSubTasksId().stream()
                .map(subMap::get)
                .filter(task -> task.getStartTime() != null)
                .map(Task::getEndTime)
                .max(Comparator.naturalOrder());
        epicTask.setEndTime(endTime.orElse(null));


    }


    protected void loadTask(Task task) {
        if (task.getId() > idNumber) idNumber = task.getId();
        if (task instanceof EpicTask epic) {
            epicMap.put(epic.getId(), epic);
        } else if (task instanceof SubTask sub) {
            subMap.put(sub.getId(), sub);
            epicMap.get(sub.getEpicId()).setSubTasksId(sub.getId());
        } else {
            taskMap.put(task.getId(), task);
        }

        if (task.getStartTime() != null && !(task instanceof EpicTask)) prioritySet.add(task);
    }

    public boolean isTaskCross(Task taskA, Task taskB) {                  //МЕТОД ПРОВЕРЯЕТ ПЕРЕСЕЧЕНИЕ ЗАДАЧ ПО ВРЕМЕНИ
        return taskA.getStartTime() != null && taskA.getEndTime() != null &&
                taskB.getStartTime() != null && taskB.getEndTime() != null &&
                (taskA.getStartTime().isBefore(taskB.getEndTime()) && taskA.getEndTime().isAfter(taskB.getStartTime()));
    }

    public boolean hasCross(Task task) {                                    // МЕТОД ПРОВЕРЯЕТ НАЛИЧИЕ ПЕРЕСЕЧЕНИЙ В СПИСКЕ ЗАДАЧ ПО ПРИОРИТЕТУ
        if (task.getStartTime() == null) return false;
        return prioritySet.stream()
                .filter(arg -> arg.getStartTime() != null)
                .filter(arg -> arg.getId() != task.getId())
                .takeWhile(arg -> !arg.getStartTime().isAfter(task.getEndTime()))
                .anyMatch(arg -> isTaskCross(arg, task));
    }


}
