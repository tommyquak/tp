package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataBackupException;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    private static final Path INVALID_DATA_FILE =
            Paths.get("src", "test", "data", "JsonAddressBookStorageTest", "notJsonFormatAddressBook.json");

    @TempDir
    public Path testFolder;

    private Path dataFilePath;
    private Path backupFilePath;
    private StorageManager storage;

    @BeforeEach
    public void setUp() throws Exception {
        dataFilePath = testFolder.resolve("addressbook.json");
        backupFilePath = JsonAddressBookStorage.getBackupFilePath(dataFilePath);
        Files.copy(INVALID_DATA_FILE, dataFilePath);
        storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }

    @Test
    public void initModelManager_backupSucceeds_startsWithEmptyAddressBook() throws Exception {
        // EP: invalid data that is backed up still starts the app, with an empty contact list.
        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(-1L, Files.mismatch(dataFilePath, backupFilePath));
    }

    @Test
    public void initModelManager_backupFails_throwsDataBackupException() throws Exception {
        // EP: invalid data that cannot be backed up must stop startup and leave the file unchanged.
        byte[] originalBytes = Files.readAllBytes(dataFilePath);
        // A non-empty folder at the backup path cannot be replaced, so the copy fails
        Files.createDirectories(backupFilePath.resolve("blocker"));

        assertThrows(DataBackupException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));

        assertArrayEquals(originalBytes, Files.readAllBytes(dataFilePath));
        assertTrue(Files.isDirectory(backupFilePath));
    }

    @Test
    public void getBackupFailureMessage_backupFailed_namesBothPaths() {
        String message = MainApp.getBackupFailureMessage(
                new DataBackupException(dataFilePath, backupFilePath, new IOException("denied")));

        assertTrue(message.contains(dataFilePath.toAbsolutePath().toString()));
        assertTrue(message.contains(backupFilePath.toAbsolutePath().toString()));
        assertTrue(message.contains("leave the data file unchanged"));
    }
}
