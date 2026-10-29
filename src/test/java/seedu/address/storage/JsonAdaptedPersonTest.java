package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CLIENT_BENSON;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+65+1234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        // EP: required fields and tags are valid, providing the baseline for single-field failures.
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_equivalentUnicodeNames_returnsCanonicalName() throws Exception {
        // EP: composed and decomposed accents represent the same valid name in stored data.
        for (String name : List.of("Jos\u00e9 Garc\u00eda", "Jose\u0301 Garci\u0301a")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    name, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
            assertEquals("Jos\u00e9 Garc\u00eda", person.toModelType().getName().fullName);
        }
    }

    @Test
    public void toModelType_nameEndingWithSuffixPeriod_returnsPerson() throws Exception {
        // EP: a name suffix such as "Jr." was valid in older data files and must still load.
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                "Martin Luther King Jr.", VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        assertEquals("Martin Luther King Jr.", person.toModelType().getName().fullName);
    }

    @Test
    public void toModelType_statusRoundTrip_returnsPerson() throws Exception {
        // EP: an explicitly supplied valid status survives conversion.
        assertEquals(CLIENT_BENSON, new JsonAdaptedPerson(CLIENT_BENSON).toModelType());
    }

    @Test
    public void jsonSerialization_statusRoundTrip_returnsPerson() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(CLIENT_BENSON));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(CLIENT_BENSON, restored.toModelType());
    }

    @Test
    public void toModelType_missingStatus_defaultsToProspect() throws Exception {
        // EP: status is absent, as in an older data file; apply the default.
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_ADDRESS,
                VALID_TAGS);
        assertEquals(Status.PROSPECT, person.toModelType().getStatus());
    }

    @Test
    public void toModelType_blankStatus_defaultsToProspect() throws Exception {
        // EP: status is present but blank; it has the same default as an absent status.
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_ADDRESS,
                "   ",
                VALID_TAGS);
        assertEquals(Status.PROSPECT, person.toModelType().getStatus());
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        // EP: a non-blank status is unrecognized; reject it rather than applying the default.
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_ADDRESS,
                "lead",
                VALID_TAGS);
        assertThrows(
                IllegalValueException.class,
                Status.MESSAGE_CONSTRAINTS,
                person::toModelType);
    }

    @Test
    public void toModelType_mixedCaseAsciiStatus_returnsPerson() throws Exception {
        // EP: an ASCII status with mixed case and surrounding whitespace remains valid in stored data.
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, " \tcLiEnT\n ", VALID_TAGS);

        assertEquals(CLIENT_BENSON, person.toModelType());
    }

    @Test
    public void toModelType_unicodeStatusLookalikes_throwsIllegalValueException() {
        // EP: dotted and dotless Unicode I resemble CLIENT but are not ASCII status spellings.
        for (String status : List.of("CL\u0130ENT", "cl\u0131ent")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, status, VALID_TAGS);

            assertThrows(IllegalValueException.class, Status.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_validFollowUp_returnsPersonWithFollowUp() throws Exception {
        // EP: an optional follow-up contains a valid calendar date.
        Person bensonWithFollowUp = new PersonBuilder(BENSON)
                .withFollowUp("25-12-2099")
                .build();
        JsonAdaptedPerson person = new JsonAdaptedPerson(bensonWithFollowUp);
        assertEquals(bensonWithFollowUp, person.toModelType());
    }

    @Test
    public void toModelType_nullFollowUp_returnsPersonWithoutFollowUp() throws Exception {
        // EP: the optional follow-up is absent; conversion must still succeed.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null);
        assertTrue(person.toModelType().getFollowUp().isEmpty());
    }

    @Test
    public void toModelType_blankFollowUp_returnsPersonWithoutFollowUp() throws Exception {
        // EP: the optional follow-up is blank; treat it as absent.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        "   ");
        assertTrue(person.toModelType().getFollowUp().isEmpty());
    }

    @Test
    public void toModelType_invalidFollowUp_throwsIllegalValueException() {
        // EP: the follow-up has the correct shape but names an impossible calendar date.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        "31-02-2099");
        assertThrows(
                IllegalValueException.class,
                FollowUp.MESSAGE_CONSTRAINTS,
                person::toModelType);
    }

    @Test
    public void toModelType_validInteractionNotes_returnsPersonWithNotes()
            throws Exception {

        // EP: valid notes with distinct timestamps survive conversion to the model.
        InteractionNote firstNote = new InteractionNote(
                "Met client",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        InteractionNote secondNote = new InteractionNote(
                "Discussed hospital coverage",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        Person bensonWithNotes = new PersonBuilder(BENSON)
                .withInteractionNotes(firstNote, secondNote)
                .build();

        JsonAdaptedPerson person = new JsonAdaptedPerson(bensonWithNotes);

        assertEquals(bensonWithNotes, person.toModelType());
    }

    @Test
    public void jsonSerialization_interactionNotesRoundTrip_returnsPerson()
            throws Exception {

        // EP: a nonempty notes list survives JSON serialization and deserialization.
        InteractionNote firstNote = new InteractionNote(
                "Met client",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        InteractionNote secondNote = new InteractionNote(
                "Client requested revised quotation",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        Person bensonWithNotes = new PersonBuilder(BENSON)
                .withInteractionNotes(firstNote, secondNote)
                .build();

        String json = JsonUtil.toJsonString(
                new JsonAdaptedPerson(bensonWithNotes));

        JsonAdaptedPerson restored =
                JsonUtil.fromJsonString(
                        json,
                        JsonAdaptedPerson.class);

        assertEquals(bensonWithNotes, restored.toModelType());
    }

    @Test
    public void jsonSerialization_interactionNoteTimestampPreserved()
            throws Exception {

        // EP: a stored note's explicit timestamp is preserved when the note is reloaded.
        LocalDateTime timestamp =
                LocalDateTime.of(2026, 10, 26, 21, 45);

        InteractionNote note =
                new InteractionNote(
                        "Called client",
                        timestamp);

        Person bensonWithNotes = new PersonBuilder(BENSON)
                .withInteractionNotes(note)
                .build();

        String json = JsonUtil.toJsonString(
                new JsonAdaptedPerson(bensonWithNotes));

        JsonAdaptedPerson restored =
                JsonUtil.fromJsonString(
                        json,
                        JsonAdaptedPerson.class);

        InteractionNote restoredNote =
                restored.toModelType()
                        .getInteractionNotes()
                        .get(0);

        assertEquals(timestamp, restoredNote.getCreatedAt());
        assertEquals("Called client", restoredNote.getValue());
    }

    @Test
    public void toModelType_nullInteractionNotes_returnsEmptyHistory()
            throws Exception {

        // EP: an absent notes field, as in older data files, becomes an empty history.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null,
                        null);

        assertTrue(
                person.toModelType()
                        .getInteractionNotes()
                        .isEmpty());
    }

    @Test
    public void toModelType_invalidInteractionNote_throwsIllegalValueException() {
        // EP: a note is present but its text is blank; reject the contact's invalid history.
        JsonAdaptedInteractionNote invalidNote =
                new JsonAdaptedInteractionNote(
                        "   ",
                        "2026-10-26T21:00:00");

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null,
                        List.of(invalidNote));

        assertThrows(
                IllegalValueException.class,
                InteractionNote.MESSAGE_CONSTRAINTS,
                person::toModelType);
    }

    @Test
    public void toModelType_nullInteractionNote_throwsIllegalValueException() {
        // EP: a null entry invalidates the notes list even when the other entries are valid.
        List<JsonAdaptedInteractionNote> notesWithNull =
                new ArrayList<>();

        notesWithNull.add(
                new JsonAdaptedInteractionNote(
                        "Met client",
                        "2026-10-26T20:30:00"));

        notesWithNull.add(null);

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null,
                        notesWithNull);

        assertThrows(
                IllegalValueException.class,
                JsonAdaptedPerson.MESSAGE_NULL_INTERACTION_NOTE,
                person::toModelType);
    }

    @Test
    public void toModelType_missingInteractionNoteTimestamp_throwsIllegalValueException() {
        // EP: otherwise valid note text has no timestamp; report the missing field.
        JsonAdaptedInteractionNote noteWithoutTimestamp =
                new JsonAdaptedInteractionNote(
                        "Met client",
                        null);

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null,
                        List.of(noteWithoutTimestamp));

        assertThrows(
                IllegalValueException.class,
                JsonAdaptedInteractionNote.MISSING_CREATED_AT_MESSAGE,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidInteractionNoteTimestamp_throwsIllegalValueException() {
        // EP: the timestamp is present but cannot be parsed; report invalid content.
        JsonAdaptedInteractionNote noteWithInvalidTimestamp =
                new JsonAdaptedInteractionNote(
                        "Met client",
                        "not-a-date");

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        null,
                        VALID_TAGS,
                        null,
                        List.of(noteWithInvalidTimestamp));

        assertThrows(
                IllegalValueException.class,
                JsonAdaptedInteractionNote.INVALID_CREATED_AT_MESSAGE,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        // EP: a required name is present but malformed; keep the other fields valid.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        INVALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        // EP: the required name is absent, so report a missing field rather than invalid content.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        null,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = String.format(
                MISSING_FIELD_MESSAGE_FORMAT,
                Name.class.getSimpleName());
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        // EP: a phone is present but has a misplaced plus sign; keep the other fields valid.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        INVALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_tooLongPhone_throwsIllegalValueException() {
        // EP: a saved phone is far over the length limit; reject it instead of overflowing the stack.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        "9".repeat(5000),
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        assertThrows(
                IllegalValueException.class,
                Phone.MESSAGE_CONSTRAINTS,
                person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        // EP: the required phone is absent.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        null,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = String.format(
                MISSING_FIELD_MESSAGE_FORMAT,
                Phone.class.getSimpleName());
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        // EP: an email is present but lacks the required separator.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        INVALID_EMAIL,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_tooLongEmail_throwsIllegalValueException() {
        // EP: a saved email is far over the length limit; reject it instead of overflowing the stack.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        "a.".repeat(5000) + "a@b.com",
                        VALID_ADDRESS,
                        VALID_TAGS);
        assertThrows(
                IllegalValueException.class,
                Email.MESSAGE_CONSTRAINTS,
                person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        // EP: the required email is absent.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        null,
                        VALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = String.format(
                MISSING_FIELD_MESSAGE_FORMAT,
                Email.class.getSimpleName());
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        // EP: the required address is present but blank.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        INVALID_ADDRESS,
                        VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        // EP: the required address is absent.
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        null,
                        VALID_TAGS);
        String expectedMessage = String.format(
                MISSING_FIELD_MESSAGE_FORMAT,
                Address.class.getSimpleName());
        assertThrows(
                IllegalValueException.class,
                expectedMessage,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        // EP: at least one tag is malformed; valid tags do not make the whole list acceptable.
        List<JsonAdaptedTag> invalidTags =
                new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        invalidTags);

        assertThrows(
                IllegalValueException.class,
                person::toModelType);
    }

    @Test
    public void toModelType_nullTagEntry_throwsIllegalValueException() {
        // EP: a tag entry is null, rather than a non-null tag with invalid content.
        List<JsonAdaptedTag> tagsWithNull =
                new ArrayList<>(VALID_TAGS);
        tagsWithNull.add(null);

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(
                        VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        tagsWithNull);

        assertThrows(
                IllegalValueException.class,
                JsonAdaptedPerson.MESSAGE_NULL_TAG,
                person::toModelType);
    }
}
