# Gitlet Design Document

**Name**: Samuel Stahl

## Classes and Data Structures

### Command

An abstract representation of all Gitlet commands, extended by
AddCommand, BranchCommand, CheckoutCommand, FindCommand,
GlobalLogCommand, InitCommand, LogCommand, MergeCommand,
ResetCommand, RmBranchCommand, RmCommand, and StatusCommand.

#### Fields

Each command has its own unique fields to be described in their
individual sections

### AddCommand

Command that stages files to be committed.

#### Fields

### BranchCommand

Command that creates a new branch in the project structure.

#### Fields

### CheckoutCommand

Command that switches the working branch to the one specified.

#### Fields

### FindCommand

Command that finds the specified commit.

#### Fields

### InitCommand

Command that switches initializes the gitlet repository. All other
commands will fail if a repository has yet to be set up.

#### Fields

1. static final File CWD: the current working directory
2. static final File GITLET: the hidden directory storing all files
necessary in a gitlet repository.

### LogCommand

Command that shows a log of the commit history for this projext.

#### Fields

### MergeCommand

Command that does the merging, will specify once I know what need do.

#### Fields

### ResetCommand

Command that does the resetting, again, to be specified.

#### Fields

### RmBranchCommand

Command that removes the specified branch.

#### Fields

### RmCommand

Command that removes the specified commit.

#### Fields

### StatusCommand

Command that shows what's staged and changed I guess.

#### Fields

### Commit

A single commit in the project structure, of which consists entirely
of commits.

#### Fields

1. List<Commit> _children: the child(ren) of a specific commit.
2. List<COmmit> _parents: the parent(s) of a commit.
3. String _message: the commit's message
4. String _branch: the branch of this commit.
5. String _id: the SHA-1 ID of this commit.
6. Date _date: the date at which this commit was created

### Stage

The set of all files that are slated to be committed. Gets cleared
after a new commit.

#### Fields

List<File> Files: the files to be stored in the stage

## Algorithms

### Main

1. main(String[] args): turns the user input into a Command through
readLine(args), then executes that command. Prints an appropriate message
if there are no arguments.
1. readLine(String[] args): determines which type of command the user
entered and constructs a Command extension class accordingly. If it
fails, it returns null.

### Command

1. execute(): performs the desired function of the specific extension
of the command class.

### Commit

1. Commit(String name, String branch): The class constructor. Stores the
name and branch of the commit in the classes's fields.
2. create(): creates and saves the commit, preserving, rewriting,
and adding files as necessary.

### Stage

1. Stage(): the constructor, creates the list of files to be stored
in the stage.
2. addToStage(File f): adds the file to both the list of files within
the class as well as to the staging directory, if changes are made

## Persistence

To create persistence, the structure of the repository will be stored in a single file,
bing written to whenever commands add or remove from the project tree.
This file will contain commits that point to blobs of files, as well as other
commits.

Additionally, each file that gets committed will be stored in the .gitlet
directory, and new ones will only be made if there are changes from different
commits.
