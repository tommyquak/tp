package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        if (newData instanceof AddressBook otherAddressBook) {
            // Another AddressBook already holds no duplicates, so skip the check that compares every pair.
            persons.setPersons(otherAddressBook.persons);
            return;
        }
        setPersons(newData.getPersonList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    /**
     * Returns true if {@code other} holds the same contacts in the same order, with every tag spelled exactly
     * the same. This is stricter than {@link #equals(Object)}, where tags that differ only in letter case are
     * equal. Use it to decide whether data must be saved, because the data file keeps each tag's exact spelling.
     */
    public boolean isIdenticalTo(ReadOnlyAddressBook other) {
        requireNonNull(other);
        List<Person> ownPersons = getPersonList();
        List<Person> otherPersons = other.getPersonList();
        if (ownPersons.size() != otherPersons.size()) {
            return false;
        }
        for (int i = 0; i < ownPersons.size(); i++) {
            Person ownPerson = ownPersons.get(i);
            Person otherPerson = otherPersons.get(i);
            if (ownPerson == otherPerson) {
                // Contacts a command did not touch are shared by both lists, so this skips most comparisons.
                continue;
            }
            if (!ownPerson.equals(otherPerson) || !getTagNames(ownPerson).equals(getTagNames(otherPerson))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the exact spellings of the tags of {@code person}.
     */
    private static Set<String> getTagNames(Person person) {
        return person.getTags().stream().map(tag -> tag.tagName).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
