package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileIOUtils {
    public static Path ensureResourceFile(Path baseDir, String resourceName) throws IOException {
        Files.createDirectories(baseDir);
        Path targetFile = baseDir.resolve(resourceName);

        if (Files.notExists(targetFile)) {
            copyFromResources(resourceName, targetFile, FileIOUtils.class);
            System.out.println(resourceName + " copied to " + baseDir);
        } else {
            System.out.println(resourceName + " already exists in " + baseDir);
        }

        return targetFile;
    }

    private static void copyFromResources(String resourceName, Path target, Class resourceOwner) throws IOException {
        try (InputStream in = resourceOwner
                .getClassLoader()
                .getResourceAsStream(resourceName)) {

            if (in == null)
                throw new IOException("Resource not found: " + resourceName);

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
