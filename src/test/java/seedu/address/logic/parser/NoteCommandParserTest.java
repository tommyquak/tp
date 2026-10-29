package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

public class NoteCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE);

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_noteContainsNotePrefix_success() throws ParseException {
        // EP: valid note text contains another nt/; only the first prefix separates the index and note.
        String noteText = "Client asked what nt/ means";

        NoteCommand expectedCommand = new NoteCommand(
                Index.fromOneBased(1),
                new InteractionNote(noteText));

        assertEquals(
                expectedCommand,
                parser.parse("1 nt/" + noteText));
    }

    @Test
    public void parse_validArgs_success() {
        // EP: index and note separated by whitespace; the note is trimmed.
        assertParseSuccess(parser, " 1 nt/Called client", expectedCommand(1, "Called client"));
        assertParseSuccess(parser, "  3   nt/  spaced out  ", expectedCommand(3, "spaced out"));

        // EP: other prefixes inside the note are kept as note text.
        assertParseSuccess(parser, " 2 nt/call re t/vip n/Amy st/client",
                expectedCommand(2, "call re t/vip n/Amy st/client"));

        // EP: a second nt/ stays in the note with its original spacing.
        assertParseSuccess(parser, " 1 nt/a  nt/  b", expectedCommand(1, "a  nt/  b"));

        // EP: non-ASCII note text is accepted.
        assertParseSuccess(parser, " 1 nt/客户 café ok", expectedCommand(1, "客户 café ok"));
    }

    @Test
    public void parse_noSpaceBeforeNotePrefix_failure() {
        // EP: nt/ attached to the index is not a prefix, so the whole word is read as the index,
        // matching how edit handles 1n/NAME.
        assertParseFailure(parser, " 1nt/nospace", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " 1nt/a nt/b", MESSAGE_INVALID_INDEX);

        // EP: without a valid nt/, a note with spaces leaves more than one word before any prefix.
        assertParseFailure(parser, " 1nt/has a space", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // EP: missing index.
        assertParseFailure(parser, " nt/Called client", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);

        // EP: more than one word before nt/.
        assertParseFailure(parser, " 1 2 nt/Called client", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 xnt/Called client", MESSAGE_INVALID_FORMAT);

        // BVA: 0 is just below the smallest valid index.
        assertParseFailure(parser, " 0 nt/Called client", MESSAGE_INVALID_INDEX);

        // EP: non-numeric index.
        assertParseFailure(parser, " a nt/Called client", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_missingNotePrefix_failure() {
        // EP: valid index but no nt/ prefix.
        assertParseFailure(parser, " 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 Called client", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_blankNote_failure() {
        // EP: nt/ is present but the note is empty or only whitespace.
        assertParseFailure(parser, " 1 nt/", InteractionNote.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 nt/   ", InteractionNote.MESSAGE_CONSTRAINTS);
    }

    private static NoteCommand expectedCommand(int oneBasedIndex, String note) {
        return new NoteCommand(Index.fromOneBased(oneBasedIndex), new InteractionNote(note));
    }
}
