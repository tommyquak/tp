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

The sequence diagram below illustrates how the `find` command filters contacts by name keywords.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `find` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `CommandInputParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a contact).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `CommandInputParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `CommandInputParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />

`Status.DEFAULT` is `PROSPECT`. Each `Person` stores one status and optionally one follow-up date.
`Name` accepts Unicode letters and common name punctuation after an initial letter; `Phone` accepts
an optional leading plus sign and separators between at least three digits.


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

### Completing a follow-up

The `done INDEX` command clears the pending follow-up of the contact at the specified index in the currently
displayed list. It preserves the contact's name, phone, email, address, status and tags.

The sequence diagram below shows a successful `done 1` command when the first displayed contact has a pending
follow-up. Individual field getters and the internal calls within `Model` and `Storage` are omitted for clarity.

<puml src="diagrams/DoneSequenceDiagram.puml" alt="Completing a pending follow-up" />

1. `LogicManager` passes the command text to `CommandInputParser`, which creates a `DoneCommandParser`.
2. `DoneCommandParser` validates the argument using `ParserUtil.parseIndex` and creates a `DoneCommand` with
   the resulting index. The command is returned to `LogicManager` for execution.
3. `DoneCommand` obtains the currently displayed contacts from `Model`, checks the index and selects the
   contact. It then checks that the selected contact has a pending follow-up.
4. The command constructs a replacement `Person`, copying the other contact details and setting the
   follow-up to `Optional.empty()`. It calls `Model.setPerson` to replace the original contact in memory.
5. The command returns a `CommandResult` containing `Marked follow-up for <name> as completed.`.
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

If saving fails with an `IOException`, `LogicManager` throws a `CommandException`.
The follow-up has already been cleared in memory and is not rolled back.
The change may not have been saved to disk.

### Viewing due follow-ups

`due [DAYS]` shows contacts whose follow-up date is on or before today plus `DAYS`, earliest first.

* `DueCommandParser` accepts no argument (0 days) or a whole number from 0 to 365, so the cutoff date can never overflow.
* `DueCommand` works out the cutoff from a `Clock`. The app passes the system clock, and tests pass a fixed one, so the tests do not depend on the real date.
* `FollowUpDueByPredicate` keeps contacts that have a follow-up on or before the cutoff. Contacts without a follow-up are left out.
* `Model#updateFilteredPersonList(Predicate, Comparator)` filters and sorts the displayed list in one step. `ModelManager` wraps its `FilteredList` in a `SortedList`, and `getFilteredPersonList()` returns the sorted view, so index-based commands such as `done 1` act on the order the user sees.
* The one-argument `updateFilteredPersonList(Predicate)` clears the comparator, so `list`, `find` and every command that resets the list go back to the stored order.

The sequence diagram below shows how `due 7` is executed.

<puml src="diagrams/DueSequenceDiagram.puml" alt="Interactions inside the Logic and Model components for the `due 7` command" />

<box type="info" seamless>

