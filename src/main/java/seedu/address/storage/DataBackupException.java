package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Represents a data file that could not be loaded and could not be backed up either.
 * The data file is left unchanged, so the app must not save over it.
 */
public class DataBackupException extends DataLoadingException {

    private final Path dataFilePath;
    private final Path backupFilePath;

    /**
     * Creates a {@code DataBackupException} for a failed copy of {@code dataFilePath} to
     * {@code backupFilePath}, where {@code cause} is the reason the copy failed.
     */
    public DataBackupException(Path dataFilePath, Path backupFilePath, Exception cause) {
        super("Could not back up data file " + dataFilePath + " to " + backupFilePath, cause);
        requireNonNull(dataFilePath);
        requireNonNull(backupFilePath);
        this.dataFilePath = dataFilePath;
        this.backupFilePath = backupFilePath;
    }

    /**
     * Returns the path of the data file that could not be loaded.
     */
    public Path getDataFilePath() {
        return dataFilePath;
    }

    /**
     * Returns the path where the backup could not be created.
     */
    public Path getBackupFilePath() {
        return backupFilePath;
    }
}
