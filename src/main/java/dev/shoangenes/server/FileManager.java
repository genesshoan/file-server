package dev.shoangenes.server;

import dev.shoangenes.utils.LoggerUtil;
import dev.shoangenes.utils.ServerProperties;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class FileManager {
    // --- Singleton Instance ---
    private static volatile FileManager instance;

    // --- Instance Variables ---
    private Map<Integer, String> idToFilename;
    private Map<String, Integer> filenameToId;
    private AtomicInteger nextId = new AtomicInteger(1);

    // --- Constants ---

    private final static Path JSON_PATH = Paths.get(ServerProperties.getInstance().getStorageDir() + "file_mappings.json");
    private final static Logger logger = LoggerUtil.getLogger(FileManager.class);

    // --- Private Constructor ---

    /**
     * Private constructor to prevent instantiation
     */
    private FileManager() {
        idToFilename = new ConcurrentHashMap<>();
        filenameToId = new ConcurrentHashMap<>();

        try {
            Path storageDir = Paths.get(ServerProperties.getInstance().getStorageDir());
            Files.createDirectories(storageDir);

            Path jsonDir = JSON_PATH.getParent();
            if (jsonDir != null) {
                Files.createDirectories(jsonDir);
            }
        } catch (IOException e) {
            logger.severe("Failed to create storage directories: " + e.getMessage());
        }

        loadIdMappings();
    }

    // --- Private Helper Methods ---

    /**
     * Generates the next unique ID for a file.
     * @return the next unique ID
     */
    private int generateNextId() {
        return nextId.getAndIncrement();
    }

    /**
     * Adds a file to the internal maps.
     * @param id the unique ID of the file
     * @param filename the name of the file
     */
    private synchronized void addToMaps(int id, String filename) {
        idToFilename.put(id, filename);
        filenameToId.put(filename, id);
    }

    /**
     * Removes a file from the internal maps.
     * @param id the unique ID of the file to remove
     */
    private synchronized void removeFromMaps(int id) {
        String fileName = idToFilename.get(id);
        idToFilename.remove(id);
        filenameToId.remove(fileName);
    }

    /**
     * Retrieves the filename associated with a given ID.
     * @param id the unique ID of the file
     * @return the filename, or null if not found
     */
    private String getFilenameById(int id) {
        return idToFilename.get(id);
    }

    /**
     * Retrieves the ID associated with a given filename.
     * @param filename the name of the file
     * @return the unique ID, or null if not found
     */
    private Integer getIdByFilename(String filename) {
        return filenameToId.get(filename);
    }

    /**
     * Constructs the full path for a given filename.
     * @param filename the name of the file
     * @return the full path to the file
     */
    private String getFullPath(String filename) {
        return ServerProperties.getInstance()
                .getStorageDir() + filename;
    }

    /**
     * Validates if the filename has an allowed extension.
     * @param filename the name of the file
     * @return true if the filename is valid, false otherwise
     */
    private boolean isValidFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return true;
        }

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1) {
            return true;
        }

        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            String extension = filename.substring(dotIndex);
            String[] allowed = ServerProperties.getInstance().getAllowedTypes().split(",");
            return Arrays.asList(allowed).contains(extension);
        }

        return false;
    }

    /**
     * Checks if a file with the given filename already exists.
     * @param filename the name of the file
     * @return true if the file exists, false otherwise
     */
    private boolean fileExistsByName(String filename) {
        return filenameToId.containsKey(filename);
    }

    /**
     * Checks if a file with the given ID already exists.
     * @param id the unique ID of the file
     * @return true if the file exists, false otherwise
     */
    private boolean fileExistsById(int id) {
        return idToFilename.containsKey(id);
    }

    /**
     * Generates a unique filename by appending a random number if necessary.
     * @param originalName the original filename
     * @return a unique filename
     */
    private String generateUniqueFilename(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');

        String extension = "";
        String baseName = originalName;

        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
            baseName = originalName.substring(0, dotIndex);
        }

        Random random = new Random();
        String candidate = baseName + extension;

        while (fileExistsByName(candidate)) {
            candidate = baseName + "_" + Math.abs(random.nextInt(1_000_000)) + extension;
        }

        return candidate;
    }

    /**
     * Loads ID mappings from the JSON file into memory.
     */
    private void loadIdMappings() {
        try {
            if (Files.exists(JSON_PATH)) {
                String jsonContent = Files.readString(JSON_PATH);
                if (!jsonContent.isBlank()) {
                    Type type = new TypeToken<Map<Integer, String>>() {}.getType();
                    Gson gson = new GsonBuilder().create();
                    Map<Integer, String> loaded = gson.fromJson(jsonContent, type);

                    if (loaded != null) {
                        idToFilename.putAll(loaded);

                        for (Map.Entry<Integer, String> entry : loaded.entrySet()) {
                            filenameToId.put(entry.getValue(), entry.getKey());
                            nextId.updateAndGet(current -> Math.max(current, entry.getKey() + 1));
                        }

                        logger.info("Loaded ID mappings from " + JSON_PATH);
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to load ID mappings: " + e.getMessage());
        }
    }

    /**
     * Saves the current ID mappings to the JSON file.
     */
    private void saveIdMappings() {

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(idToFilename);

            Path tempPath = Path.of(JSON_PATH + ".temp");

            Files.writeString(tempPath, jsonContent);
            Files.move(tempPath, JSON_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            logger.info("Saved ID mappings to " + JSON_PATH);
        } catch (IOException e) {
            logger.severe("Failed to save ID mappings: " + e.getMessage());
        }
    }

    /**
     * Sanitizes a filename by removing potentially dangerous characters.
     * @param filename the original filename
     * @return the sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        // Remover caracteres peligrosos y paths
        filename = filename.replaceAll("[/\\\\]", "_");
        filename = filename.replaceAll("\\.\\.", "_");

        // Remover espacios al inicio/final
        filename = filename.trim();

        return filename;
    }

    // --- Public Methods ---

    /**
     * Singleton getInstance method
     * @return the singleton instance of FileManager
     */
    public static FileManager getInstance() {
        if (instance == null) {
            synchronized (FileManager.class) {
                if (instance == null) {
                    instance = new FileManager();
                }
            }
        }
        return instance;
    }

    /**
     * Saves a file with the given filename and content.
     * @param filename the name of the file
     * @param content the content of the file as a byte array
     * @return the unique ID of the saved file, or -1 if an error occurred
     */
    public int saveFile(String filename, byte[] content) {
        if (filename == null || filename.isEmpty()) {
            filename = generateUniqueFilename("file.dat");
        }

        filename = sanitizeFilename(filename);

        if (!isValidFilename(filename)) {
            logger.warning("Invalid file extension: " + filename);
            return -1;
        }

        int id;
        synchronized (this) {
            if (fileExistsByName(filename)) {
                filename = generateUniqueFilename(filename);
            }

            id = generateNextId();
        }

        Path fullPath = Paths.get(getFullPath(filename));
        Path tempPath = Paths.get(fullPath + ".tmp");

        try {
            Files.write(tempPath, content);
            Files.move(tempPath, fullPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            addToMaps(id, filename);
            saveIdMappings();
            return id;
        } catch (IOException e) {
            logger.severe("Can't save file: " + e.getMessage());
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ex) {
                logger.warning("Failed to delete temp file: " + ex.getMessage());
            }
            return -1;
        }
    }

    /**
     * Deletes a file with the given unique ID.
     * @param id the unique ID of the file to delete
     * @return true if the file was deleted successfully, false otherwise
     */
    public boolean deleteFileById(int id) {
        String filename = getFilenameById(id);

        if (filename == null) {
            return false;
        }

        Path fullPath = Paths.get(getFullPath(filename));

        try {
            Files.deleteIfExists(fullPath);

            removeFromMaps(id);

            saveIdMappings();

            return true;
        } catch (IOException e) {
            logger.severe("Can't delete file: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFileByName(String filename) {
        Integer id = getIdByFilename(filename);

        return id != null && deleteFileById(id);
    }

    /**
     * Retrieves the content of a file by its unique ID.
     * @param id the unique ID of the file
     * @return the content of the file as a byte array, or null if an error occurred
     */
    public synchronized byte[] getFileById(int id) {
        if (!fileExistsById(id)) {
            return null;
        }

        String filename = getFilenameById(id);
        Path fullPath = Paths.get(getFullPath(filename));

        try {
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            logger.severe("Can't read file: " + e.getMessage());
            return null;
        }
    }

    public synchronized byte[] getFileByName(String filename) {
        Integer id = getIdByFilename(filename);
        return id != null ? getFileById(id) : null;
    }
}
