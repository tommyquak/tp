---
layout: default.md
title: "Developer Guide"
pageNav: 3
---

# Easy-Insurance Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
* Tommy Quak used AI coding assistants (Claude and Codex) to help with parts of his coding, testing and documentation.
  He reviewed all of the resulting code himself, and every change was also reviewed by a teammate, built and tested
  before it was merged.
* Kasey Ho used Codex to assist with implementation, regression tests and
  documentation. All code was reviewed by himself, and all pull requests were
  also reviewed by teammates, built and approved before being merged.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

The sequence diagram below focuses on `FindCommand.execute(model)`, after parsing has produced a `FindCommand`.
`FindCommandParser` supplies its predicate: `find alice` creates a `NameContainsKeywordsPredicate`, while
`find p/9123` creates a `PhoneContainsKeywordPredicate`. Both commands follow the same execution sequence.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Find command execution: filter contacts, obtain the list and format the result count" />

`LogicManager` calls `execute(model)` and receives the returned `CommandResult`.
The `matchingPersons` and `count` names explain the intermediate values in the chained expression
`model.getFilteredPersonList().size()`; they are not additional fields or variables in the implementation.
`Model` returns the list, and `FindCommand` calls `size()` on that list before calling the static method on `Messages`.

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `CommandInputParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a contact).<br>
   A command can require several interactions with the `Model` to complete the operation.
   The sequence diagrams omit internal model operations for clarity.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `CommandInputParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `CommandInputParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

The first diagram shows the Model's main classes and the lists of contacts they maintain.
The second expands the fields of the same `Person` class. Methods are omitted from both diagrams.

<puml src="diagrams/ModelClassDiagram.puml" alt="Model overview showing all contacts and the filtered view" />

<puml src="diagrams/PersonClassDiagram.puml" alt="Person fields, including one shared Status value and optional dates" />

`Status.DEFAULT` is `PROSPECT`. Each `Person` stores one status, optionally one follow-up date, optionally one last-contact date, and zero or more interaction notes.
`Status` is an enum whose values can be shared by many contacts, so its link is an ordinary association with multiplicity `1`.
`Name` starts with a Unicode letter, ends with a letter or decimal digit (possibly carrying combining marks), and accepts common name punctuation
between characters, up to 1000 input characters; `Phone` accepts
an optional leading plus sign and separators between at least three digits.
`Phone` is capped at 30 characters and `Email` at 254. Both lengths are checked before the regex, so
a very long value from a command or the data file is rejected instead of overflowing the stack.


The `Model` component,

* stores the contact data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user's preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both contact data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

### Loading and backing up the data file

Contact data is stored in `data/addressbook.json`, in the folder the app is launched from. `MainApp#initModelManager`
reads it once at startup by calling `Storage#readAddressBookWithBackup()`, which `StorageManager` passes on to
`JsonAddressBookStorage`. The activity diagram below shows the possible outcomes.

<puml src="diagrams/DataFileLoadingActivityDiagram.puml" alt="Loading the data file at startup" />

1. If the data file does not exist, `JsonUtil.readJsonFile` returns `Optional.empty()` and `MainApp` loads the
   sample contacts from `SampleDataUtil`. The file is created the first time a command changes contact data.
   `JsonAddressBookStorage#readAddressBook` first checks that the file is really absent. If its folder cannot be
   opened, or the file is a link to a missing file, it throws a `DataLoadingException` instead, which leads to
   step 6. Treating these cases as a missing file would load the sample contacts, and the next save would replace
   the real data with them.
2. Otherwise, `FileUtil.readFromFile` reads the bytes and decodes them as UTF-8 with a decoder that reports invalid
   bytes instead of replacing them with the character U+FFFD. Replaced characters would be written back on the next
   save, so the original text would be lost. The error gives the byte offset of the first invalid sequence.
3. `JsonUtil.fromJsonString` parses the text in three steps:
   * `requireValidStructure` reads the tokens without recursion. It rejects a key that appears twice in one object,
     and objects or arrays nested more than 100 levels deep (`MAX_NESTING_DEPTH`), giving the line and column.
     Without this pass, a repeated key would be reported without its location, and deep nesting would overflow
     the stack inside Jackson. It also rejects text holding half of a surrogate pair, such as an escaped
     `\ud83c` with no low half, which valid UTF-8 cannot hold and which would be saved as `?`.
   * Jackson maps the JSON value to a `JsonSerializableAddressBook`. Unknown fields are ignored.
   * `requireNoTrailingContent` rejects anything other than whitespace after the JSON value, because Jackson stops
     reading after the first value and would drop the rest without an error.

   `readJsonFile` also rejects a file that contains only `null`, and an empty file, which Jackson would report
   without a useful message.
4. `JsonSerializableAddressBook#toModelType` converts each `JsonAdaptedPerson`, which checks every field with the
   same model methods that the commands use, such as `Name#isValidName`. Null entries and duplicate contacts are
   rejected. A missing or blank `status` becomes `PROSPECT` and a missing `followUp` means no follow-up, so files
   without the `status` and `followUp` fields still load. Values that later versions made stricter, such as v1.4
   phones with repeated separators, are rejected like any other invalid value.
5. A failure in any of steps 2 to 4 causes a `DataLoadingException`. `readAddressBookWithBackup` copies the data
   file to `addressbook.json.bak` in the same folder, replacing any older backup, and rethrows the exception.
   `MainApp` logs a warning and starts with an empty `AddressBook`. It also keeps a notice from
   `StartupNotice#forUnloadableDataFile`, which `UiManager` shows in the result box once the window opens. The
   notice names the data file, the `.bak` copy and the reason. `JsonSerializableAddressBook` starts the reason with
   the position of the invalid contact, e.g. `Contact 3 is invalid.`, so the user knows where to look. Jackson's
   own messages name internal classes and quote the file, so `StartupNotice` replaces them with a plain reason and
   the line number only, because Jackson's column is not always right.
6. If the data file is not a regular file, such as a folder, or the copy cannot be made, `readAddressBookWithBackup`
   throws a `DataBackupException` instead. `MainApp#initModelManager` rethrows it as an `IllegalStateException`, so
   the app does not open. The message names the data file and the backup path and says that the original file was
   not changed.

Starting with an empty list does not save anything. The invalid data file stays on disk until the first command
that changes contact data replaces it, because `LogicManager` skips saving when the data is unchanged.
From then on, the `.bak` copy is the only copy of the old data.

#### Design considerations

**Aspect: What to do when the data file cannot be loaded**

* **Alternative 1 (current choice):** Back up the file, then start with an empty contact list.
  * Pros: The app opens whenever the invalid file can be backed up, and the user's data is kept in full in the
    `.bak` file for recovery.
  * Cons: The next change replaces the invalid file, so the user must recover from the `.bak` file. A later
    failed load replaces that backup too.
* **Alternative 2:** Refuse to start until the file is fixed.
  * Pros: The invalid file can never be overwritten.
  * Cons: The agent cannot use the app at all until they find and fix the problem in the JSON by hand. We keep
    this behaviour only for the case where the backup cannot be made, because starting then would put the only
    copy of the data at risk.

**Aspect: How strictly to read the file**

* **Alternative 1 (current choice):** Reject invalid UTF-8, unpaired surrogate escapes, repeated keys, deep
  nesting, trailing content and invalid field values. Accept unknown fields and missing optional fields.
  * Pros: No contact data is dropped or changed without the user knowing (only unknown fields, which the app does
    not use, are discarded on the next save), and a rejected file is backed up whole. Files without the `status`
    and `followUp` fields still load.
  * Cons: One bad value makes the whole file load as an empty list, instead of skipping only the bad contact.
* **Alternative 2:** Read leniently, for example by skipping invalid contacts, keeping the last of repeated keys
  and replacing bad bytes.
  * Pros: More of the data appears straight away.
  * Cons: The skipped contacts and replaced text are lost for good on the next save, with no backup and no
    warning, so the user may not notice until much later.

### Completing a follow-up

The `done INDEX` command clears the pending follow-up of the contact at the specified index in the currently
displayed list. It preserves the contact's name, phone, email, address, status, tags, last-contact date and
interaction notes, including their text, stored order and timestamps.

The sequence diagram below shows a successful `done 1` command when the first displayed contact has a pending
follow-up. Individual field getters and the internal calls within `Model` and `Storage` are omitted for clarity.

