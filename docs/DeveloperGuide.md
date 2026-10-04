---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

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

**Value proposition**: Keep track of prospects and clients, their status, and when to follow up next, faster than juggling phone contacts, chat apps and spreadsheets.

Easy-Insurance focuses on managing prospects and clients. It does not send messages or make calls, generate quotations or premiums, recommend policies, manage claims, track commissions, or replace the agency's official CRM. It is a single-user app, so nothing is shared with other agents.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                  | I want to …                                            | So that I can…                                                       |
|----------|------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------|
| `* * *`  | new insurance agent                      | add a new prospect                                      | keep track of people I may want to follow up with                    |
| `* * *`  | insurance agent                          | view my list of prospects and clients                   | see the people I am currently managing                               |
| `* * *`  | insurance agent looking for a person     | search for a contact by name                            | quickly retrieve the person's information                            |
| `* * *`  | insurance agent                          | edit a contact's details                                | keep their information up to date when it changes                    |
| `* * *`  | insurance agent                          | delete a contact                                        | remove people who no longer want to hear from me                     |
| `* * *`  | insurance agent                          | set a contact's status to prospect, client or inactive | tell at a glance who is still worth chasing                          |
| `* * *`  | insurance agent                          | set a follow-up date for a contact                      | remember who I promised to call back                                 |
| `* * *`  | insurance agent                          | mark a follow-up as done                                | keep my list to what is still pending                                |
| `* * *`  | insurance agent unfamiliar with CLI      | see a guide to the available commands and their formats | learn to use the app without prior CLI experience                    |
| `* *`    | insurance agent                          | record when I last contacted someone                    | see who I have not spoken to in a while                              |
| `* *`    | insurance agent planning my day          | see all follow-ups due today                            | know who to call before I start                                      |
| `* *`    | insurance agent unfamiliar with CLI      | get a clear error message when I type a command wrongly | learn from the error and type it correctly next time                 |
| `* *`    | insurance agent                          | filter my list by status                                | focus on prospects when planning outreach                            |
| `* *`    | insurance agent with many contacts       | search by partial name or phone number                  | find someone when I only remember part of it                         |
| `* *`    | insurance agent                          | see overdue follow-ups                                  | catch the people I missed                                            |
| `* *`    | insurance agent                          | see follow-ups due this week                            | plan ahead when today is already full                                |
| `* *`    | insurance agent                          | push a follow-up to a later date                        | reschedule when a prospect asks me to call next week                 |
| `* *`    | insurance agent                          | record the policies a client has bought                 | avoid pitching what they already have                                |
| `* *`    | insurance agent                          | see renewals due in the next month                      | plan retention calls early                                           |
| `* *`    | insurance agent                          | sort contacts by how many times I met them              | identify prospects who are more engaged with me                      |
| `* *`    | insurance agent                          | be warned when I add a duplicate                        | keep my list clean                                                   |
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

**Use case: Add a prospect and plan a follow-up**

**MSS**

1. Agent provides the new prospect's name and contact details.
2. System validates the details and saves the contact with the prospect status.
3. System shows the new prospect in the contact list.
4. Agent sets a follow-up date for the prospect.
5. System saves the pending follow-up and shows its date with the prospect.

   Use case ends.

**Extensions**

* 2a. The contact details are invalid or incomplete.
  * 2a1. System explains which details need correction.
  * 2a2. Agent corrects the details.
  * Use case resumes at step 2.
* 2b. A contact with the same name already exists (ignoring letter case and extra spaces).
  * 2b1. System informs the agent that the contact already exists and does not add it.
  * Use case ends.
* 4a. The follow-up date is invalid.
  * 4a1. System shows an error and does not change the saved contact.
  * Use case resumes at step 4.

**Use case: Find contacts by name**

**MSS**

1. Agent requests to find contacts using one or more name keywords.
2. System shows the contacts whose names contain any of the keywords, and how many were found.

   Use case ends.

**Extensions**

* 1a. Agent gives no keyword.
  * 1a1. System shows an error message.
  * Use case resumes at step 1.
* 2a. No contact matches the keywords.
  * 2a1. System shows an empty list.
  * Use case ends.

**Use case: Edit a contact**

**MSS**

1. Agent requests to list contacts.
2. System shows the list of contacts.
3. Agent requests to change some details of a specific contact in the list.
4. System updates the contact and shows the updated details.

   Use case ends.

**Extensions**

* 2a. The list is empty.
  * Use case ends.
* 3a. The given contact does not exist in the list.
  * 3a1. System shows an error message.
  * Use case resumes at step 2.
* 3b. No details to change are given, or a new detail is invalid.
  * 3b1. System explains what needs correction and does not change the contact.
  * Use case resumes at step 2.
* 3c. The change would make the contact a duplicate of another contact.
  * 3c1. System informs the agent that the contact already exists and does not change the contact.
  * Use case resumes at step 2.

**Use case: Delete a contact**

**MSS**

1. Agent requests to list contacts.
2. System shows the list of contacts.
3. Agent requests to delete a specific contact in the list.
4. System deletes the contact.

   Use case ends.

**Extensions**

* 2a. The list is empty.
  * Use case ends.
* 3a. The given contact does not exist in the list.
  * 3a1. System shows an error message.
  * Use case resumes at step 2.

**Use case: Complete a follow-up**

**MSS**

1. Agent views a contact with a pending follow-up.
2. System shows the scheduled follow-up date.
3. Agent requests to mark that follow-up as done.
4. System records the follow-up as completed and shows the updated contact.

   Use case ends.

**Extensions**

* 3a. The contact has no pending follow-up.
  * 3a1. System explains that there is no follow-up to complete.
  * Use case ends without changing the contact.

### Non-Functional Requirements

1. Easy-Insurance should run on Windows, macOS, and Linux with Java `25` or above installed.
2. With up to 1000 contacts, listing and finding contacts should complete within two seconds on a typical laptop.
3. An agent should be able to add, find, edit, and view contacts and manage follow-ups using the keyboard alone.
4. The app should work without an internet connection and should not transmit contact details to a network service.
5. Contact data should be stored locally in a human-editable text file, so that advanced users can back it up or edit it directly.
6. The app should be packaged as a single JAR file that runs without an installer.

### Glossary

* **Contact**: A person recorded in Easy-Insurance, together with their contact details, status, and any follow-up.
* **Status**: Where a contact is in the agent's sales process: prospect, client, or inactive.
* **Prospect**: A contact the agent may sell a policy to but who is not yet a client.
* **Client**: A contact who has bought a policy from the agent.
* **Inactive**: A contact the agent is no longer actively pursuing but keeps in the app.
* **Follow-up**: A planned future contact with a prospect or client, recorded with a scheduled date.
* **Pending follow-up**: A follow-up that has not been marked as done.
* **Due follow-up**: A pending follow-up scheduled for today.
* **Overdue follow-up**: A pending follow-up whose scheduled date has passed.
* **Name keyword**: A non-empty, complete word used by `find` to match contact names, ignoring letter case. Partial-name matching is outside the initial version.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
