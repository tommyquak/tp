package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Checks that failed saves name the data file and the actual problem, not a temporary file or a bare path.
 */
public class JsonAddressBookStorageSaveErrorTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_readOnlyDataFolder_namesDataFile() throws Exception {
        Path folder = Files.createDirectory(testFolder.resolve("data"));
        Path filePath = folder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        assumeTrue(Files.getFileAttributeView(folder, PosixFileAttributeView.class) != null);
        byte[] savedBytes = Files.readAllBytes(filePath);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(folder);
        try {
            Files.setPosixFilePermissions(folder, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_EXECUTE));
            assumeFalse(Files.isWritable(folder), "Elevated users can write to read-only folders");

            // The data file itself is writable, so only creating the temporary file in the folder fails.
            AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () ->
                    storage.saveAddressBook(getTypicalAddressBook()));
            assertEquals(filePath.toString(), thrown.getMessage());

            CommandException commandError = assertThrows(CommandException.class, () ->
                    createLogic(storage).execute("note 1 nt/Called"));
            assertEquals(String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, filePath),
                    commandError.getMessage());
            assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
            assertEquals(List.of(filePath), listFolder(folder));
        } finally {
            Files.setPosixFilePermissions(folder, originalPermissions);
        }
    }

    @Test
    public void execute_fileInPlaceOfDataFolder_reportsNotAFolder() throws Exception {
        Path notFolder = testFolder.resolve("data");
        Files.writeString(notFolder, "Do not overwrite");
        Path filePath = notFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        String expectedProblem = "\"" + notFolder + "\" exists but is not a folder, so " + filePath
                + " cannot be saved.";

        CommandException thrown = assertThrows(CommandException.class, () ->
                createLogic(storage).execute("delete 1"));

        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT, expectedProblem), thrown.getMessage());
        assertEquals("Do not overwrite", Files.readString(notFolder));
        assertEquals(List.of(notFolder), listFolder(testFolder));
    }

    private LogicManager createLogic(JsonAddressBookStorage storage) {
        return new LogicManager(new ModelManager(getTypicalAddressBook(), new UserPrefs()),
                new StorageManager(storage, new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private static List<Path> listFolder(Path folder) throws Exception {
        try (var files = Files.list(folder)) {
            return files.toList();
        }
    }
}
