package practicum.statandtype;

import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

public class TaskType {
    public enum Type {
        TASK,
        SUBTASK,
        EPIC
    }

    public static Type fromTask(Task task) {
        if (task instanceof Epic) return Type.EPIC;
        if (task instanceof Subtask) return Type.SUBTASK;
        return Type.TASK;
    }
}