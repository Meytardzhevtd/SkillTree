package com.skilltree.dto;

import java.util.List;

public class CreateCourseRequest {
    private String name;
    private String description;
    private Long userId;
    private List<CreateModuleRequest> modules;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CreateModuleRequest> getModules() {
        return modules;
    }
    public void setModules(List<CreateModuleRequest> modules) {
        this.modules = modules;
    }
}