<puml src="diagrams/DoneSequenceDiagram.puml" alt="Completing a pending follow-up" />

1. `LogicManager` passes the command text to `CommandInputParser`, which creates a `DoneCommandParser`.
2. `DoneCommandParser` validates the argument using `ParserUtil.parseIndex` and creates a `DoneCommand` with
   the resulting index. The command is returned to `LogicManager` for execution.
3. Before execution, `LogicManager` copies the address book and remembers the current list filter and sort order.
   `DoneCommand` obtains the currently displayed contacts from `Model`, checks the index and selects the
   contact. It then checks that the selected contact has a pending follow-up.
4. The command constructs a replacement `Person`, copying the other contact details and setting the
   follow-up to `Optional.empty()`. It calls `Model.setPerson` to replace the original contact in memory.
5. The command returns a `CommandResult` with the original contact's name and scheduled date:
   `Marked follow-up for <name> as completed (scheduled for <DD-MM-YYYY>).`
   `LogicManager` obtains the updated address book from `Model` and passes it to `Storage.saveAddressBook`.
   After the save succeeds, `LogicManager` returns the result to its caller.

`DoneCommand` interacts with the `Model` interface to update the contact. `LogicManager` coordinates saving
through the `Storage` interface; the model does not call storage directly.

#### Unsuccessful outcomes

* An invalid index argument, such as a missing, zero, negative or non-numeric index, causes a `ParseException`.
* A positive index outside the currently displayed list causes a `CommandException`.
* A contact without a pending follow-up causes a `CommandException` with the message
  `<name> does not have a pending follow-up.`.

These validation failures leave the contact unchanged. Execution stops before `Model.setPerson` and
`Storage.saveAddressBook` are called.

Before executing a command, `LogicManager` snapshots the current `AddressBook`. Copying a concrete
`AddressBook` takes linear time: it copies the already-validated list and shares its immutable `Person`
values, without repeating pairwise duplicate checks. The snapshot has its own list, so later additions,
edits and deletions do not change it. Other read-only implementations and subclasses still undergo
duplicate validation when copied.

If saving fails with an `IOException` (including `AccessDeniedException`), `LogicManager` restores the
pre-command address book, filter and sort order, then throws a `CommandException` with a save-error message.
Permission errors name the configured data file rather than an internal temporary file. If a file blocks a
required parent folder, the error names both the data file and the blocking path.
The restored contact still has its pending follow-up, and the displayed indexes return to their previous order.
No success result is returned. This rollback applies to commands that change contact data, including
interaction notes. `LogicManager` compares the address book before and after execution and skips saving
when the data is unchanged. Read-only commands such as `list`, `find`, `due`, `history`, `help` and `exit`
therefore still work when the data file cannot be written, and their view changes are retained.

`JsonAddressBookStorage` writes the complete replacement JSON to a temporary file in the destination's
directory, then atomically replaces the destination. A failed write or replacement leaves the previous JSON
intact. It deletes the temporary file on failure; if cleanup also fails, that exception is retained with the
original failure for diagnosis. If the file system does not support atomic replacement, saving fails safely
instead of falling back to an operation that could damage the existing file.

### Managing interaction notes

The `note`, `history` and `deletenote` commands record, display and remove a contact's interaction notes.
Their contact indexes refer to the currently displayed list, including any active filter or sort order.

#### Model and storage

Each `Person` holds a list of immutable `InteractionNote` values. Each note contains its text and a
`LocalDateTime` creation timestamp. `Person` copies the supplied list using `List.copyOf`, so commands cannot
change the stored list through `getInteractionNotes()`. Adding or deleting a note constructs a replacement
`Person` and calls `Model.setPerson`, preserving all other contact fields, including both dates.

