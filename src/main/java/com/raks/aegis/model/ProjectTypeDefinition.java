package com.raks.aegis.model;

 
public class ProjectTypeDefinition {
    private String description;
    private DetectionCriteria detectionCriteria;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DetectionCriteria getDetectionCriteria() {
        return detectionCriteria;
    }

    public void setDetectionCriteria(DetectionCriteria detectionCriteria) {
        this.detectionCriteria = detectionCriteria;
    }
}
