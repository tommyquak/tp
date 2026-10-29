package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; stored in Unicode NFC form; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with a letter and end with a letter, a digit, or a period directly after a letter "
                    + "(as in \"Jr.\"). They may contain letters, digits, spaces, hyphens, apostrophes, periods, "
                    + "commas and slashes between characters.";

    /*
     * A name starts with a letter and ends with a letter, mark or decimal digit, with name
     * punctuation such as "s/o" and "d/o" allowed between them. It may instead end with a period
     * directly after a letter and any of its combining marks, for suffixes such as "Jr." and "Sr.".
     * Digits are decimal digits (\p{Nd}) from any script, so number symbols such as fractions and
     * superscripts are rejected.
     */
    public static final String VALIDATION_REGEX =
            "(?:\\p{L}(?:[\\p{L}\\p{M}\\p{Nd} '.,/-]*[\\p{L}\\p{M}\\p{Nd}])?"
                    + "|\\p{L}(?:[\\p{L}\\p{M}\\p{Nd} '.,/-]*\\p{L})?\\p{M}*+\\.)";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        String trimmedName = name.trim();
        checkArgument(isValidName(trimmedName), MESSAGE_CONSTRAINTS);
        fullName = Normalizer.normalize(trimmedName.replaceAll(" +", " "), Form.NFC);
    }

    /**
     * Returns true if a given string is a valid name after Unicode NFC normalization.
     */
    public static boolean isValidName(String test) {
        return Normalizer.normalize(test, Form.NFC).matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
