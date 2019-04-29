package com.syfri.process.model;

public class ProcessModel {


    public String modelId ;
    public String name;
    public String revision ;
    public String description ;
    public String jsonXml;
    public String svgXml;


    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getJsonXml() {
        return jsonXml;
    }

    public void setJsonXml(String jsonXml) {
        this.jsonXml = jsonXml;
    }

    public String getSvgXml() {
        return svgXml;
    }

    public void setSvgXml(String svgXml) {
        this.svgXml = svgXml;
    }
}
