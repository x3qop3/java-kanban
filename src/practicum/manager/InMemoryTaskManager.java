package practicum.manager;

import practicum.statandtype.Status;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public  class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()))
    );

    private int generateId() {
        return nextId++;
    }

    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }
        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newStart.plus(newTask.getDuration());
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .filter(task -> task.getId() != newTask.getId())
                .anyMatch(existing -> {
                    LocalDateTime existingStart = existing.getStartTime();
                    LocalDateTime existingEnd = existingStart.plus(existing.getDuration());
                    return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
                });
    }


    private void updateEpicTime(Epic epic) {
        List<Subtask> epicSubtasks = getSubtasksByEpic(epic.getId());
        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            return;
        }
        LocalDateTime start = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime end = epicSubtasks.stream()
                .map(subtask -> subtask.getStartTime().plus(subtask.getDuration()))
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (start != null && end != null) {
            epic.setStartTime(start);
            epic.setDuration(Duration.between(start, end));
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Хадача не может быть null");
        }
        if (hasTimeOverlap(task)) {
            throw new IllegalStateException("Время выполнения совпадает с существующими задачами");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Неверная задача");
        }
        Task existingTask = tasks.get(task.getId());
        prioritizedTasks.remove(existingTask);
        if (hasTimeOverlap(task)) {
            prioritizedTasks.add(existingTask);
            throw new IllegalStateException("Время задачи совпадает с существующими задачами");
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            historyManager.remove(epic.getId());
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Некорректный Эпик");
        }
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setTitle(epic.getTitle());
        existingEpic.setDescription(epic.getDescription());
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(subtask -> {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        });
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic);
        });
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик не найден для подзадачи");
        }
        if (hasTimeOverlap(subtask)) {
            throw new IllegalStateException("Время подзадачи перекрывается с существующими задачами");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(subtask.getEpicId());
        updateEpicTime(epics.get(subtask.getEpicId()));
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Неверная подзадача");
        }
        Subtask existingSubtask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(existingSubtask);
        if (hasTimeOverlap(subtask)) {
            prioritizedTasks.add(existingSubtask);
            throw new IllegalStateException("Время подзадач пересекается с существующими задачами");
        }
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(subtask.getEpicId());
        updateEpicTime(epics.get(subtask.getEpicId()));
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                updateEpicStatus(epic.getId());
                updateEpicTime(epic);
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

   public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getSubtasksByEpic(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
    @Override
    public Status getEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        return epic != null ? epic.getStatus() : null;
    }
    @Override
    public boolean isTimeSlotAvailable(LocalDateTime startTime, Duration duration) {
        if (startTime == null || duration == null) return true;
        LocalDateTime endTime = startTime.plus(duration);
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .noneMatch(task -> {
                    LocalDateTime taskStart = task.getStartTime();
                    LocalDateTime taskEnd = taskStart.plus(task.getDuration());
                    return startTime.isBefore(taskEnd) && taskStart.isBefore(endTime);
                });
    }
    @Override
    public void updateSubtaskTime(int subtaskId, LocalDateTime startTime, Duration duration) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            subtask.setStartTime(startTime);
            subtask.setDuration(duration);
            if (!hasTimeOverlap(subtask)) {
                if (startTime != null) {
                    prioritizedTasks.add(subtask);
                }
                updateEpicTime(epics.get(subtask.getEpicId()));
            } else {
                // Вернуть предыдущие значения, если есть пересечение
                subtask.setStartTime(subtask.getStartTime());
                subtask.setDuration(subtask.getDuration());
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.add(subtask);
                }
                throw new IllegalStateException("Время подзадач пересекается с существующими задачами");
            }
        }
    }
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
    @Override
    public boolean containsTask(int id) {
        return tasks.containsKey(id); // предполагая, что tasks - это Map<Integer, Task>
    }

    @Override
    public boolean containsEpic(int id) {
        return epics.containsKey(id);
    }

    @Override
    public boolean containsSubtask(int id) {
        return subtasks.containsKey(id);
    }

    @Override
    public void updateTaskTime(int taskId, LocalDateTime startTime, Duration duration) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setStartTime(startTime);
            task.setDuration(duration);
        }
    }
    private void updateEpicParameters(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getSubtasksByEpic(epicId);

        // Обновление статуса
        updateEpicStatus(epicId);

        // Обновление времени
        if (subtasks.isEmpty()) {
            epic.updateDuration(null);
            epic.updateStartTime(null);
            epic.updateEndTime(null);
            return;
        }

        LocalDateTime start = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Вычисление времени окончания
        LocalDateTime end = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Вычисление продолжительности
        Duration duration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.updateStartTime(start);
        epic.updateEndTime(end);
        epic.updateDuration(duration.equals(Duration.ZERO) ? null : duration);
    }




}