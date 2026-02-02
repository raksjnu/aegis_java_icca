package com.raks.aegis.model;

import java.util.List;

 
public class DetectionCriteria {
    private List<String> markerFiles;       
    private String namePattern;              
    private List<String> nameContains;       
    private List<String> excludePatterns;    
    private String logic;                    

    public List<String> getMarkerFiles() {
        return markerFiles;
    }

    public void setMarkerFiles(List<String> markerFiles) {
        this.markerFiles = markerFiles;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public void setNamePattern(String namePattern) {
        this.namePattern = namePattern;
    }

    public List<String> getNameContains() {
        return nameContains;
    }

    public void setNameContains(List<String> nameContains) {
        this.nameContains = nameContains;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public String getLogic() {
        return logic != null ? logic : "OR";
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }
}
