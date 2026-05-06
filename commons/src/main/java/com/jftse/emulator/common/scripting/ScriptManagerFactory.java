package com.jftse.emulator.common.scripting;

import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Supplier;

public class ScriptManagerFactory {
    @Getter
    private static class ScriptFileVisitor implements FileVisitor<Path> {
        private final Deque<Path> dirStack = new ArrayDeque<>();

        private final List<ScriptFile> scriptFileList;
        private final Supplier<Logger> logger;

        public ScriptFileVisitor(List<ScriptFile> scriptFileList, Supplier<Logger> logger) {
            this.scriptFileList = scriptFileList;
            this.logger = logger;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            dirStack.push(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path currentDir = dirStack.peek();

            String fileName = file.getFileName().toString();
            String type = currentDir.getFileName().toString().toUpperCase();
            String subType = "";

            if (!ScriptManager.allowedTypes.contains(type)) {
                Path parentDir = currentDir.getParent();
                if (parentDir != null && ScriptManager.allowedTypes.contains(parentDir.getFileName().toString().toUpperCase())) {
                    type = parentDir.getFileName().toString().toUpperCase();
                    if (type.equals("GUARDIAN-PHASE")) {
                        subType = currentDir.getFileName().toString().toUpperCase();
                    }
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            Long id = null;
            try {
                id = Long.valueOf(fileName.split("_")[0]);
            } catch (NumberFormatException ignored) {
            }

            String[] parts = file.toFile().getName().split("\\.")[0].split("_");
            String name = String.join("_", Arrays.copyOfRange(parts, 1, parts.length));

            ScriptFile scriptFile = new ScriptFile(id, name, file.toFile(), type, subType);
            scriptFileList.add(scriptFile);

            logger.get().info("Read " + scriptFile);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            throw new IOException(file.toString() + " " + exc.getMessage(), exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            dirStack.pop();
            return FileVisitResult.CONTINUE;
        }
    }

    public static Optional<ScriptManager> loadScripts(String pathToScriptFolder, Supplier<Logger> logger) {
        List<ScriptFile> scriptFileList = new ArrayList<>();
        ScriptManagerFactory.ScriptFileVisitor sfv = new ScriptManagerFactory.ScriptFileVisitor(scriptFileList, logger);

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(pathToScriptFolder);
            if (url == null)
                return Optional.of(ScriptManager.getInstance());

            Path p;
            try {
                p = Paths.get(url.toURI());
            } catch (FileSystemNotFoundException e) {
                p = Paths.get(pathToScriptFolder).toAbsolutePath();
            }

            logger.get().info("Reading scripts...");

            Files.walkFileTree(p, sfv);
        } catch (IOException | URISyntaxException e) {
            logger.get().error("Failed finding scripts: " + e.getMessage(), e);
            return Optional.of(ScriptManager.getInstance());
        }

        ScriptManager scriptManager = new ScriptManager(sfv.getScriptFileList());

        logger.get().info("Successfully loaded");
        logger.get().info("--------------------------------------");

        return Optional.of(scriptManager);
    }

    public static Optional<ScriptManagerV2> loadScriptsV2(String pathToScriptFolder, Supplier<Logger> logger) {
        List<ScriptFile> scriptFileList = new ArrayList<>();
        ScriptManagerFactory.ScriptFileVisitor sfv = new ScriptManagerFactory.ScriptFileVisitor(scriptFileList, logger);

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(pathToScriptFolder);
            if (url == null)
                return Optional.of(ScriptManagerV2.getInstance());

            Path p;
            try {
                p = Paths.get(url.toURI());
            } catch (FileSystemNotFoundException e) {
                p = Paths.get(pathToScriptFolder).toAbsolutePath();
            }

            logger.get().info("Reading scripts...");

            Files.walkFileTree(p, sfv);
        } catch (IOException | URISyntaxException e) {
            logger.get().error("Failed finding scripts: " + e.getMessage(), e);
            return Optional.of(ScriptManagerV2.getInstance());
        }

        ScriptManagerV2 scriptManagerV2 = new ScriptManagerV2(scriptFileList);

        logger.get().info("Successfully loaded");
        logger.get().info("--------------------------------------");

        return Optional.of(scriptManagerV2);
    }
}
