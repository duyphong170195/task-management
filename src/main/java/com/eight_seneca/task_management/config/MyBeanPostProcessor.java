package com.eight_seneca.task_management.config;

import com.eight_seneca.task_management.service.TaskService;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof TaskService) {
            System.out.println("Wrapping MyService");
        }
        return bean;
    }
}
