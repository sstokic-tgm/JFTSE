package com.jftse.emulator.common.utilities;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ResourceUtil {
    public static InputStream getResource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    public static Optional<Path> getPath(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null)
            return Optional.empty();

        Path p;
        try {
            p = Paths.get(url.toURI());
        } catch (FileSystemNotFoundException e) {
            p = Paths.get(resource).toAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(p);
    }
}
