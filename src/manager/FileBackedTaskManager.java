package manager;

import exceptions.ManagerSaveException;
import taskobject.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;


public class FileBackedTaskManager extends InMemoryTaskManager {
    Path path;

    public FileBackedTaskManager(String fileName) {
        try {
            this.path = Paths.get(fileName);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try (Reader reader = new FileReader(file)) {
            BufferedReader br = new BufferedReader(reader);
            String head = br.readLine();
            while (br.ready()) {
                Task task = manager.fromString(br.readLine());
                manager.loadTask(task);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения из файла", e);
        }

        return manager;
    }

    void save() {
        try (BufferedWriter bufferWrite = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {

            bufferWrite.write("id,type,name,status,description,start time,duration,epic\n");
            for (Task task : getAllTaskAllType()) {

                bufferWrite.write(toString(task));

            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл", e);
        }

    }

    String toString(Task task) {
        if (task instanceof EpicTask) {
            return task.getId() + "," + TaskType.EPICTASK + "," + task.getTitle() + "," + task.getStatus() + "," +
                    task.getDescription() + "," +
                    (task.getStartTime() != null ? task.getStartTime().format(task.form) : "не указано") +
                    "," + (task.getDuration() != null ? task.durationInMinute(task.getDuration()) : "не указано") + "\n";
        } else if (task instanceof SubTask) {
            return task.getId() + "," + TaskType.SUBTASK + "," + task.getTitle() + "," + task.getStatus() + "," +
                    task.getDescription() + "," +
                    (task.getStartTime() != null ? task.getStartTime().format(task.form) : "не указано") + "," +
                    (task.getDuration() != null ? task.durationInMinute(task.getDuration()) : "не указано") +
                    "," + ((SubTask) task).getEpicId() + "\n";
        } else {
            return task.getId() + "," + TaskType.TASK + "," + task.getTitle() + "," + task.getStatus() + "," +
                    task.getDescription() + "," +
                    (task.getStartTime() != null ? task.getStartTime().format(task.form) : "не указано") +
                    "," + (task.getDuration() != null ? task.durationInMinute(task.getDuration()) : "не указано") + "\n";
        }
    }

    Task fromString(String value) {
        String[] str = value.split(",");
        Task task;
        if (TaskType.valueOf(str[1]) == TaskType.SUBTASK) {
            task = new SubTask(Integer.parseInt(str[7]), str[2], str[4], Status.valueOf(str[3]));
            if (!str[5].equals("не указано")) {
                task.setStartTime(LocalDateTime.parse(str[5], task.form));
                task.setDuration(Duration.ofMinutes(Long.parseLong(str[6])));
            }
        } else if (TaskType.valueOf(str[1]) == TaskType.EPICTASK) {
            task = new EpicTask(str[2], str[4]);
            task.setStatus(Status.valueOf(str[3]));
            if (!str[5].equals("не указано")) {
                task.setStartTime(LocalDateTime.parse(str[5], task.form));
                task.setDuration(Duration.ofMinutes(Long.parseLong(str[6])));
            }
        } else {
            task = new Task(str[2], str[4], Status.valueOf(str[3]));
            if (!str[5].equals("не указано")) {
                task.setStartTime(LocalDateTime.parse(str[5], task.form));
                task.setDuration(Duration.ofMinutes(Long.parseLong(str[6])));
            }
        }
        task.setId(Integer.parseInt(str[0]));

        return task;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void removeAllTask() {
        super.removeAllTask();
        save();
    }

    @Override
    public void removeAllEpic() {
        super.removeAllEpic();
        save();
    }

    @Override
    public void removeAllSub() {
        super.removeAllSub();
        save();
    }

    @Override
    public void removeAllTasksAllType() {
        super.removeAllTasksAllType();
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSub(int id) {
        super.removeSub(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        super.updateEpicTask(epicTask);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void setEpicStatus(EpicTask epicTask) {
        super.setEpicStatus(epicTask);
        save();
    }

    @Override
    public void setEpicTime(EpicTask epicTask) {
        super.setEpicTime(epicTask);
        save();
    }

}
