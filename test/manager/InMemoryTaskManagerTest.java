package manager;

import org.junit.jupiter.api.AfterEach;


class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @AfterEach
    void clearAll() {
        manager.removeAllTasksAllType();
    }

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

}