package com.raks.aegis.util;

import com.raks.aegis.model.DetectionCriteria;
import com.raks.aegis.model.ProjectTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

 
public class ProjectTypeClassifier {
    private static final Logger logger = LoggerFactory.getLogger(ProjectTypeClassifier.class);
    
    private final Map<String, ProjectTypeDefinition> projectTypes;
    private final Map<Path, Set<String>> classificationCache = new HashMap<>();

    public ProjectTypeClassifier(Map<String, ProjectTypeDefinition> projectTypes) {
        this.projectTypes = projectTypes != null ? projectTypes : new HashMap<>();
    }

     
    public Set<String> classifyProject(Path projectPath) {
         
        if (classificationCache.containsKey(projectPath)) {
            return classificationCache.get(projectPath);
        }

        Set<String> matchedTypes = new HashSet<>();
        
         
        if (projectTypes.isEmpty()) {
            classificationCache.put(projectPath, matchedTypes);
            return matchedTypes;
        }

         
        for (Map.Entry<String, ProjectTypeDefinition> entry : projectTypes.entrySet()) {
            String typeName = entry.getKey();
            ProjectTypeDefinition typeDef = entry.getValue();
            
            if (matchesCriteria(projectPath, typeDef.getDetectionCriteria())) {
                matchedTypes.add(typeName);
                logger.debug("Project {} matched type: {}", projectPath.getFileName(), typeName);
            }
        }

         
        classificationCache.put(projectPath, matchedTypes);
        
        if (matchedTypes.isEmpty()) {
            logger.debug("Project {} did not match any defined types", projectPath.getFileName());
        }
        
        return matchedTypes;
    }

     
    private boolean matchesCriteria(Path projectPath, DetectionCriteria criteria) {
        if (criteria == null) {
            return false;
        }

        String projectName = projectPath.getFileName().toString();
        String logic = criteria.getLogic();  
        boolean isAndLogic = "AND".equalsIgnoreCase(logic);
        
        List<Boolean> results = new ArrayList<>();

         
        if (criteria.getNamePattern() != null && !criteria.getNamePattern().isEmpty()) {
            boolean matches = projectName.matches(criteria.getNamePattern());
            results.add(matches);
            logger.trace("namePattern '{}' match for {}: {}", criteria.getNamePattern(), projectName, matches);
        }

         
        if (criteria.getNameContains() != null && !criteria.getNameContains().isEmpty()) {
            boolean matches = criteria.getNameContains().stream()
                .anyMatch(substring -> projectName.toLowerCase().contains(substring.toLowerCase()));
            results.add(matches);
            logger.trace("nameContains match for {}: {}", projectName, matches);
        }

         
        if (criteria.getMarkerFiles() != null && !criteria.getMarkerFiles().isEmpty()) {
            boolean matches = hasAnyMarkerFile(projectPath, criteria.getMarkerFiles());
            results.add(matches);
            logger.trace("markerFiles match for {}: {}", projectName, matches);
        }

         
        if (criteria.getExcludePatterns() != null && !criteria.getExcludePatterns().isEmpty()) {
            boolean excluded = criteria.getExcludePatterns().stream()
                .anyMatch(projectName::matches);
            if (excluded) {
                logger.trace("Project {} excluded by pattern", projectName);
                return false;  
            }
        }

         
        if (results.isEmpty()) {
            return false;  
        }

        if (isAndLogic) {
            return results.stream().allMatch(Boolean::booleanValue);
        } else {
            return results.stream().anyMatch(Boolean::booleanValue);
        }
    }

     
    private boolean hasAnyMarkerFile(Path projectPath, List<String> markerFiles) {
        for (String markerFile : markerFiles) {
            if (markerFile.contains("*")) {
                 
                if (hasMatchingFile(projectPath, markerFile)) {
                    return true;
                }
            } else {
                 
                if (Files.exists(projectPath.resolve(markerFile))) {
                    return true;
                }
            }
        }
        return false;
    }

     
    private boolean hasMatchingFile(Path projectPath, String globPattern) {
        try (Stream<Path> paths = Files.walk(projectPath, 5)) {  
            return paths.anyMatch(path -> {
                String relativePath = projectPath.relativize(path).toString().replace("\\", "/");
                return matchesGlob(relativePath, globPattern);
            });
        } catch (IOException e) {
            logger.warn("Error searching for pattern {} in {}: {}", globPattern, projectPath, e.getMessage());
            return false;
        }
    }

     
    private boolean matchesGlob(String path, String pattern) {
         
        String regex = pattern
            .replace(".", "\\.")
            .replace("**", ".*")
            .replace("*", "[^/]*");
        return path.matches(regex);
    }

     
    public void clearCache() {
        classificationCache.clear();
    }
}
