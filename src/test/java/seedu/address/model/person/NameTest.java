package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_extraSpaces_normalizesName() {
        assertEquals("Alice Pauline Tan", new Name("Alice  Pauline   Tan ").toString());
    }

    @Test
    public void constructor_decomposedUnicode_normalizesToNfc() {
        // EP: decomposed Latin accents are stored in the same form as their composed equivalents.
        Name decomposedName = new Name("Jose\u0301 Nu\u0301n\u0303ez");
        Name composedName = new Name("José Núñez");

        assertEquals("José Núñez", decomposedName.fullName);
        assertEquals(composedName, decomposedName);
        assertEquals(composedName.hashCode(), decomposedName.hashCode());

        // EP: canonical composition also applies to non-Latin scripts, such as Hangul syllables.
        Name decomposedHangul = new Name("\u1100\u1161 Kim");
        assertEquals("\uac00 Kim", decomposedHangul.fullName);
        assertEquals(new Name("\uac00 Kim"), decomposedHangul);
        assertEquals(new Name("\uac00 Kim").hashCode(), decomposedHangul.hashCode());
    }

    @Test
    public void equals_differentCaseOrAccents_returnsFalse() {
        Name name = new Name("José Núñez");

        // EP: normalization preserves accents and the existing case-sensitive value equality.
        assertNotEquals(name, new Name("Jose Núñez"));
        assertNotEquals(name, new Name("JOSÉ NÚÑEZ"));
    }

    @Test
    public void isValidName() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // EP: blank input with no starting letter.
        assertFalse(Name.isValidName("")); // Boundary value: zero characters.
        assertFalse(Name.isValidName(" ")); // spaces only

        // EP: characters outside the permitted letters, digits, spaces and name punctuation.
        assertFalse(Name.isValidName("^"));
        assertFalse(Name.isValidName("peter*"));

        // EP: otherwise permitted characters used before the required first letter.
        assertFalse(Name.isValidName("12345")); // must start with a letter
        assertFalse(Name.isValidName("-Alice"));
        assertFalse(Name.isValidName(" Alice"));
        assertFalse(Name.isValidName("Alice-"));
        assertFalse(Name.isValidName("Alice'"));
        assertFalse(Name.isValidName("Alice,"));
        assertFalse(Name.isValidName("Alice/"));

        // EP: a final period that does not come directly after a letter.
        assertFalse(Name.isValidName("Alice ."));
        assertFalse(Name.isValidName("Alice -."));
        assertFalse(Name.isValidName("Alice-."));
        assertFalse(Name.isValidName("Alice.."));
        assertFalse(Name.isValidName("Agent 007."));

        // EP: number symbols that are not decimal digits.
        assertFalse(Name.isValidName("A\u00bd")); // A½
        assertFalse(Name.isValidName("John\u00b2")); // John²
        assertFalse(Name.isValidName("Alice\u00bc")); // Alice¼
        assertFalse(Name.isValidName("Peter \u00bd Tan"));

        // EP: names starting with a letter and containing only letters, digits and spaces.
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names

        // EP: permitted punctuation inside a name, represented by each supported punctuation mark.
        assertTrue(Name.isValidName("Anne-Marie"));
        assertTrue(Name.isValidName("O'Connor"));
        assertTrue(Name.isValidName("Dr. Lee"));
        assertTrue(Name.isValidName("Tan, Alice"));
        assertTrue(Name.isValidName("Raj s/o Kumar"));
        assertTrue(Name.isValidName("Aisha d/o Rahman"));

        // EP: a final period directly after a letter, as in name suffixes.
        assertTrue(Name.isValidName("Martin Luther King Jr."));
        assertTrue(Name.isValidName("Robert Downey Sr."));
        assertTrue(Name.isValidName("Alice."));
        assertTrue(Name.isValidName("A.")); // Boundary value: shortest name with a final period.
        assertTrue(Name.isValidName("Jose\u0301 Jr.")); // composes to a letter before the period
        assertTrue(Name.isValidName("\u0921\u0949.")); // combining mark kept on the letter

        // EP: decimal digits from any script.
        assertTrue(Name.isValidName("Ali \u0663")); // Arabic-Indic digit three
        assertTrue(Name.isValidName("Ali \u0663 Tan"));

        // EP: Unicode letters, represented in both composed and decomposed forms.
        assertTrue(Name.isValidName("José Núñez"));
        assertTrue(Name.isValidName("Jose\u0301 Núñez"));
        assertTrue(Name.isValidName("\uac00 Kim"));
        assertTrue(Name.isValidName("\u1100\u1161 Kim"));

        // EP: normalization must not make otherwise invalid names valid.
        assertFalse(Name.isValidName("José^"));
        assertFalse(Name.isValidName("Jose\u0301^"));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
