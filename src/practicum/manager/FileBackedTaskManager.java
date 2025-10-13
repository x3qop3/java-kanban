package practicum.manager;

import practicum.statandtype.Status;
import practicum.statandtype.TaskType;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    private TaskType.Type getTaskType(Task task) {
        return TaskType.fromTask(task);
    }

    public  void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,epic,duration(min),startTime");

            getAllTasks().forEach(task -> lines.add(taskToString(task)));
            getAllEpics().forEach(epic -> lines.add(taskToString(epic)));
            getAllSubtasks().forEach(subtask -> lines.add(taskToString(subtask)));

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }


    private String taskToString(Task task) {
        TaskType.Type type = getTaskType(task);
        String epicId = (type == TaskType.Type.SUBTASK) ?
                String.valueOf(((Subtask) task).getEpicId()) : "";

        String durationStr = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ?
                task.getStartTime().format(DATE_TIME_FORMATTER) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                type.name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr
        );
    }


    public FileBackedTaskManager(File file) {
        this.file = file;
        if (file.exists()) {
            loadFromFile();
        }
    }
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists()) return manager;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return manager;

            for (int i = 1; i < lines.size(); i++) {
                Task task = manager.taskFromString(lines.get(i));
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.createEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.createSubtask((Subtask) task);
                    } else {
                        manager.createTask(task);
                    }
                }
            }
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }


    private void loadFromFile() {
        if (!file.exists()) return;

        super.deleteAllTasks();
        super.deleteAllEpics();
        super.deleteAllSubtasks();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return;

            for (int i = 1; i < lines.size(); i++) {
                Task task = taskFromString(lines.get(i));
                if (task != null) {
                    if (task instanceof Epic) {
                        super.createEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        super.createSubtask((Subtask) task);
                    } else {
                        super.createTask(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }


        private Task taskFromString(String line) {
            String[] parts = line.split(",");
            if (parts.length < 8) return null;

            try {
                int id = Integer.parseInt(parts[0]);
                TaskType.Type type = TaskType.Type.valueOf(parts[1]);
                String name = parts[2];
                Status status = Status.valueOf(parts[3]);
                String description = parts[4];
                Duration duration = parts[6].isEmpty() ?
                        null : Duration.ofMinutes(Long.parseLong(parts[6]));
                LocalDateTime startTime = parts[7].isEmpty() ?
                        null : LocalDateTime.parse(parts[7], DATE_TIME_FORMATTER);

                switch (type) {
                    case TASK:
                        return new Task(id, name, description, status, duration, startTime);
                    case EPIC:
                        Epic epic = new Epic(id, name, description);
                        epic.setStatus(status);
                        return epic;
                    case SUBTASK:
                        int epicId = Integer.parseInt(parts[5]);
                        return new Subtask(id, name, description, status, epicId, duration, startTime);
                    default:
                        return null;
                }
            } catch (Exception e) {
                throw new ManagerSaveException("Ошибка парсинга", e);
            }
        }


    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
    public static FileBackedTaskManager createSampleManager(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Task 1", "Описание 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Описание 2", Status.IN_PROGRESS,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(2));

        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic = new Epic("Epic 1", "Большой эпик");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Часть 1", Status.NEW,
                epic.getId(), Duration.ofHours(2), LocalDateTime.now().plusDays(1));
        Subtask subtask2 = new Subtask("Subtask 2", "Часть 2", Status.DONE,
                epic.getId(), Duration.ofHours(3), LocalDateTime.now().plusDays(2));

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        return manager;
    }
}


class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}