**Note:** The lifeline for `DueCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

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

**Use case: Add a prospect and plan a follow-up**

**MSS**

1. Agent provides the new prospect's name and contact details.
2. System validates the details and saves the contact with status `PROSPECT`.
3. System shows the new prospect in the contact list.
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

1. Agent enters `find KEYWORD [MORE_KEYWORDS]` or `find p/PHONE_DIGITS`.
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
* 3a. No contact matches the name keywords or phone digits.
  * 3a1. System shows an empty result list and reports `0 contacts listed.`
  * Use case ends.

**Use case: Edit a contact**

**MSS**

1. Agent views the contact to edit.
2. Agent provides one or more replacement details, including an optional status or follow-up date.
3. System validates the replacement details.
4. System updates the contact and shows its updated details.

   Use case ends.

**Extensions**

* 2a. Agent provides an empty follow-up date.
  * 2a1. System clears the contact's follow-up date.
  * Use case resumes at step 4.
* 3a. A replacement detail is invalid.
  * 3a1. System shows the relevant validation error and does not change the contact.
  * Use case ends.
* 3b. The follow-up date is in the past.
  * 3b1. System saves the date and warns the agent that it is in the past.
  * Use case resumes at step 4.
* 4a. The selected contact is not in the displayed list.
  * 4a1. System explains that the contact reference is invalid.
  * Use case ends without changing any contacts.

**Use case: Complete a follow-up**

**MSS**

1. Agent views a contact with a pending follow-up.
2. System shows the scheduled follow-up date.
3. Agent requests to mark that follow-up as done.
4. System clears the follow-up and shows the updated contact.

   Use case ends.

**Extensions**

* 3a. The contact has no pending follow-up.
  * 3a1. System explains that there is no follow-up to complete.
  * Use case ends without changing the contact.
* 3b. The selected contact is not in the displayed list.
  * 3b1. System explains that the contact reference is invalid.
  * Use case ends without changing any contacts.

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

### Non-Functional Requirements

1. Easy-Insurance should run on Windows, macOS, and Linux with Java `25` or above installed.
2. With up to 1000 contacts, listing and finding contacts should complete within two seconds on a typical laptop.
3. An agent should be able to add, find, edit, and view contacts and manage follow-ups using the keyboard alone.
4. The app should work without an internet connection and should not transmit contact details to a network service.
5. An agent who knows the commands should be able to complete common contact tasks faster by typing than by using the mouse.
6. Contact data should be stored locally as human-editable JSON.
7. The app should be distributed as a single JAR of at most 100 MB, with no installer required.

### Glossary

* **Contact**: A person recorded in Easy-Insurance, together with their contact details, status, and any follow-up.
* **Status**: Where a contact is in the agent's pipeline: `PROSPECT`, `CLIENT` or `INACTIVE`. New contacts are prospects unless told otherwise.
* **Prospect**: A contact the agent may sell a policy to but who is not yet a client.
* **Client**: A contact who has bought a policy from the agent.
* **Inactive**: A contact the agent is no longer actively pursuing but keeps in the app.
* **Follow-up**: The date on which the agent intends to next contact a prospect or client. It may be in the past, for example when recording a follow-up that already happened.
* **Pending follow-up**: A follow-up date that is still recorded on a contact. Marking it done removes the date.
* **Duplicate contact**: A contact whose name matches an existing contact's name after ignoring letter case and extra spaces. Easy-Insurance refuses to add a duplicate.
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
      The result is `Listed 2 contacts with tag referral.`

   1. Test case: `list t/ref`<br>
      Expected: No contacts are shown.
      The result is `Listed 0 contacts with tag ref.`

1. Combining filters

   1. Test case: `list st/client t/referral`<br>
      Expected: Only Filter Client Referral is shown.
      The result is `Listed 1 contact with status CLIENT and tag referral.`

   1. Test case: `list t/REFERRAL st/CLIENT`<br>
      Expected: The same contact and result message are shown.

1. Resetting the filter

   1. Test case: `list`<br>
      Expected: All four contacts are shown.
      The result is `Listed all contacts.`

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

   1. Test case: `add n/Sarah Lim p/92345678 e/sarah@example.com a/10 Tampines Street 1 f/25-12-2099`<br>
      Expected: The contact is added and the card shows `Follow-up: 25-12-2099`. No note is shown.

1. Adding a contact with a past follow-up date

   1. Test case: `add n/Ben Ong p/93456789 e/ben@example.com a/5 Bishan Road f/01-01-2020`<br>
      Expected: The contact is added. The result message ends with `Note: 01-01-2020 is in the past; it was saved anyway.`

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
      Expected: The first contact's follow-up is cleared and its card shows `Follow-up: None`.

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
      Expected: The result shows `Marked follow-up for Followup Test as completed.`
      The card shows `Follow-up: None`. The contact's name, phone, email, address, status and tags remain unchanged.

   1. Test case: Run `done 1` again.<br>
      Expected: The result shows `Followup Test does not have a pending follow-up.` No contact details change.

   1. Test case: `done 2` while only the test contact is displayed.<br>
      Expected: The result shows `There is no contact 2. The list shows 1 contact.` No contact details change.

   1. Other incorrect commands to try: `done`, `done 0`, `done x`, `done 1 extra`<br>
      Expected: `done 0` and `done x` show `Index must be a positive integer.` Other malformed forms show the
      invalid-command-format error with `done INDEX`. No contact details change.

1. Saving a completed follow-up

   1. Prerequisites: Complete the test contact's follow-up as described above.

   1. Test case: Close the app, relaunch it, and run `find Followup`.<br>
      Expected: The contact still shows `Follow-up: None`, and all its other details remain unchanged.

### Viewing follow-ups that are due

1. Listing due follow-ups in date order

   1. Prerequisites: Add three contacts with follow-ups, one overdue, one today and one in 3 days. For example, if today is 21-10-2026:<br>
      `add n/Due Past p/91110001 e/duepast@example.com a/1 Test Road f/01-10-2026`<br>
      `add n/Due Today p/91110002 e/duetoday@example.com a/2 Test Road f/21-10-2026`<br>
      `add n/Due Soon p/91110003 e/duesoon@example.com a/3 Test Road f/24-10-2026`

   1. Test case: `due`<br>
      Expected: `Due Past` then `Due Today` are shown, plus any other contact due by today. `Due Soon` is not shown.
      The result shows `N follow-ups due by 21-10-2026.` The `Due Past` card shows its date in red with `(overdue)`.

   1. Test case: `due 3`<br>
      Expected: `Due Soon` is now included, after the other two.

   1. Test case: `due` followed by `done 1`<br>
      Expected: The earliest follow-up in the sorted list is completed.

   1. Other incorrect commands to try: `due -1`, `due 366`, `due abc`, `due 7 days`<br>
      Expected: No change to the list. Error: `Number of days must be a whole number from 0 to 365, e.g. due 7`

### Saving data

1. Dealing with a missing data file

   1. Close the app. Delete `data/addressbook.json` from the folder you ran the app from.

   1. Relaunch the app.<br>
      Expected: The app starts with the sample contacts. The data file is created again after the next command that runs without an error, such as `list`.

1. Dealing with a corrupted data file

   1. Close the app. Open `data/addressbook.json` and change one contact's `"status"` value to `"lead"`.

   1. Relaunch the app.<br>
      Expected: The app starts with an empty contact list, because the file could not be read. A copy of the invalid file is saved as `data/addressbook.json.bak`. Restoring the original value and relaunching brings the contacts back.

   1. Close the app. Make sure `data/addressbook.json` still has `"status": "lead"`. Delete `data/addressbook.json.bak`, then create a folder with that same name in `data` and put any file inside it.

   1. Relaunch the app.<br>
      Expected: An error says the data file could not be backed up and shows the full paths of both files. The app closes after you dismiss the error, and `data/addressbook.json` is unchanged.

   1. Delete the `data/addressbook.json.bak` folder and relaunch the app.<br>
      Expected: The app starts with an empty contact list and a new `data/addressbook.json.bak` is saved, as above.

1. Dealing with an older data file

   1. Close the app. Remove the `"status"` and `"followUp"` fields from one contact in `data/addressbook.json`.

   1. Relaunch the app.<br>
      Expected: The contact loads with status `PROSPECT` and no follow-up. Nothing else is lost.
