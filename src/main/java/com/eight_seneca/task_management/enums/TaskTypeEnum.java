package com.eight_seneca.task_management.enums;

public enum TaskTypeEnum {
    BUG("bug"),
    FEATURE("feature");

    private final String value;

    TaskTypeEnum(String value) {
        this.value = value;
    }

    public String getName() {
        return this.value;
    }

    public static TaskTypeEnum fromString(String text) {
        for (TaskTypeEnum b : TaskTypeEnum.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static String fromEnum(TaskTypeEnum expiredType) {
        for (TaskTypeEnum b : TaskTypeEnum.values()) {
            if (b.equals(expiredType)) {
                return b.getName();
            }
        }
        return null;
    }
}