`JsonAdaptedPerson` saves the list in the `interactionNotes` field. `JsonAdaptedInteractionNote` saves each
note's `text` and `createdAt`, preserving the list order and timestamp when the data is loaded again.
A missing or null `interactionNotes` field becomes an empty list, allowing files without notes to load.
A null note entry, invalid note text, or missing or invalid timestamp rejects the data file through the
[existing loading and backup process](#loading-and-backing-up-the-data-file).

#### Adding a note

For `note INDEX nt/NOTE`:

1. `CommandInputParser` delegates to `NoteCommandParser`. The parser finds the first `nt/` prefix separated
   from the contact index by whitespace and validates the index with `ParserUtil.parseIndex`.
2. Everything after that prefix is note text. Further prefix-like text, such as `nt/`, `st/` or `a/`, is
   part of the note rather than another argument. `InteractionNote.isValidNote` checks the text before
   the parser constructs the note with the current local timestamp and creates a `NoteCommand`.
3. `NoteCommand` checks the contact index against `Model.getFilteredPersonList()`. It copies the contact's
   notes, appends the new note and replaces the contact through `Model.setPerson`.
4. `LogicManager` saves the changed address book before returning the success result. If saving fails,
   it restores the pre-command data and view, including the original notes, as described above.

#### Viewing and deleting notes

`history INDEX` is parsed by `HistoryCommandParser`. `HistoryCommand` selects the displayed contact and
reverses a copy of its stored note list, so the most recently added note is shown first. The stored creation
timestamps are only displayed, not used for ordering, so a clock or time-zone change cannot reorder notes. It
returns a `CommandResult`
containing numbered notes with timestamps formatted as `dd-MM-yyyy HH:mm`, or a message that the contact
has no notes. It does not change the stored note order or contact data, so `LogicManager` skips saving.

`deletenote CONTACT_INDEX NOTE_INDEX` is parsed by `DeleteNoteCommandParser`, which validates both positive
indexes using `ParserUtil.parseIndex`. `DeleteNoteCommand` selects the displayed contact and counts `NOTE_INDEX` from the
end of the stored list, the same newest-added-first order that `history` shows. It removes that note from a copy of the
stored list, preserving the relative order of the remaining notes, then replaces the contact and lets
`LogicManager` save it. The success result identifies the contact, note index and deleted text.

The contact index and note index refer to different lists. For example, after `history 2`, `deletenote 2 1`
deletes the first note shown for the second displayed contact. Adding or deleting notes can change the
history indexes, so the user should view the history again before choosing another note to delete.

#### Validation and failure handling

Malformed arguments and invalid note text cause a `ParseException`. Positive indexes outside the displayed
contact list or the selected contact's history cause a `CommandException`. These failures leave the data
unchanged. A failed save after adding or deleting a note restores the previous notes and does not return
a success result. Viewing history remains possible when the data file cannot be written.

#### Design considerations

* **Replace the contact (current choice):** Copying the note list and replacing the immutable `Person`
  keeps model updates observable and lets the existing address-book snapshot restore notes after a failed save.
* **Mutate notes in place (alternative):** This would avoid copying the list, but a snapshot sharing the same
  mutable notes could change too, preventing rollback from restoring the original history.

### Viewing due follow-ups

`due [DAYS]` shows contacts whose follow-up date is on or before today plus `DAYS`, earliest first.

* `DueCommandParser` accepts no argument (0 days) or a whole number from 0 to 365, so the cutoff date can never overflow.
* `DueCommand` works out the cutoff from a `Clock`. The app passes the system clock, and tests pass a fixed one, so the tests do not depend on the real date.
* `FollowUpDueByPredicate` keeps contacts that have a follow-up on or before the cutoff. Contacts without a follow-up are left out.
* `Model#updateFilteredPersonList(Predicate, Comparator)` filters and sorts the displayed list in one step. `ModelManager` wraps its `FilteredList` in a `SortedList`, and `getFilteredPersonList()` returns the sorted view, so index-based commands such as `done 1` act on the order the user sees.
* The one-argument `updateFilteredPersonList(Predicate)` clears the comparator, so `list` and `find` replace the current view and return to stored order. Adding or editing a contact retains the active filter and sort order; the displayed list updates as contacts change.

The sequence diagram below shows how `due 7` is executed.

<puml src="diagrams/DueSequenceDiagram.puml" alt="Interactions inside the Logic and Model components for the `due 7` command" />

**Design considerations:**

* **Sorting the displayed list (current choice):** the stored order is unchanged and only the view is sorted, so the data file stays the same and `list` restores the usual order.
* **Sorting the stored contacts (alternative):** simpler to display, but it would permanently reorder the data file every time `due` is run.

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* junior insurance agent who is building their own base of prospects and clients
* has a growing number of prospects and clients, and struggles to remember who to follow up with and when
* currently keeps prospects scattered across phone contacts, chat apps and spreadsheets
* works alone on their own laptop and prefers desktop apps
* can type fast and prefers typing and keyboard shortcuts to mouse interactions
* is new to CLI apps, but willing to learn a few short commands

**Value proposition**: Keep track of prospects and clients, what status each one has, and when to follow up next, faster than juggling phone contacts, chat apps and spreadsheets.

Easy-Insurance focuses on managing prospects and clients. It does not send messages or make calls, generate quotations or premiums, recommend policies, manage claims, track commissions, or replace the agency's official CRM. It is a single-user app, so nothing is shared with other agents.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                  | I want to …                                            | So that I can…                                                       |
|----------|------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------|
| `* * *`  | new insurance agent                      | add a new prospect                                      | keep track of people I may want to follow up with                    |
| `* * *`  | insurance agent                          | view my list of prospects and clients                   | see the people I am currently managing                               |
| `* * *`  | insurance agent looking for a person     | search for a contact by name                            | quickly retrieve the contact's information                           |
| `* * *`  | insurance agent                          | edit a contact's details                                | keep their information up to date when it changes                    |
| `* * *`  | insurance agent                          | delete a contact                                        | remove people who no longer want to hear from me                     |
| `* * *`  | insurance agent                          | set a contact's status to prospect, client or inactive  | tell at a glance who is still worth chasing                          |
| `* * *`  | insurance agent                          | set a follow-up date for a contact                      | remember who I promised to call back                                 |
| `* * *`  | insurance agent                          | mark a follow-up as done                                | keep my list to what is still pending                                |
| `* * *`  | insurance agent unfamiliar with CLI      | see a guide to the available commands and their formats | learn to use the app without prior CLI experience                    |
| `* *`    | insurance agent                          | record when I last contacted someone                    | see who I have not spoken to in a while                              |
| `* *`    | insurance agent                          | record a note about an interaction with a contact        | remember what we discussed and what to follow up on                   |
| `* *`    | insurance agent preparing to contact someone | view that contact's interaction history               | continue the conversation with the context of earlier discussions     |
| `* *`    | insurance agent correcting a mistake     | remove an incorrect interaction note                    | avoid relying on misleading information in future conversations       |
| `* *`    | insurance agent planning my day          | see all follow-ups due today                            | know who to call before I start                                      |
| `* *`    | insurance agent unfamiliar with CLI      | get a clear error message when I type a command wrongly | learn from the error and type it correctly next time                 |
| `* * *`  | insurance agent                          | filter my list by status                                | focus on prospects when planning outreach                            |
| `* *`    | insurance agent                          | filter my list by tag                                   | reach everyone in one group, such as all referrals, at once          |
| `* *`    | insurance agent with many contacts       | search by partial name or phone number                  | find someone when I only remember part of it                         |
| `* *`    | insurance agent                          | see overdue follow-ups                                  | catch the people I missed                                            |
| `* *`    | insurance agent                          | see follow-ups due this week                            | plan ahead when today is already full                                |
| `* *`    | insurance agent                          | push a follow-up to a later date                        | reschedule when a prospect asks me to call next week                 |
| `* *`    | insurance agent                          | record the policies a client has bought                 | avoid pitching what they already have                                |
| `* *`    | insurance agent                          | see renewals due in the next month                      | plan retention calls early                                           |
| `* *`    | insurance agent                          | sort contacts by how many times I met them              | identify prospects who are more engaged with me                      |
| `* * *`  | insurance agent                          | be stopped from adding a contact whose name already exists | keep my list clean                                               |
| `* *`    | insurance agent                          | be asked to confirm before deleting                     | avoid deleting someone by accident                                   |
| `* *`    | insurance agent                          | undo my last change                                     | recover from a mistake quickly                                       |
| `* *`    | insurance agent                          | see the contacts I added most recently                  | check what I keyed in after a busy day                               |
| `* *`    | insurance agent                          | mark a contact as do-not-contact                        | respect people who said no without deleting their record             |
| `* *`    | insurance agent back from leave          | see everything that went overdue while I was away       | catch up in one go                                                   |
| `*`      | insurance agent                          | import contacts from a spreadsheet                      | bring in lead lists without retyping them                            |
| `*`      | insurance agent                          | export my contacts to a spreadsheet                     | send my manager my numbers                                           |
| `*`      | insurance agent                          | archive contacts I no longer work with                  | keep my main list short without losing their history                 |
| `*`      | insurance agent who types fast           | use short forms for common commands                     | save keystrokes on things I do many times a day                      |
| `*`      | insurance agent                          | recall a previous command                               | repeat it without retyping                                           |
| `*`      | insurance agent switching laptops        | copy my data file to a new computer                     | keep working without starting over                                   |
| `*`      | insurance agent                          | get a clear message if my data file is corrupted        | know what happened instead of losing everything silently             |
| `*`      | insurance agent                          | record a client's birthday                              | keep in touch between renewals                                       |
| `*`      | insurance agent                          | record a prospect's life stage                          | pitch products that fit them                                         |
| `*`      | insurance agent                          | record the language a client prefers                    | speak to them in a language they are comfortable with                |
| `*`      | insurance agent                          | link two contacts as family                             | pitch family plans to the right people                               |
| `*`      | insurance agent                          | see how many prospects, clients and lost leads I have   | track progress toward my target                                      |
| `*`      | insurance agent                          | set a preferred contact channel for a person            | reach them the way they like                                         |
| `*`      | insurance agent                          | copy a phone number with one command                    | paste it into WhatsApp quickly                                       |
| `*`      | insurance agent                          | see how many contacts I added this month                | check if I am prospecting enough                                     |

### Use cases

(For all use cases below, the **System** is Easy-Insurance and the **Actor** is the insurance agent.)

**Recoverable saving failures:** For any use case that changes contact data, the system confirms completion only
after saving succeeds. If saving fails, the system reports the failure; the previous contact data and displayed
list, including its filter and ordering, remain in place. The requested change has not been saved. After the
storage problem is corrected, the agent can submit the request again. The system does not retry automatically.

**Use case: Add a prospect and plan a follow-up**

**MSS**

1. Agent provides the new prospect's name and contact details.
2. System validates the details and saves the contact with status `PROSPECT`.
3. System confirms the new prospect. If the current view filters it out, the agent can run `list` to show it.
4. Agent sets a follow-up date for the prospect.
5. System saves the pending follow-up and shows its date with the prospect.

   Use case ends.

**Extensions**

* 2a. The contact details are invalid or incomplete.
  * 2a1. System explains which details need correction without changing saved contact data.
  * 2a2. Agent corrects the details.
  * Use case resumes at step 2.
* 2b. A contact with the same name already exists, ignoring case and extra spaces.
  * 2b1. System explains that the contact already exists without changing saved contact data.
  * Use case ends.
* 4a. The follow-up date is invalid.
  * 4a1. System shows an error and does not change the saved contact.
  * Use case resumes at step 4.
* 5a. The follow-up date is in the past.
  * 5a1. System saves the date and warns the agent that it is in the past.
  * Use case ends.

**Use case: Find contacts by name or phone**

**MSS**

1. Agent enters `find KEYWORD [MORE_KEYWORDS]...` or `find p/PHONE_DIGITS`.
2. System ignores leading and trailing spaces and checks that the search value is present.
   A phone search must contain digits only.
3. For a name search, the system matches the start of any word in a contact's name against any supplied keyword,
   ignoring case. For example, `find ta` matches `John Tan`.
   For a phone search, it looks for the supplied consecutive digits in the phone number, ignoring stored formatting
   such as spaces and dashes.
4. System shows only matching contacts and reports how many were found.

   Use case ends.

**Extensions**

* 2a. No name keyword was supplied.
  * 2a1. System shows `Please enter at least one name keyword to search for. Example: find alice tan`.
  * Use case ends without changing the contact list.
* 2b. No phone digits were supplied after `p/`.
  * 2b1. System shows `Please enter at least one phone digit to search for. Example: find p/9123`.
  * Use case ends without changing the contact list.
* 2c. A phone search contains non-digit characters.
  * 2c1. System explains that the phone search must contain digits only.
  * Use case ends without changing the contact list.
* 2d. The search contains a prefix other than `p/`, such as `n/`, `st/` or `t/`.
  * 2d1. System names the prefix, explains that `find` only searches names or phone numbers, and points to `list st/STATUS` or `list t/TAG` for filtering.
  * Use case ends without changing the contact list.
* 3a. No contact matches the name keywords or phone digits.
  * 3a1. System shows an empty result list and reports `0 contacts listed.`
  * Use case ends.

**Use case: Edit a contact**

**MSS**

1. Agent views the contact to edit.
2. Agent provides one or more replacement details, including an optional status or follow-up date.
3. System validates the selected contact reference and the replacement details.
4. System updates the contact and confirms its updated details. If the current view filters it out, the agent can run `list` to show it.

   Use case ends.

**Extensions**

* 2a. Agent provides an empty follow-up date.
  * 2a1. System treats the empty date as a request to clear the follow-up as part of the edit, without changing the contact yet.
  * Use case resumes at step 3.
* 3a. A replacement detail is invalid.
  * 3a1. System shows the relevant validation error and does not change the contact.
  * Use case ends.
* 3b. The new name matches another contact's name, ignoring case and extra spaces.
  * 3b1. System shows `This contact already exists: NAME (PHONE).` with that contact's name and phone number, and does not change the contact.
  * Use case ends.
* 3c. The selected contact is not in the displayed list.
  * 3c1. System explains that the contact reference is invalid.
  * Use case ends without changing any contacts.
* 4a. The agent supplied a past follow-up date.
  * 4a1. After the successful update, System warns the agent that the saved follow-up date is in the past.
  * Use case ends.

**Use case: Complete a follow-up**

**MSS**

1. Agent views a contact with a pending follow-up.
2. System shows the scheduled follow-up date.
3. Agent requests to mark that follow-up as done.
4. System clears the follow-up and confirms the update. If the current view filters it out, the agent can run `list` to show it.

   Use case ends.

**Extensions**

* 3a. The contact has no pending follow-up.
  * 3a1. System explains that there is no follow-up to complete.
  * Use case ends without changing the contact.
* 3b. The selected contact is not in the displayed list.
  * 3b1. System explains that the contact reference is invalid.
  * Use case ends without changing any contacts.

**Use case: Review due follow-ups**

**MSS**

1. Agent requests to see the follow-ups due within a given number of days from today.
2. System works out the cutoff date, which is today plus that number of days.
3. System shows only the contacts whose follow-up date is on or before the cutoff date, including overdue ones,
   earliest date first. Contacts with the same date are sorted by name.
4. System reports how many follow-ups are due and the cutoff date.

   Use case ends.

**Extensions**

* 1a. Agent does not give a number of days.
  * 1a1. System uses 0 days, so the cutoff date is today.
  * Use case resumes at step 3.
* 1b. The number of days is not a whole number from 0 to 365.
  * 1b1. System shows `Number of days must be a whole number from 0 to 365, e.g. due 7.`
  * Use case ends without changing the displayed list.
* 3a. No contact has a follow-up on or before the cutoff date.
  * 3a1. System shows an empty list and reports that no follow-ups are due by the cutoff date.
  * Use case ends.
* 4a. Agent completes one of the listed follow-ups, referring to it by its position in the sorted list.
  * 4a1. System clears that follow-up as in the use case *Complete a follow-up*, and removes the contact from
    the list because it no longer has a follow-up.
  * Use case ends.

**Use case: Delete a contact**

**MSS**

1. Agent requests to list contacts, or finds contacts by name.
2. System shows the matching contacts with their index numbers.
3. Agent requests to delete the contact at a given index.
4. System removes the contact and confirms which contact was deleted.

   Use case ends.

**Extensions**

* 2a. No contacts are shown.

  Use case ends.

* 3a. The given index does not refer to a shown contact.
  * 3a1. System explains that the index is invalid and changes nothing.

    Use case resumes at step 2.

**Use case: Record an interaction note**

**MSS**

1. Agent requests to view contacts.
2. System shows the matching contacts with their references.
3. Agent supplies a note for a selected contact.
4. System records the note with its creation time and confirms which contact was updated.

   Use case ends.

**Extensions**

* 2a. No contacts are shown.
  * Use case ends.
* 3a. The contact reference is invalid.
  * 3a1. System explains the error without changing any contacts.
  * Use case ends.
* 3b. The note is blank, contains control characters or exceeds the allowed length.
  * 3b1. System explains the note constraints without adding the note.
  * Use case ends.
* 3c. System cannot save the note.
  * 3c1. System reports the saving failure and retains the previous notes, contact data and displayed list.
  * Use case ends without adding the note. Agent may retry after the storage problem is corrected.

**Use case: View interaction history**

**MSS**

1. Agent requests to view contacts.
2. System shows the matching contacts with their references.
3. Agent requests the interaction history of a selected contact.
4. System shows the contact's notes in reverse order of addition, each with a note reference and creation time.

   Use case ends.

**Extensions**

* 2a. No contacts are shown.
  * Use case ends.
* 3a. The contact reference is invalid.
  * 3a1. System explains the error without changing any contacts.
  * Use case ends.
* 3b. The contact has no notes.
  * 3b1. System reports that no interaction notes have been recorded for that contact.
  * Use case ends.

**Use case: Remove an incorrect interaction note**

**Precondition:** Agent has viewed the selected contact's current history and identified an incorrect note.

**MSS**

1. Agent requests to remove the note using the contact reference and the note reference shown in that history.
2. System removes the selected note and confirms the contact and deleted text.
3. Agent requests the contact's history again.
4. System shows the remaining notes with updated references, preserving their relative order.

   Use case ends.

**Extensions**

* 1a. Either reference is invalid.
  * 1a1. System explains which reference is invalid without changing any notes.
  * Use case ends.
* 1b. System cannot save the deletion.
  * 1b1. System reports the saving failure and retains all previous notes, contact data and the displayed list.
  * Use case ends without deleting the note. Agent may retry after the storage problem is corrected.
* 3a. The deleted note was the contact's only note.
  * 3a1. System reports that no interaction notes have been recorded for that contact.
  * Use case ends.

**Use case: Clear all contacts with confirmation**

**MSS**

1. Agent requests to clear all contacts.
2. System states the total number of contacts to be removed and asks for confirmation.
3. Agent confirms the removal.
4. System removes all contacts, including any hidden by the current view, and confirms completion.

   Use case ends.

**Extensions**

* 1a. Agent's request already includes confirmation.
  * Use case resumes at step 4 without another confirmation prompt.
* 2a. Agent decides not to confirm and continues with another task.
  * Use case ends without clearing any contacts.
* \*a. System cannot save the requested removal, including a request that already contains confirmation.
  * \*a1. System reports the saving failure and retains all contacts and the previous displayed list.
  * Use case ends without clearing any contacts. Agent may retry after the storage problem is corrected.

### Non-Functional Requirements

1. Easy-Insurance should run on Windows, macOS, and Linux with Java `25` or above installed.
2. An agent should be able to add, find, edit, and view contacts and manage follow-ups using the keyboard alone.
3. The app should work without an internet connection and should not transmit contact details to a network service.
4. Contact data should be stored locally as human-editable JSON.
5. The app should be distributed as a single JAR of at most 100 MB, with no installer required.

### Glossary

* **Contact**: A person recorded in Easy-Insurance, together with their contact details, status, and any follow-up.
* **Status**: Where a contact is in the agent's pipeline: `PROSPECT`, `CLIENT` or `INACTIVE`. New contacts are prospects unless told otherwise.
* **Prospect**: A contact the agent may sell a policy to but who is not yet a client.
* **Client**: A contact who has bought a policy from the agent.
* **Inactive**: A contact the agent is no longer actively pursuing but keeps in the app.
* **Follow-up**: The date on which the agent intends to next contact a prospect or client. A past date is accepted with a warning and is treated as an overdue follow-up until it is marked done.
* **Pending follow-up**: A follow-up date that is still recorded on a contact. Marking it done removes the date.
* **Duplicate contact**: A contact whose name matches an existing contact's name after ignoring letter case and extra spaces. Easy-Insurance refuses to add a duplicate or to rename a contact to another contact's name.
* **Index**: The number shown next to a contact in the currently displayed list. Commands such as `edit`, `delete` and `done` refer to contacts by index.
* **Name keyword**: Non-empty text used by `find` to match the start of any word in a contact's name, ignoring letter case.
* **Phone search digits**: A non-empty sequence of digits used by `find p/PHONE_DIGITS` to match consecutive digits in a phone number, ignoring stored formatting.
* **Tag**: A short label made of letters and digits, such as `referral`, that the agent attaches to a contact. A contact can have any number of tags.
* **Card**: The box in the contact list that shows one contact's details, including its status and follow-up date.
* **CLI**: Command line interface. The agent types commands into the command box instead of clicking buttons.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Disposable test setup

Use a separate, empty folder containing a copy of `EasyInsurance.jar`. Open a terminal in that folder and run
`java -jar EasyInsurance.jar`. Use only this copy for the tests below: several tests deliberately clear contacts
or make the data file unavailable. Keep your real data in its original folder and run only one app instance
against the test folder.

Unless a step says **terminal**, enter each command in the app's command box and press **Enter**. After a
rejected command, replace the retained input before entering the next command. Follow each section in order.
For restart checks, close the app and launch it again from the same test folder.

### Three-contact test fixture

Recreate this fixture before each section that asks for it. In the disposable app, run these commands in order:

1. `clear confirm`
1. `add n/Alice Tan p/+65 9123-4567 e/alice@example.com a/1 Test Road st/CLIENT f/02-01-2020 t/review`
1. `add n/Bob Lim p/92345678 e/bob@example.com a/2 Test Road st/PROSPECT f/01-01-2020`
1. `add n/Carol Ng p/93456789 e/carol@example.com a/3 Test Road st/CLIENT`
1. `list`

Expected: Exactly Alice, Bob and Carol appear at indexes 1, 2 and 3. Alice and Bob have the specified follow-up
dates; Carol has none. The past-date warnings on the first two additions are expected. All three initially have
no last-contact date or interaction notes. The file `data/addressbook.json` exists in the test folder.

### Launch and shutdown

1. Initial launch

   1. Download `EasyInsurance.jar` and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar EasyInsurance.jar`.<br>
      Expected: The window titled Easy-Insurance opens with a set of sample contacts, each showing a status badge.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app the same way.<br>
      Expected: The most recent window size and location are retained.

### Listing contacts with filters

1. Preparing test contacts

   1. Prerequisites: Use disposable test data and start with an empty contact list.

   1. Add these four contacts:

      `add n/Filter Client Referral p/91110001 e/one@example.com a/1 Test Road st/CLIENT t/ReFeRRaL`

      `add n/Filter Client Family p/91110002 e/two@example.com a/2 Test Road st/CLIENT t/family`

      `add n/Filter Prospect Referral p/91110003 e/three@example.com a/3 Test Road st/PROSPECT t/referral`

      `add n/Filter Inactive p/91110004 e/four@example.com a/4 Test Road st/INACTIVE`

1. Filtering by status

   1. Test case: `list st/CLIENT`<br>
      Expected: Only Filter Client Referral and Filter Client Family are shown.
      The result is `Listed 2 contacts with status CLIENT.`

1. Filtering by tag

   1. Test case: `list t/REFERRAL`<br>
      Expected: Only Filter Client Referral and Filter Prospect Referral are shown.
      The result is `Listed 2 contacts with tag REFERRAL.`

   1. Test case: `list t/ref`<br>
      Expected: No contacts are shown.
      The result is `Listed 0 contacts with tag ref.`

1. Combining filters

   1. Test case: `list st/client t/referral`<br>
      Expected: Only Filter Client Referral is shown.
      The result is `Listed 1 contact with status CLIENT and tag referral.`

   1. Test case: `list t/REFERRAL st/CLIENT`<br>
      Expected: The same contact is shown.
      The result is `Listed 1 contact with status CLIENT and tag REFERRAL.`

1. Resetting the filter

   1. Test case: `list`<br>
      Expected: All four contacts are shown.
      The result is `Listed all contacts.`

1. Adding and editing while a filter is active

   1. Prerequisites: Run `list st/CLIENT` with the four test contacts above.<br>
      Expected: Only the two clients are shown.

   1. Test case: `add n/Hidden Prospect p/91110005 e/five@example.com a/5 Test Road st/PROSPECT`<br>
      Expected: The contact is saved but remains hidden. The result explains that the current view hides it and suggests `list` to show all contacts.

   1. Test case: `add n/Visible Client p/91110006 e/six@example.com a/6 Test Road st/CLIENT`<br>
      Expected: The new client appears in the filtered list without a hidden-contact note.

   1. Test case: `edit 1 st/PROSPECT`<br>
      Expected: The edited contact leaves the filtered list. The other clients remain visible.

1. Rejecting invalid filters

   1. Test cases: `list st/LEAD` and `list st/`<br>
      Expected: The status constraint message is shown.

   1. Test cases: `list t/` and `list t/referral!`<br>
      Expected: The tag constraint message is shown.

   1. Test cases: `list st/CLIENT st/PROSPECT` and `list t/referral t/family`<br>
      Expected: An error identifies the repeated filter prefix.

   1. Test cases: `list extra`, `list extra st/CLIENT` and `list st/CLIENT extra`<br>
      Expected: An error is shown and the current displayed list is unchanged.

### Deleting a contact

1. Deleting a contact while all contacts are being shown

   1. Prerequisites: List all contacts using the `list` command, with multiple contacts in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is removed from the list. The result shows `Deleted contact: ` followed by that contact's name.

   1. Test case: `delete 0`<br>
      Expected: No contact is deleted. The result shows `Index must be a positive integer.`

   1. Test case: `delete 99` (where 99 is larger than the list size)<br>
      Expected: No contact is deleted. The result shows `There is no contact 99. The list shows N contacts.`

   1. Other incorrect delete commands to try: `delete`, `delete x`, `delete 1 2`<br>
      Expected: No contact is deleted. An error explains the problem.

1. Deleting a contact from a filtered list

   1. Prerequisites: Run `find` with a keyword that matches at least one contact.

   1. Test case: `delete 1`<br>
      Expected: The first contact in the filtered list is deleted, not the first contact overall.

### Adding a contact with a follow-up date

1. Adding a contact with a future follow-up date

   1. Prerequisites: Run `list` so that all contacts are shown. A filter left over from an earlier test, such as `find`, would hide the new contact.

   1. Test case: `add n/Sarah Lim p/92345678 e/sarah@example.com a/10 Tampines Street 1 f/25-12-2099`<br>
      Expected: The contact is added and the card shows `Follow-up: 25-12-2099`. No note is shown.

1. Adding a contact with a past follow-up date

   1. Test case: `add n/Ben Ong p/93456789 e/ben@example.com a/5 Bishan Road f/01-01-2020`<br>
      Expected: The contact is added. The result message ends with `Note: 01-01-2020 is in the past; it was saved anyway.`<br>
      The card shows `Follow-up: 01-01-2020 (overdue)` in red, and `due` lists the contact until its follow-up is marked done.

1. Adding a contact with an impossible or badly formatted date

   1. Test case: `add n/Ann Lee p/94567890 e/ann@example.com a/1 Clementi Ave f/31-02-2026`<br>
      Expected: No contact is added. Error: `31-02-2026 is not a real date.`

   1. Test case: `add n/Ann Lee p/94567890 e/ann@example.com a/1 Clementi Ave f/2026-12-25`<br>
      Expected: No contact is added. Error: `Follow-up date must be in DD-MM-YYYY format, e.g. 25-12-2026.`

### Editing a contact

1. Editing a contact's status and follow-up date

   1. Prerequisites: List the contacts using the `list` command and note the first contact's current details.

   1. Test case: `edit 1 st/CLIENT f/25-12-2099`<br>
      Expected: The first contact's status changes to `CLIENT`, its follow-up changes to `25-12-2099`,
      and the updated details are shown.

   1. Test case: `edit 1 f/`<br>
      Expected: The first contact's follow-up is cleared. The result includes `Follow-up: none`, and its card shows `Follow-up: None`. Other contact details remain unchanged.

   1. Test case: Repeat `edit 1 f/`.<br>
      Expected: The result still includes `Follow-up: none`. The contact remains unchanged.

1. Replacing and clearing tags

   1. Prerequisites: Run `list` and ensure at least one contact is shown.

   1. Test case: `edit 1 t/b t/a`<br>
      Expected: The first contact's tags are replaced with `a` and `b`. The result includes `Tags: [a] [b]`, and the follow-up date is unchanged.

   1. Test case: `edit 1 t/`<br>
      Expected: The tags are cleared. The result includes `Tags: none`, and the follow-up date is unchanged. Repeating the command still shows `Tags: none`.

   1. Test case: `edit 1 t/ f/`<br>
      Expected: Both tags and the follow-up are cleared. The result ends with `Tags: none; Follow-up: none`.

   1. Test case: Close and reopen the app.<br>
      Expected: The cleared fields remain empty, and the contact's other details are unchanged.

1. Editing a contact with a past follow-up date

   1. Test case: `edit 1 f/01-01-2020`<br>
      Expected: The date is saved and the result includes a warning that `01-01-2020` is in the past.

1. Invalid follow-up dates when editing

   1. Test case: `edit 1 f/31-02-2026`<br>
      Expected: No contact details change. Error: `31-02-2026 is not a real date.`

   1. Test case: `edit 1 f/2026-12-25`<br>
      Expected: No contact details change. Error: `Follow-up date must be in DD-MM-YYYY format, e.g. 25-12-2026.`

### Completing a follow-up

1. Completing a pending follow-up from a filtered list

   1. Prerequisites: Add a contact named `Followup Test` with a scheduled follow-up, for example:<br>
      `add n/Followup Test p/91234567 e/followuptest@example.com a/21 Clementi Ave 3 st/CLIENT f/25-12-2099 t/referral`<br>
      Run `find Followup` and check that only this contact is shown.

   1. Test case: `done 1`<br>
      Expected: The result shows `Marked follow-up for Followup Test as completed (scheduled for 25-12-2099).`
      The card shows `Follow-up: None`. The contact's name, phone, email, address, status and tags remain unchanged.

   1. Test case: Run `done 1` again.<br>
      Expected: The result shows `Followup Test does not have a pending follow-up.` No contact details change.

   1. Test case: `done 2` while only the test contact is displayed.<br>
      Expected: The result shows `There is no contact 2. The list shows 1 contact.` No contact details change.

   1. Other incorrect commands to try: `done`, `done 0`, `done x`, `done 1 extra`<br>
      Expected: `done 0` and `done x` show `Index must be a positive integer.` Commands such as `done`
      and `done 1 extra` show the invalid-command-format error followed by the full `done` usage:
      a description, index parameter guidance and an example. No contact details change.

1. Saving a completed follow-up

   1. Prerequisites: Complete the test contact's follow-up as described above.

   1. Test case: Close the app, relaunch it, and run `find Followup`.<br>
      Expected: The contact still shows `Follow-up: None`, and all its other details remain unchanged.

### Viewing follow-ups that are due

1. Listing due follow-ups in date order

   1. Prerequisites: Add three contacts with follow-ups: one in the past, one today and one 3 days from today.
      Replace `TODAY` with today's date and `IN_3_DAYS` with the date 3 days from today, both in `DD-MM-YYYY` format.<br>
      `add n/Due Past p/91110001 e/duepast@example.com a/1 Test Road f/01-01-2020`<br>
      `add n/Due Today p/91110002 e/duetoday@example.com a/2 Test Road f/TODAY`<br>
      `add n/Due Soon p/91110003 e/duesoon@example.com a/3 Test Road f/IN_3_DAYS`

   1. Test case: `due`<br>
      Expected: `Due Past` and `Due Today` are shown in that order, plus any other contact due by today. `Due Soon` is not shown.
      The result shows `N follow-ups due by TODAY.`, where `N` is the number of contacts listed. The `Due Past` card shows its date in red with `(overdue)`.

   1. Test case: `due 3`<br>
      Expected: `Due Soon` is now included, after `Due Past` and `Due Today`. The result shows `N follow-ups due by IN_3_DAYS.`

   1. Test case: `due` followed by `done 1`<br>
      Expected: The first contact's follow-up is completed, and that contact leaves the list because it no longer has a follow-up.

   1. Other incorrect commands to try: `due -1`, `due 366`, `due abc`, `due 7 days`<br>
      Expected: No change to the list. Error: `Number of days must be a whole number from 0 to 365, e.g. due 7.`

### Searching by name prefix and phone

Prerequisite: Recreate the [three-contact test fixture](#three-contact-test-fixture).

| Input | Expected result |
|-------|-----------------|
| `find TA` | Only Alice Tan appears at index 1; `1 contact listed.` Matching is case-insensitive and includes the start of a surname. |
| `find an` | Empty list; `0 contacts listed.` A substring inside `Tan` is not a word prefix. |
| `find ta ng` | Alice Tan and Carol Ng appear, in that order; `2 contacts listed.` Either keyword can match. |
| `find p/659123` | Only Alice appears; `1 contact listed.` Stored `+`, spaces and dashes do not interrupt phone-digit matching. |
| `find p/0000` | Empty list; `0 contacts listed.` |
| `find p/9123` | Only Alice appears again. |
| `find`, then `find p/`, then `find p/9123-4567` | Each command reports its missing/invalid search input. Alice remains the only displayed contact after each rejection. Phone search input must contain digits only. |
| `list` | All three contacts reappear with unchanged details. |

### Recording last-contact dates

Prerequisite: Recreate the [three-contact test fixture](#three-contact-test-fixture).

1. Run `find bob`, then `contacted 1 d/01-01-2020`.<br>
   Expected: `Updated last contact date for Bob Lim to 01-01-2020.` Bob's card shows `Last contacted: 01-01-2020`.
   His follow-up remains `01-01-2020`; Alice and Carol are unchanged.
1. Run `contacted 1 d/02-01-2020`.<br>
   Expected: Bob's last-contact date changes to `02-01-2020`, while his follow-up remains `01-01-2020`.
1. Try `contacted 1 d/31-02-2020`, `contacted 1 d/` and `contacted 2 d/01-01-2020` separately.<br>
   Expected: Each reports an error. The last command explains that there is no contact 2 in the one-contact view.
   Bob remains displayed with last-contact date `02-01-2020` and his original follow-up.
1. Work out tomorrow's date in `DD-MM-YYYY` format. Run `contacted 1 d/TOMORROW`, replacing `TOMORROW` with that date.<br>
   Expected: An error says the last-contact date cannot be in the future. Bob's dates remain unchanged.
1. Restart, then run `find bob`.<br>
   Expected: The last-contact date is still `02-01-2020`, and the follow-up is still `01-01-2020`.
   Run `list` to verify that Alice and Carol still have no last-contact date.

### Recording, viewing and removing interaction notes

Prerequisite: Recreate the [three-contact test fixture](#three-contact-test-fixture).

1. Run `find bob`, then `history 1`.<br>
   Expected: `No interaction notes recorded for Bob Lim.` Bob is contact 1 in this filtered view, although he
   was contact 2 in the full list.
1. Run `note 1 nt/Discussed hospital coverage`, then `note 1 nt/Sent policy summary`.<br>
   Expected after each command: `Added interaction note for Bob Lim.`
1. Run `history 1`.<br>
   Expected: The header names Bob Lim. Note **1** is `Sent policy summary`; note **2** is
   `Discussed hospital coverage`. Each has a creation timestamp in `DD-MM-YYYY HH:mm` format. Notes are ordered
   by when they were added, even if both displayed timestamps are the same minute. Record the two timestamps.
1. Try `note 1 nt/`, `note 2 nt/Wrong contact`, `history 2`, `deletenote 1 3` and `deletenote 1 0` separately.<br>
   Expected: Each reports an error. `deletenote 1 3` reports `There is no interaction note 3. The history shows 2 notes.`
   Run `history 1` again: the same two notes, timestamps and indexes remain.
1. Restart, run `find bob`, then `history 1`.<br>
   Expected: Both notes, their timestamps and the newest-added-first order survive the restart.
1. Run `deletenote 1 1`.<br>
   Expected: `Deleted interaction note 1 for Bob Lim: Sent policy summary`.
   Run `history 1`: only `Discussed hospital coverage` remains, now numbered **1** with its original timestamp.
1. Restart, run `find bob`, then `history 1`.<br>
   Expected: The deleted note stays absent, and the remaining note is still numbered **1**.
   Run `list`: all contact details, including both follow-up dates, remain unchanged.
   Run `history 1` and `history 3`: Alice and Carol still have no notes.
1. Run `find bob`, then `deletenote 1 1`, then `history 1`.<br>
   Expected: Deleting the last note succeeds, followed by `No interaction notes recorded for Bob Lim.`
   Restart, run `find bob` and `history 1` again: the history remains empty.

### Clearing contacts with confirmation

Prerequisite: Recreate the [three-contact test fixture](#three-contact-test-fixture).

1. Run `find bob`, then `clear`.<br>
   Expected: `This deletes all 3 contacts. Type 'clear confirm' to proceed.` Bob remains visible; no contact is removed.
1. Run `clear yes`, `clear CONFIRM`, `CLEAR CONFIRM` and `clear confirm now` separately.<br>
   Expected each time: `The clear command accepts no arguments or exactly 'confirm' in lowercase. Type 'clear confirm' to proceed.`
   Bob remains visible. Run `list`: all three contacts are still present.
1. Restart and run `list`.<br>
   Expected: All three contacts remain saved; the unconfirmed request did not delete them.
1. Run `find bob`, then `CLEAR confirm` without running `clear` again.<br>
   Expected: `All contacts have been cleared from Easy-Insurance.` The list is empty.
   Run `list` to confirm that Alice and Carol, which were hidden, were also removed.
1. Restart and run `list`.<br>
   Expected: The list remains empty; sample contacts do not reappear. Run `clear`:
   `This deletes all 0 contacts. Type 'clear confirm' to proceed.` Run `clear confirm`: the list remains empty.

### Recalling commands and restoring a draft

Prerequisite: Recreate the [three-contact test fixture](#three-contact-test-fixture), then restart to empty the
session's command history. Keep the command box focused. Use arrow keys without modifier keys.

1. Press **Up**, then **Down** before submitting anything.<br>
   Expected: The empty command box stays empty.
1. Submit `list`, then `find ta`. Type `edit 1 p/99998888` but **do not press Enter**.<br>
   Expected: Only Alice is displayed, with her original phone number. The typed edit is an unfinished draft.
1. Press **Up** once: `find ta` appears. Press **Up** again: `list` appears. Press **Up** once more: it stays `list`.<br>
   Expected: Only the command text changes. Alice remains the only displayed contact.
1. Press **Down**: `find ta` appears. Press **Down** again: `edit 1 p/99998888` is restored.
   Press **Down** once more: the draft remains unchanged. It has not been executed.
1. Replace the input with `find p/` and press **Enter**.<br>
   Expected: A missing-phone-digits error appears and the failed command remains in the box.
   Clear the box without submitting anything, then press **Up**: `find p/` is recalled, proving that failed
   submissions are stored too. Press **Down**: the empty draft is restored.
1. Restart. Press **Up** in the empty command box.<br>
   Expected: It stays empty because command history is not persisted. Run `find ta`: Alice's phone is still
   `+65 9123-4567`, confirming that recalling the draft never submitted it.

### Navigating Help and copying text

Prerequisite: Use the [disposable test setup](#disposable-test-setup). Open a blank plain-text editor to check
clipboard contents. In this section, **Select All / Copy / Paste** mean **Ctrl+A / Ctrl+C / Ctrl+V** on Windows
and Linux, or **Cmd+A / Cmd+C / Cmd+V** on macOS. Do not submit copied examples as app commands.

1. Run `help`. Keep Help short enough that the guide has a vertical scrollbar.<br>
   Expected: Help opens with the command guide focused. Press **Page Down** to reveal lower rows, then
   **Page Up** to return towards the top. **Down** and **Up** also scroll while the guide is focused.
1. Press **Tab** until the `contacted` example has the text cursor. Press **Select All**, then **Copy**.
   Paste into the plain-text editor.<br>
   Expected: The pasted text is exactly `contacted 2 d/27-10-2026`.
1. Return to that example in Help, select its text and type `x`.<br>
   Expected: The example remains unchanged because it is read-only. Press **Tab** to move to the next example,
   then **Shift+Tab** to return. Arrow keys now move the text cursor rather than scroll the guide.
1. Use **Shift+Tab** to reach the `clear` example. Select all, copy and paste into the editor.<br>
   Expected: Two lines, `clear` and `clear confirm`, are copied. Selecting only one line with the mouse and
   copying it pastes only that command.
1. Return to Help. Press **Tab** until **Copy User Guide URL** has focus, then press **Space**.<br>
   Expected: `User Guide URL copied.` appears. Paste into the editor: the clipboard contains exactly
   `https://ay2627s1-cs2103t-t09-2.github.io/tp/UserGuide.html`.
1. Return to Help and use **Tab** or **Shift+Tab** to focus the guide again; **Page Down** scrolls it.
   Press **Esc**.<br>
   Expected: Help closes, the main app remains open and no contacts have changed.

### Recovering from a failed save

1. Recreate the [three-contact test fixture](#three-contact-test-fixture). Run `find bob`, then
   `note 1 nt/Existing saved note`, then `due`.<br>
   Expected: Only Bob and Alice appear, in that order: Bob is index **1** because his follow-up is earlier.
   Carol is hidden. This checks both filtering and sorting, rather than just an unfiltered list.
1. Leave the app open. In a **second terminal in the test folder**, temporarily replace the data-file path with
   an empty directory. Keep the original file and an additional comparison copy:

   **macOS / Linux:**

   ```bash
   cp data/addressbook.json data/before-save-failure.json
   mv data/addressbook.json data/addressbook.saved.json
   mkdir data/addressbook.json
   ```

   **Windows PowerShell:**

   ```powershell
   Copy-Item data/addressbook.json data/before-save-failure.json
   Move-Item data/addressbook.json data/addressbook.saved.json
   New-Item -ItemType Directory -Path data/addressbook.json
   ```

   Expected: `addressbook.json` is now an empty directory; both saved JSON files remain intact.
   This deliberately blocked path makes saving fail without relying on platform-specific permission settings.
1. In the app, try `done 1`, `note 1 nt/Unwritten note` and `clear confirm` separately.<br>
   Expected after **each** command: A save error identifies the data path as not a regular file, with no success
   message. Bob and Alice remain at indexes 1 and 2; Carol stays hidden. Both original follow-up dates remain.
   Run `history 1`: it still shows only `Existing saved note`, so the failed note was not added.
1. In the **second terminal**, restore the original file and compare it with the copy:

   **macOS / Linux:**

   ```bash
   rmdir data/addressbook.json
   mv data/addressbook.saved.json data/addressbook.json
   cmp data/before-save-failure.json data/addressbook.json
   ```

   **Windows PowerShell:**

   ```powershell
   Remove-Item -LiteralPath data/addressbook.json
   Move-Item data/addressbook.saved.json data/addressbook.json
   $savedHash = (Get-FileHash data/before-save-failure.json).Hash
   $restoredHash = (Get-FileHash data/addressbook.json).Hash
   $savedHash -eq $restoredHash
   ```

   Expected: `cmp` prints nothing and exits successfully, or PowerShell prints `True`. The original saved data
   is unchanged. Restore this path before closing or restarting the app, even if you stop the test early.
1. Retry `done 1` in the app.<br>
   Expected: Bob's follow-up is completed successfully. Only Alice remains in the due view, now at index 1.
   This time the change is saved.
1. Restart and run `due`.<br>
   Expected: Only Alice appears. Run `list`: all three contacts remain, Bob has no follow-up, Alice retains
   `02-01-2020` and Carol still has none. Run `find bob`, then `history 1`: only `Existing saved note` remains.

### Saving data

1. Dealing with a missing data file

   1. Close the app. Delete `data/addressbook.json` from the folder you ran the app from.

   1. Relaunch the app.<br>
      Expected: The app starts with the sample contacts. The data file is created again after the next command that changes contact data, such as `add` or `note`. Commands that only show data, such as `list`, do not create it.

1. Dealing with a corrupted data file

   1. Prerequisites: `data/addressbook.json` exists and holds at least one contact. The previous test leaves no data file, so run `add n/Test Person p/91234567 e/test@example.com a/1 Test Road` first.

   1. Close the app. Open `data/addressbook.json` and change one contact's `"status"` value to `"lead"`.

   1. Relaunch the app.<br>
      Expected: The app starts with an empty contact list, because the file could not be read. A copy of the invalid file is saved as `data/addressbook.json.bak`. The result box says so and gives the reason, starting with the position of the changed contact. Restoring the original value and relaunching brings the contacts back.

   1. Repeat with one contact's `"phone"` changed to 31 or more digits.<br>
      Expected: The same as above. The app still opens.

1. Dealing with a data file containing JSON `null`

   1. Close the app. Replace the entire contents of `data/addressbook.json` with `null`.

   1. Relaunch the app.<br>
      Expected: The app starts with an empty contact list. Both `data/addressbook.json` and its new `.bak` copy contain exactly `null`; sample contacts do not appear.

1. Dealing with a failed backup

   1. Close the app. Put invalid JSON in `data/addressbook.json`. If `data/addressbook.json.bak` exists, move it somewhere safe, then create a non-empty folder with that name.

   1. Relaunch the app.<br>
      Expected: Startup stops with an error naming the data and backup paths. The original `data/addressbook.json` remains unchanged.

   1. Remove the blocking folder and relaunch.<br>
      Expected: The invalid data is backed up and the app starts with an empty contact list.

1. Dealing with an older data file

   1. Prerequisites: `data/addressbook.json` holds at least one valid contact. The previous test leaves invalid data and an empty list, so run `add n/Old Format p/92345678 e/old@example.com a/2 Test Road st/CLIENT f/01-01-2030` first.

   1. Close the app. Remove the `"status"`, `"followUp"`, `"lastContact"` and `"interactionNotes"` fields from one contact in `data/addressbook.json`.

   1. Relaunch the app.<br>
      Expected: The contact loads with status `PROSPECT`, no follow-up, no last-contact date and no interaction notes. Nothing else is lost.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned Enhancements**

Team size: 5

The items below are known flaws in the current version. Each item describes the flaw and the exact change we plan to make in a future version.

1. **Distinguish contacts that share a name and flag likely duplicates under reordered names.**
   Uniqueness currently uses only the name, ignoring case and extra spaces. Two different people named `John Tan`
   cannot both be stored, while `Tan John` can be added as a second entry for the same person. We plan to give every
   contact a stable ID, allow the same name on different IDs, and show a confirmation when a new contact shares a
   phone number or email with an existing contact. The agent can then keep two people who share contact details or
   cancel a likely duplicate without losing either contact.

1. **Stop a second window or a hand edit of the data file from being overwritten.**
   Easy-Insurance reads `data/addressbook.json` once at startup. After that, every command that changes contact data writes the app's own copy over the file. If two windows are open on the same `data` folder, or the user edits the file while the app is running, the last save wins and the other changes are lost with no warning. For example, window 1 runs `add n/Bob Lim p/92345678 e/bob@example.com a/2 Road` and then window 2 runs `add n/Carol Ng p/93456789 e/carol@example.com a/3 Road`. Both show a success message, but after a restart Bob Lim is gone.<br>
   We plan to make the app remember a hash of the data file's contents when it loads the file and after each of its own successful saves. Before each save, it hashes the file again. If the contents have changed, the app does not save, rolls back the change in this window as it does for other save errors, and shows: `The data file was changed outside this window, so this change was not saved. Restart Easy-Insurance to load the latest data.` In the example above, window 2's `add` then shows this message and Bob Lim is kept.

1. **Reject prefixes typed in capitals instead of saving them into the previous field.**
   Prefixes are case-sensitive, so a capitalised prefix after another field is kept as part of that field's value. For example, `add n/Ann Lee p/91234567 e/ann@example.com a/1 Road ST/CLIENT F/01-01-2030` gives a result starting with `New contact added: Ann Lee (PROSPECT)`, followed by its displayed index. The address is saved as `1 Road ST/CLIENT F/01-01-2030`, and the status and follow-up are not set. `edit 1 n/Bob ST/CLIENT` renames the contact to `Bob ST/CLIENT`.<br>
   We plan to make `add` and `edit` check each value for a known prefix (`n/`, `p/`, `e/`, `a/`, `st/`, `t/` or `f/`) typed with one or more capital letters after a space. If one is found, the command changes nothing and shows: `Prefixes must be typed in lowercase. Use st/ instead of ST/.` `A/L` and `A/P` inside a name stay allowed, so `add n/Ravi A/L Kumar p/92345679 e/ravi@example.com a/10 Tampines St 1` still works.

1. **Keep `a/l` and `a/p` in a name as part of the name.**
   A lowercase `a/` always starts the address, even inside a name. For example, `edit 2 n/Ravi a/l Kumar` gives `Edited contact: Ravi; ... Address: l Kumar; ...`, so the name loses `a/l Kumar` and the address is overwritten. `add n/Ravi a/l Kumar p/92345678 e/ravi@example.com a/10 Tampines St 1` fails with `Multiple values specified for the following single-valued field(s): a/`. The UG lists this as a known issue.<br>
   We plan to treat `a/` as part of the name when it comes after `n/` with no other prefix in between, and is followed by a single `l` or `p` (in either case) and then a space. The name is saved with `A/L` or `A/P` in capitals. `edit 2 n/Ravi a/l Kumar` then gives `Edited contact: Ravi A/L Kumar; ...` and leaves the address unchanged. An `a/` anywhere else still starts the address, so `edit 2 a/Lorong 1 Geylang` still sets the address.

1. **Point out a missing space after the index in `edit`.**
   `edit 4st/CLIENT` gives `Index must be a positive integer.`, although 4 is a positive integer. The real problem is the missing space before `st/`.<br>
   We plan to check whether the text in front of the first prefix is a positive integer followed directly by a known prefix. If it is, the command changes nothing and shows: `Add a space between the index and st/, e.g. edit 4 st/CLIENT.` Other invalid indexes, such as `edit abc st/CLIENT`, keep the current message.

1. **Treat an index above 2147483647 as an index outside the list.**
   An index is read as a Java `int`, so a larger number is rejected as if it were not a number at all. `delete 2147483647` gives `There is no contact 2147483647. The list shows 8 contacts.`, but `delete 99999999999` and `done 2147483648` give `Index must be a positive integer.`, which is not true.<br>
   We plan to give a positive whole number that is too large to store the same message as any other index outside the list. `delete 99999999999` then gives `There is no contact 99999999999. The list shows 8 contacts.` This applies to every command that takes an index: `delete`, `edit`, `done`, `contacted`, `note`, `history` and both indexes of `deletenote`.

1. **Accept only the digits 0 to 9 in an index.**
   Java's number parser also accepts digits from other scripts, so they work as indexes. `delete ١` (the Arabic-Indic digit one) gives `Deleted contact: John Tan`, and `edit ２ n/Bob Wide` (a full-width 2) renames the second contact. Other numbers are stricter: `due ７` is rejected with `Number of days must be a whole number from 0 to 365, e.g. due 7.`<br>
   We plan to accept an index only if every character in it is one of the digits 0 to 9. `delete ١` then gives `Index must be a positive integer.` and deletes nothing.

1. **Say that `find` cannot search by name and phone at the same time.**
   `find alice p/9123` and `find p/9123 alice` give `Phone search keywords must contain digits only. Example: find p/9123`, although `9123` is all digits. The real problem is that a name keyword and a phone search are mixed.<br>
   We plan to give this case its own message when there is a name keyword before `p/` or a word after the phone digits: `Search by name or by phone, not both. Use find KEYWORD [MORE_KEYWORDS]... or find p/PHONE_DIGITS.` A phone search with a non-digit value, such as `find p/9a23`, keeps the current message.

1. **Make email and address optional when adding a contact.**
   `add` requires `e/` and `a/`, so a prospect known only by name and phone cannot be added. For example, `add n/Jane Doe p/98765432` gives `Missing required field(s): e/EMAIL, a/ADDRESS`. The only workaround is a made-up email and address, which then appear on the contact card and in the data file as if they were real.<br>
   We plan to make `e/EMAIL` and `a/ADDRESS` optional in `add`, like `st/`, `f/` and `t/`. A contact added without them has no email or address: the contact card leaves out those lines and the data file stores no value for them. `add n/Jane Doe p/98765432` then gives `New contact added: Jane Doe (PROSPECT)`. Name and phone stay required, and a given `e/` or `a/` value must still be valid, so `add n/Jane Doe p/98765432 e/jane` still shows the email format error.
