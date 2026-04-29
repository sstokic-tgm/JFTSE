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
    private static class ScriptFileVisitor extends SimpleFileVisitor<Path> {
        private final Path root;

        private final List<ScriptFile> scriptFileList;
        private final Supplier<Logger> logger;

        public ScriptFileVisitor(Path root, List<ScriptFile> scriptFileList, Supplier<Logger> logger) {
            this.root = root;
            this.scriptFileList = scriptFileList;
            this.logger = logger;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!file.getFileName().toString().endsWith(".js")) {
                return FileVisitResult.CONTINUE;
            }

            Path relative = root.relativize(file);
            if (relative.getNameCount() < 2) {
                logger.get().warn("Script file {} is not in a valid folder structure. It will be ignored.", file.toAbsolutePath());
                return FileVisitResult.CONTINUE;
            }

            String type = relative.getName(0).toString().toUpperCase();
            if (!ScriptManagerV2.allowedTypes.contains(type)) {
                return FileVisitResult.CONTINUE;
            }

            String fileName = file.getFileName().toString();
            String name = stripExtension(fileName);

            String groupPath = relative.getNameCount() > 2 ? relative.subpath(1, relative.getNameCount() - 1).toString().replace("\\", "/") : "";

            ScriptFile scriptFile = new ScriptFile(name, file.toFile(), type, groupPath);
            scriptFileList.add(scriptFile);

            logger.get().info("Read {}", scriptFile);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            throw new IOException(file + " " + exc.getMessage(), exc);
        }

        private String stripExtension(String fileName) {
            int dot = fileName.lastIndexOf('.');
            return dot > 0 ? fileName.substring(0, dot) : fileName;
        }
    }

    public static Optional<ScriptManagerV2> loadScriptsV2(String pathToScriptFolder, Supplier<Logger> logger) {
        List<ScriptFile> scriptFileList = new ArrayList<>();

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(pathToScriptFolder);
            if (url == null) {
                return Optional.of(ScriptManagerV2.getInstance());
            }

            Path root;
            try {
                root = Paths.get(url.toURI());
            } catch (FileSystemNotFoundException e) {
                root = Paths.get(pathToScriptFolder).toAbsolutePath();
            }

            logger.get().info("Reading scripts...");

            ScriptFileVisitor sfv = new ScriptFileVisitor(root, scriptFileList, logger);
            Files.walkFileTree(root, sfv);

        } catch (IOException | URISyntaxException e) {
            logger.get().error("Failed finding scripts: " + e.getMessage(), e);
            return Optional.of(ScriptManagerV2.getInstance());
        }

        scriptFileList.sort(
                Comparator.comparing(ScriptFile::getType)
                        .thenComparing(ScriptFile::getGroupPath, Comparator.nullsFirst(String::compareToIgnoreCase))
                        .thenComparing(ScriptFile::getName, String.CASE_INSENSITIVE_ORDER)
        );

        ScriptManagerV2 scriptManagerV2 = new ScriptManagerV2(scriptFileList);

        logger.get().info("Successfully loaded");
        logger.get().info("--------------------------------------");

        return Optional.of(scriptManagerV2);
    }
}
