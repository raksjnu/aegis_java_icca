package com.raks.aegis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

 
public class ProjectContextHelper {
    private static final Logger logger = LoggerFactory.getLogger(ProjectContextHelper.class);

     
    public static Optional<Path> findLinkedConfig(Path projectRoot, List<Path> discoveredProjects) {
        if (projectRoot == null) return Optional.empty();
        
        String projectName = projectRoot.getFileName().toString();
         
        if (projectName.toLowerCase().contains("config")) {
            return Optional.empty();
        }

        String[] suffixes = {"_config", "-config", ".config"};
        
         
        for (String suffix : suffixes) {
            String targetName = projectName + suffix;
            Optional<Path> found = discoveredProjects.stream()
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(targetName))
                    .findFirst();
            
            if (found.isPresent()) {
                logger.debug("🔗 Linked project matched from discovery: {} <-> {}", projectName, targetName);
                return found;
            }
        }

         
        Path parent = projectRoot.getParent();
        if (parent != null && Files.isDirectory(parent)) {
            for (String suffix : suffixes) {
                 Path sibling = parent.resolve(projectName + suffix);
                 if (Files.isDirectory(sibling)) {
                     logger.info("🔗 Linked project found as sibling on disk: {} <-> {}", projectName, sibling.getFileName());
                     return Optional.of(sibling);
                 }
            }
        }
        
        return Optional.empty();
    }

     
    public static List<Path> getEffectiveSearchRoots(Path projectRoot, Path linkedConfig, boolean includeLinked) {
        List<Path> roots = new ArrayList<>();
        if (projectRoot != null) {
            roots.add(projectRoot);
        }
        if (includeLinked && linkedConfig != null && Files.exists(linkedConfig)) {
            roots.add(linkedConfig);
        }
        return roots;
    }

     
    public static String resolveWithFallback(String val, Path projectRoot, Path linkedConfig, boolean resolveLinked) {
         
        String resolved = PropertyResolver.resolve(val, projectRoot);
        
         
        if (resolveLinked && isUnresolved(resolved) && linkedConfig != null && Files.exists(linkedConfig)) {
            logger.debug("Attempting cross-project resolution for: {} in {}", val, linkedConfig);
            resolved = PropertyResolver.resolve(val, linkedConfig);
        }
        
        return resolved;
    }

    private static boolean isUnresolved(String val) {
        return val != null && val.contains("${");
    }
}
