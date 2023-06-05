package com.jftse.emulator.common.scripting;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class ScriptManagerFactory {
    public static Optional<ScriptManager> loadScripts(String pathToScriptFolder, Supplier<Logger> logger) {
        List<ScriptFile> scriptFileList = new ArrayList<>();

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(pathToScriptFolder);
            if (url == null)
                return Optional.of(ScriptManager.getInstance());

            logger.get().info("Loading scripts...");

            Files.walkFileTree(Paths.get(url.toURI()), new FileVisitor<>() {
                private Path preVisitDir;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    preVisitDir = dir;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    String type = file.getParent().toString().equals(preVisitDir.getFileName().toString())
                            ? preVisitDir.getFileName().toString().toUpperCase()
                            : file.getParent().getFileName().toString().toUpperCase();

                    Long id = null;
                    try {
                        id = Long.valueOf(fileName.split("_")[0]);
                    } catch (NumberFormatException ignored) {
                    }

                    ScriptFile scriptFile = new ScriptFile(id, file.toFile(), type);
                    scriptFileList.add(scriptFile);

                    logger.get().info("Loaded " + scriptFile);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw new IOException(file.toString() + " " + exc.getMessage(), exc);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.get().error("Failed finding scripts: " + e.getMessage(), e);
            return Optional.of(ScriptManager.getInstance());
        } catch (URISyntaxException e) {
            logger.get().error(e.getMessage(), e);
            return Optional.of(ScriptManager.getInstance());
        }

        ScriptManager scriptManager = new ScriptManager(scriptFileList);

        logger.get().info("Successfully loaded");
        logger.get().info("--------------------------------------");

        return Optional.of(scriptManager);
    }
}
