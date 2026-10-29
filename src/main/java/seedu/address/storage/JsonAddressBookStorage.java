package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final String BACKUP_FILE_SUFFIX = ".bak";

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Similar to {@link #readAddressBook()}, but if the data file exists and cannot be loaded,
     * copies it to {@code <file name>.bak} in the same folder before rethrowing the exception.
     * If the backup cannot be created, throws a {@link DataBackupException} so startup cannot
     * continue with an empty writable contact list.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBookWithBackup() throws DataLoadingException {
        try {
            return readAddressBook(filePath);
        } catch (DataLoadingException e) {
            if (backUpFile(filePath).isEmpty()) {
                throw new DataBackupException(filePath, getBackupFilePath(filePath), e);
            }
            throw e;
        }
    }

    /**
     * Returns the path of the backup file for {@code filePath}, e.g. {@code data/a.json.bak} for
     * {@code data/a.json}.
     */
    public static Path getBackupFilePath(Path filePath) {
        requireNonNull(filePath);
        Path fileName = filePath.getFileName();
        assert fileName != null : "Data file path should end with a file name";
        return filePath.resolveSibling(fileName + BACKUP_FILE_SUFFIX);
    }

    /**
     * Copies {@code filePath} to its backup path, replacing any older backup.
     * Returns the backup path, or {@code Optional.empty()} if the copy could not be made.
     */
    static Optional<Path> backUpFile(Path filePath) {
        requireNonNull(filePath);
        Path backupPath = getBackupFilePath(filePath);
        assert !backupPath.equals(filePath) : "Backup must not overwrite the data file";

        if (!Files.isRegularFile(filePath)) {
            logger.warning("Data file " + filePath + " is not a regular file, so it was not backed up.");
            return Optional.empty();
        }

        try {
            Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | SecurityException e) {
            logger.warning("Could not back up data file " + filePath + " to " + backupPath + ": " + e);
            return Optional.empty();
        }

        logger.info("Backed up data file " + filePath + " to " + backupPath);
        return Optional.of(backupPath);
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     * Writes a temporary sibling file before atomically replacing the data file. If atomic replacement
     * is unavailable or writing fails, the existing file is left unchanged.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path target = filePath.toAbsolutePath();
        if (Files.isSymbolicLink(target)) {
            // Follow an existing link instead of replacing the link itself. A dangling link fails safely.
            target = target.toRealPath();
        }
        if (Files.exists(target)) {
            if (!Files.isRegularFile(target)) {
                throw new IOException("Data file is not a regular file: " + filePath);
            }
            if (!Files.isWritable(target)) {
                throw new AccessDeniedException(filePath.toString());
            }
        }

        checkNoFileBlocksFolder(filePath);
        Path temporaryFile;
        try {
            Files.createDirectories(target.getParent());
            temporaryFile = Files.createTempFile(target.getParent(), ".addressbook-", ".tmp");
        } catch (AccessDeniedException e) {
            // Report the data file, not the folder or temporary file that could not be created.
            AccessDeniedException denied = new AccessDeniedException(filePath.toString());
            denied.initCause(e);
            throw denied;
        }
        try {
            writeAddressBook(addressBook, temporaryFile);
            copyPermissionsIfSupported(target, temporaryFile);
            replaceFile(temporaryFile, target);
        } catch (IOException | RuntimeException e) {
            try {
                deleteTemporaryFile(temporaryFile);
            } catch (IOException | SecurityException cleanupError) {
                e.addSuppressed(cleanupError);
                logger.warning("Could not remove temporary data file " + temporaryFile + ": " + cleanupError);
            }
            throw e;
        }
    }

    /**
     * Throws an {@code IOException} that names the problem if a folder in {@code filePath} exists
     * but is not a folder, e.g. a file named {@code data} where the {@code data} folder should be.
     */
    private static void checkNoFileBlocksFolder(Path filePath) throws IOException {
        for (Path folder = filePath.getParent(); folder != null; folder = folder.getParent()) {
            if (Files.isDirectory(folder)) {
                return;
            }
            if (Files.exists(folder)) {
                throw new IOException(String.format("\"%s\" exists but is not a folder, so %s cannot be saved.",
                        folder, filePath));
            }
        }
    }

    /**
     * Writes JSON data to the given file.
     */
    void writeAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

    /**
     * Preserves the existing file's access permissions on file systems that support POSIX permissions.
     */
    private void copyPermissionsIfSupported(Path target, Path temporaryFile) throws IOException {
        PosixFileAttributeView permissions = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        if (Files.exists(target) && permissions != null) {
            Files.setPosixFilePermissions(temporaryFile, permissions.readAttributes().permissions());
        }
    }

    /**
     * Commits a fully written file. There is deliberately no non-atomic fallback, which could lose saved data.
     */
    void replaceFile(Path temporaryFile, Path target) throws IOException {
        Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Removes an uncommitted temporary file after a failed save.
     */
    void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

}
