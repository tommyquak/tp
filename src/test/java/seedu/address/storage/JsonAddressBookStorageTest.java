package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        // EP: no path is supplied, unlike a supplied path whose file does not exist.
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        // EP: the data file is absent, as on first launch.
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        // EP: the file exists but its syntax is not valid JSON.
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        // EP: valid JSON contains a contact with invalid field values.
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        // EP: valid and invalid contacts are mixed; loading must reject the whole file.
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // EP: the destination does not exist; saving creates a readable file.
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // EP: the destination already exists; saving replaces its previous contents.
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        // EP: the address book is null while the save path is supplied.
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        // EP: the save path is null while the address book is supplied.
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void getBackupFilePath_dataFile_appendsBakSuffix() {
        assertEquals(Paths.get("data", "addressbook.json.bak"),
                JsonAddressBookStorage.getBackupFilePath(Paths.get("data", "addressbook.json")));
    }

    @Test
    public void readAddressBookWithBackup_notJsonFormat_backupCreatedWithSameContent() throws Exception {
        // EP: unreadable JSON syntax triggers a backup of the original bytes.
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_invalidPerson_backupCreatedWithSameContent() throws Exception {
        // EP: readable JSON with invalid contact data also requires a backup.
        assertCorruptedFileBackedUp("invalidPersonAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_existingBackup_backupReplaced() throws Exception {
        // EP: a backup already exists; the newest unreadable file replaces it.
        Path backupPath = testFolder.resolve("addressbook.json.bak");
        Files.writeString(backupPath, "old backup");
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_missingFile_noBackup() throws Exception {
        // EP: the source file is absent, so there is nothing to back up.
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertFalse(storage.readAddressBookWithBackup().isPresent());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_validFile_noBackup() throws Exception {
        // EP: the source loads successfully, so no recovery backup is needed.
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());

        Optional<ReadOnlyAddressBook> readBack = storage.readAddressBookWithBackup();

        assertEquals(getTypicalAddressBook(), new AddressBook(readBack.get()));
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_backupDestinationIsFolder_throwsDataBackupException() throws Exception {
        // EP: reading fails and the backup destination is unusable; the original must stay unchanged.
        Path filePath = testFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("notJsonFormatAddressBook.json"), filePath);
        byte[] originalBytes = Files.readAllBytes(filePath);
        // A non-empty folder at the backup path cannot be replaced, so the copy fails
        Path backupPath = JsonAddressBookStorage.getBackupFilePath(filePath);
        Files.createDirectories(backupPath.resolve("blocker"));

        DataBackupException e = Assertions.assertThrows(DataBackupException.class, () ->
                new JsonAddressBookStorage(filePath).readAddressBookWithBackup());

        assertEquals(filePath, e.getDataFilePath());
        assertEquals(backupPath, e.getBackupFilePath());
        assertTrue(Files.isDirectory(backupPath));
        assertArrayEquals(originalBytes, Files.readAllBytes(filePath));
    }

    @Test
    public void readAddressBookWithBackup_folderNotWritable_throwsDataBackupException() throws Exception {
        // EP: reading fails and the data folder is read-only, so no backup can be created there.
        Path dataFolder = Files.createDirectories(testFolder.resolve("data"));
        Path filePath = dataFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("notJsonFormatAddressBook.json"), filePath);
        byte[] originalBytes = Files.readAllBytes(filePath);
        File folder = dataFolder.toFile();

        try {
            // Skips where a read-only folder is not enforced, e.g. on Windows or when running as root
            assumeTrue(folder.setWritable(false));
            assumeFalse(canCreateFileIn(dataFolder));

            DataBackupException e = Assertions.assertThrows(DataBackupException.class, () ->
                    new JsonAddressBookStorage(filePath).readAddressBookWithBackup());

            assertEquals(filePath, e.getDataFilePath());
            assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
            assertArrayEquals(originalBytes, Files.readAllBytes(filePath));
        } finally {
            folder.setWritable(true);
        }
    }

    @Test
    public void readAddressBookWithBackup_dataPathIsFolder_throwsDataLoadingExceptionOnly() throws Exception {
        // EP: the data path is a folder, which cannot be saved over, so no backup is needed.
        Path filePath = Files.createDirectories(testFolder.resolve("addressbook.json"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        DataLoadingException e = Assertions.assertThrows(DataLoadingException.class,
                storage::readAddressBookWithBackup);

        assertFalse(e instanceof DataBackupException);
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void backUpFile_notRegularFile_noBackup() throws Exception {
        // EP: the source path names a directory instead of a regular file.
        Path folder = testFolder.resolve("addressbook.json");
        Files.createDirectories(folder);

        assertFalse(JsonAddressBookStorage.backUpFile(folder).isPresent());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(folder)));
    }

    /**
     * Returns true if a new file can be created in {@code folder}, deleting the probe file afterwards.
     */
    private static boolean canCreateFileIn(Path folder) {
        try {
            Files.delete(Files.createFile(folder.resolve("probe.tmp")));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Copies {@code fileInTestDataFolder} to a temporary data file, reads it with backup,
     * and checks that the read fails and the backup matches the original byte for byte.
     */
    private void assertCorruptedFileBackedUp(String fileInTestDataFolder) throws IOException {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve(fileInTestDataFolder), filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBookWithBackup);

        Path backupPath = JsonAddressBookStorage.getBackupFilePath(filePath);
        assertTrue(Files.exists(backupPath));
        assertEquals(-1L, Files.mismatch(TEST_DATA_FOLDER.resolve(fileInTestDataFolder), filePath));
        assertEquals(-1L, Files.mismatch(filePath, backupPath));
    }
}
