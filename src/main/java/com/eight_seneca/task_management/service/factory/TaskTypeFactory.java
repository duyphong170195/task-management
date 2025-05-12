package com.eight_seneca.task_management.service.factory;

import com.eight_seneca.task_management.service.TaskTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskTypeFactory {

    private final Map<String, TaskTypeHandler> serviceMap;

    @Autowired
    public TaskTypeFactory(Map<String, TaskTypeHandler> serviceMap) {
        this.serviceMap = serviceMap;
    }

    public TaskTypeHandler getTaskTypeHandler(String type) {
        TaskTypeHandler service = serviceMap.get(type);
        if (service == null) {
            throw new IllegalArgumentException("Unknown task type: " + type);
        }
        return service;
    }
}
