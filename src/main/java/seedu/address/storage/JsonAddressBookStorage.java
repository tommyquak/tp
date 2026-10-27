package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
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
     *
     * @throws DataBackupException if the data file cannot be loaded and the copy could not be made.
     *     The data file is left unchanged, so callers must not save over it.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBookWithBackup() throws DataLoadingException {
        try {
            return readAddressBook(filePath);
        } catch (DataLoadingException e) {
            try {
                backUpFile(filePath);
            } catch (IOException | SecurityException backupError) {
                DataBackupException backupException =
                        new DataBackupException(filePath, getBackupFilePath(filePath), backupError);
                backupException.addSuppressed(e);
                throw backupException;
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
     * Returns the backup path, or {@code Optional.empty()} if {@code filePath} is not a regular file,
     * since such a path cannot be saved over and so needs no backup.
     *
     * @throws IOException if the copy could not be made.
     */
    static Optional<Path> backUpFile(Path filePath) throws IOException {
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
            logger.severe("Could not back up data file " + filePath + " to " + backupPath + ": " + e);
            throw e;
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
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

}
