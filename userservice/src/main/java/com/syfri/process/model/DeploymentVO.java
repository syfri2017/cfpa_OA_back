package com.syfri.process.model;

public class DeploymentVO {


    private String id;
    private String name;
    private String deploymentTime;
    private String category;
    private String key;
    private String tenantId;

    public DeploymentVO() {
    }

    public DeploymentVO(String id, String name, String deploymentTime, String key) {
        this.id = id;
        this.name = name;
        this.deploymentTime = deploymentTime;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeploymentTime() {
        return deploymentTime;
    }

    public void setDeploymentTime(String deploymentTime) {
        this.deploymentTime = deploymentTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
