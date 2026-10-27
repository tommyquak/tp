---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# Easy-Insurance User Guide

Easy-Insurance is a **desktop app for junior insurance agents to keep track of their prospects and clients**. It records each contact's details, their status (prospect, client or inactive), when you should next follow up with them, and notes from past client interactions. It is optimized for use through a Command Line Interface (CLI) while keeping a Graphical User Interface (GUI), so if you type quickly, you can manage your contacts faster than with a mouse-driven app.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T09-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for Easy-Insurance.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar EasyInsurance.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to Easy-Insurance.

   * `delete 3` : Deletes the 3rd contact shown in the current list.
  
   * `note 2 nt/Discussed hospital coverage with client` : Adds an interaction note to the 2nd displayed contact.

   * `history 2` : Shows the interaction history of the 2nd displayed contact.

   * `clear confirm` : Deletes all contacts after confirmation.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* `help` and `exit` accept no parameters.<br>
  Extra parameters are rejected with an error naming the command.<br>
  For example, `clear 123` shows `The clear command does not accept additional parameters.`
  and leaves all contacts unchanged.

* `clear` accepts either no arguments or exactly `confirm`.
  Other parameters are rejected with an error naming the command.
  
* `list` accepts optional `st/STATUS` and `t/TAG` filters.
  Each filter may appear at most once.

* Command words are case-insensitive.
  For example, `list`, `LIST`, and `LiSt` are treated as the same command.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens the Easy-Insurance Command Guide, which shows the available commands together with their formats and examples.

The help window also provides a **Copy User Guide URL** button for copying the full User Guide link.

![help message](images/helpMessage.png)

Format: `help`

### Adding a contact: `add`

Adds a contact to Easy-Insurance.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [st/STATUS] [f/FOLLOW_UP_DATE] [t/TAG]...`

<box type="tip" seamless>

**Status:** `st/STATUS` is optional and case-insensitive. `PROSPECT` means a potential client, `CLIENT` means a policyholder, and `INACTIVE` means a contact no longer being pursued. The default is `PROSPECT`.
The contact list shows blue, green, and grey badges respectively for these statuses.
</box>

<box type="tip" seamless>

**Follow-up date:** `f/FOLLOW_UP_DATE` is optional. Use the `DD-MM-YYYY` format, e.g. `f/25-12-2026`. The date must be a real calendar date (`31-02-2026` is rejected). A date in the past is accepted, so you can record a follow-up you already did, but Easy-Insurance shows a note reminding you that it is in the past. The follow-up date appears on the contact's card.
</box>

<box type="tip" seamless>

**Tags:** A contact can have any number of tags, including zero.
</box>

Names with extra spaces are saved with single spaces. A contact cannot be added if its name matches an existing contact's name after ignoring case and extra spaces.
Names must start with a letter, including accented letters. They may then contain letters, digits, spaces,
hyphens (`-`), apostrophes (`'`), periods (`.`), commas (`,`), and slashes (`/`), so names such as
`Anne-Marie`, `O'Connor`, and `Raj s/o Kumar` are accepted. Phone numbers must contain at least three
digits. They may start with `+` and include spaces or dashes between digits, for example `+65 9123-4567`.
Phone numbers are saved exactly as entered. If a name is already in the contact list, Easy-Insurance
shows that contact's name and phone number.

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Sarah Lim p/98765432 e/sarahlim@example.com a/10 Tampines St 11 st/PROSPECT t/referral`
* `add n/Ravi Kumar p/92345678 e/ravi@example.com a/10 Tampines Street 1 st/CLIENT f/25-12-2026`

### Listing contacts: `list`

Shows all contacts, or filters contacts by status, tag, or both.

Format: `list [st/STATUS] [t/TAG]`

Known issue: names containing `a/l` are interpreted as an address prefix by the command parser.

* Plain `list` removes the current filter and shows all contacts.
* `st/STATUS` accepts `PROSPECT`, `CLIENT`, or `INACTIVE`, ignoring case.
* `t/TAG` matches a complete tag name, ignoring case.
  For example, `t/REFERRAL` matches the tag `referral`, but `t/ref` does not.
* When both filters are supplied, a contact must match both.
* Filters may appear in either order.
* Each filter may appear at most once, and its value cannot be blank.
* Invalid filter values and unrelated arguments are rejected.
* A filtered result reports the number of matching contacts.
  If no contacts match, the displayed list is empty.
* Each new `list` command replaces the previous filter.

Examples:

* `list` shows all contacts and reports `Listed all contacts.`
* `list st/CLIENT` shows only clients.
* `list t/referral` shows contacts tagged `referral`, regardless of the tag's case.
* `list st/CLIENT t/referral` shows clients with the referral tag.
* `list t/REFERRAL st/client` applies the same combined filter.

### Editing a contact: `edit`

Edits an existing contact in Easy-Insurance.

Format: `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [st/STATUS] [f/FOLLOW_UP_DATE] [t/TAG]...`

* Edits the contact at the specified `INDEX`. The index refers to the index number shown in the displayed contact list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* `st/STATUS` changes the contact's status. Status is case-insensitive.
* `f/FOLLOW_UP_DATE` changes the follow-up date. The date must be real and in `DD-MM-YYYY` format.
* Past follow-up dates are accepted with a warning.
* To clear an existing follow-up, enter `f/` without a date after it.
* When editing tags, all of the contact's existing tags are removed; adding tags is not cumulative.
* To remove all of a contact's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st contact to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd contact to be `Betsy Crower` and clears all existing tags.
*  `edit 1 st/CLIENT f/25-12-2026` Changes the 1st contact's status and follow-up date.
*  `edit 1 f/` Clears the 1st contact's follow-up date.

### Finding contacts by name or phone: `find`

Finds contacts whose names start with any of the given keywords, or whose phone numbers contain the given digits.

Format: `find KEYWORD [MORE_KEYWORDS]` or `find p/PHONE_DIGITS`

* Name searches are case-insensitive and match the start of any word; for example, `han` matches `Hans`.
* Any keyword may match; for example, `find Hans Bo` returns contacts whose names contain either `Hans` or `Bo`.
* Phone searches match contacts whose phone number contains the supplied digits; for example, `find p/9123`.
* If no keyword is provided, Easy-Insurance shows: `Please enter at least one name keyword to search for. Example: find alice tan`.
* If no phone digits are provided, Easy-Insurance shows: `Please enter at least one phone digit to search for. Example: find p/9123`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns contacts matching either `alex` or `david`.

### Deleting a contact: `delete`

Deletes the specified contact from Easy-Insurance.

Format: `delete INDEX`

* Deletes the contact at the specified `INDEX`.
* The index refers to the index number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd contact in Easy-Insurance.
* `find Priya` followed by `delete 1` deletes the 1st contact in the results of the `find` command.

The result shows the deleted contact's name, for example `Deleted contact: Priya Nair`.

### Completing a follow-up: `done`

Marks a contact's pending follow-up as completed by removing its scheduled follow-up date.

Format: `done INDEX`

* The index refers to the number shown in the displayed contact list.
* The index must be a positive integer and refer to a displayed contact.
* The contact must have a pending follow-up.
* The contact's other details remain unchanged. Its card shows `Follow-up: None` after completion.

On success, the result is:
`Marked follow-up for <name> as completed.`

If the contact has no pending follow-up, the result is:
`<name> does not have a pending follow-up.`

For an index beyond the displayed list, the result is:
`There is no contact INDEX. The list shows N contacts.`

Examples:

* `list` followed by `done 2` completes the second displayed contact's follow-up, if it has one.
* `find John` followed by `done 1` completes the first matching contact's follow-up, if it has one.

### Adding an interaction note: `note`

Adds a note about an interaction with an existing contact.

Format: `note INDEX nt/NOTE`

* The index refers to the number shown in the displayed contact list.
* The index must be a positive integer and refer to a displayed contact.
* `nt/NOTE` is required and cannot be blank.
* The note may contain spaces.
* New notes are appended to the contact's existing interaction history. Existing notes are not replaced.
* Interaction notes remain attached to the contact when other contact details are edited or when a follow-up is marked as completed.

On success, the result is:

`Added interaction note for <name>.`

Examples:

* `note 1 nt/Called client to discuss renewal options`
* `note 2 nt/Discussed hospital coverage and requested updated quotation`

### Viewing interaction history: `history`

Shows all interaction notes recorded for an existing contact.

Format: `history INDEX`

* The index refers to the number shown in the displayed contact list.
* The index must be a positive integer and refer to a displayed contact.
* Notes are shown in the order they were recorded, with the oldest note first.
* `history` does not modify the contact or its notes.

If notes have been recorded, the result is shown in this format:

```text
Interaction history for John Tan:
1. Called client
2. Discussed renewal options
```

If the contact has no interaction notes, the result is:

`No interaction notes recorded for <name>.`

Examples:

* `history 1`
* `find John` followed by `history 1` shows the interaction history of the first displayed matching contact.

### Viewing follow-ups that are due: `due`

Format: `due [DAYS]`

* `due` shows follow-ups due today, plus any overdue ones.
* `due DAYS` also includes follow-ups in the next `DAYS` days. For example, on 21-10-2026, `due 7` shows everything up to and including 28-10-2026.
* `DAYS` must be a whole number from 0 to 365.
* Contacts without a follow-up date are not shown.
* The list is sorted by follow-up date, earliest first. Contacts with the same date are sorted by name.
* The result reports how many follow-ups are due, for example `3 follow-ups due by 28-10-2026.`
  If none are due, the result is `No follow-ups due by 28-10-2026.` and the displayed list is empty.
* The index numbers follow this sorted list, so `done 1` after `due` completes the earliest follow-up.
* `list` shows all contacts again in their usual order.

On the contact cards, a follow-up date that has already passed is shown in red with `(overdue)` after it.

Examples:

* `due` shows today's and overdue follow-ups.
* `due 7` shows follow-ups due within the next week, including overdue ones.

### Clearing all contacts: `clear`

Requests confirmation before removing all contacts from Easy-Insurance.

Format: `clear [confirm]`

* `clear` does not remove any contacts immediately.
* It shows how many contacts would be deleted and asks you to type `clear confirm`.
* `clear confirm` removes all contacts from Easy-Insurance.
* Any other additional parameter is rejected.

Examples:

* `clear` shows `This deletes all N contacts. Type 'clear confirm' to proceed.`
* `clear confirm` clears all contacts.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Easy-Insurance automatically saves contact details, follow-up information and interaction notes after every successful command. You do not need to save manually.

### Editing the data file

Easy-Insurance data is saved automatically as a JSON file `data/addressbook.json`, inside the folder you run the app from. If you follow the Quick start, that is your home folder. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, Easy-Insurance copies it to `addressbook.json.bak` in the same `data` folder and then starts with an empty contact list. The invalid `addressbook.json` is overwritten after your next successful command, but the `.bak` copy is kept. To recover, close the app, fix the `.bak` file and rename it back to `addressbook.json`. A later backup replaces the previous `.bak` file, so we still recommend keeping your own copy before editing.<br>
If Easy-Insurance cannot create the `.bak` file (for example, because the `data` folder is read-only), it shows an error naming both files and closes without changing `addressbook.json`. Make the `data` folder writable, then start the app again.<br>
Two contacts whose names differ only in case or extra spaces also make the data file invalid and cause it to load as an empty contact list.<br>
Furthermore, certain edits can cause Easy-Insurance to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous Easy-Insurance home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [st/STATUS] [f/FOLLOW_UP_DATE] [t/TAG]...` <br> e.g., `add n/Sarah Lim p/98765432 e/sarahlim@example.com a/10 Tampines St 11 st/PROSPECT f/25-12-2026 t/referral`
**Clear**  | `clear` to request confirmation<br>`clear confirm` to clear all contacts
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Done**   | `done INDEX`<br> e.g., `done 1`
**Due**    | `due [DAYS]`<br> e.g., `due 7`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [st/STATUS] [f/FOLLOW_UP_DATE] [t/TAG]...`<br> e.g., `edit 2 n/James Lee st/CLIENT f/25-12-2026`
**Find**   | `find KEYWORD [MORE_KEYWORDS]` or `find p/PHONE_DIGITS`<br> e.g., `find James Jake` or `find p/9123`
**List**   | `list [st/STATUS] [t/TAG]`<br> e.g., `list st/CLIENT t/referral`
**History**| `history INDEX`<br> e.g., `history 2`
**Note**   | `note INDEX nt/NOTE`<br> e.g., `note 2 nt/Discussed hospital coverage`
**Help**   | `help`
**Exit**   | `exit`
