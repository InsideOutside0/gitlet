package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static gitlet.Utils.*;

/** Contains the logic needed to checkout a file or branch.
 * @author Sam Stahl
 */
public class CheckoutCommand extends Command {

    /** The checkout command, contains all ARGS needed
     *  for each possible case of the command.
     */
    CheckoutCommand(String[] args) {
        _args = args;
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (incorrectOperands()) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
    }

    /** Returns true if the args are formatted incorrectly. */
    boolean incorrectOperands() {
        if (_args.length == 1 || _args.length > 4) {
            return true;
        }
        if (_args.length == 3 && !_args[1].equals("--")) {
            return true;
        }
        if (_args.length == 4 && !_args[2].equals("--")) {
            return true;
        }
        return false;
    }

    /** Changes the given file in the working directory
     *  to contain those of Commit C's version.
     */
    private void checkoutFileFromCommit(Commit c) {
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File fromCommit = c.getVersionOf(_f);
        if (fromCommit == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        if (!_f.exists()) {
            try {
                _f.createNewFile();
            } catch (IOException e) {
                throw error(e.getMessage());
            }
        }
        writeContents(_f, readContentsAsString(fromCommit));
    }

    /** Changes the contents of the file of the working directory
     *  to that of its version in the head commit.
     */
    private void checkoutFileFromHead() {
        checkoutFileFromCommit(getHead());
    }

    /** Changes all the contents of the working directory to
     *  match the contents of the head commit of the given branch
     *  and sets the head branch to it.
     */
    private void checkoutBranch() {
        String currentBranch = getHeadBranch(), newBranch = _args[1];
        if (currentBranch.equals(newBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File[] branches = join(GITLET, "branches").listFiles();
        boolean branchExists = false;
        for (File f : branches) {
            String name = filenameWithoutExtension(f.getName());
            if (name.equals(newBranch)) {
                branchExists = true;
                break;
            }
        }
        if (!branchExists) {
            System.out.println("No such branch exists.");
            return;
        }
        List<File> cwdFiles = Arrays.asList(CWD.listFiles());
        for (File f : cwdFiles) {
            if (untracked(f)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        File head = join(GITLET, "HEAD.txt");
        writeContents(head, newBranch);
        Commit newHead = getHead();
        for (File f : cwdFiles) {
            if (newHead.getVersionOf(f) == null) {
                f.delete();
            }
        }
        for (File f : newHead.getContents().keySet()) {
            if (!cwdFiles.contains(f)) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    throw error(e.getMessage());
                }
            }
            writeContents(f, readContentsAsString(newHead.getVersionOf(f)));
        }
        new Stage().clear();
    }

    @Override
    void execute() {
        if (!failed()) {
            switch (_args.length) {
            case 2:
                checkoutBranch();
                break;
            case 3:
                _f = join(CWD, _args[2]);
                checkoutFileFromHead();
                break;
            case 4:
                _f = join(CWD, _args[3]);
                checkoutFileFromCommit(getCommitByID(_args[1]));
                break;
            default:
                throw Utils.error("fuck");
            }
        }
    }

    /** The arguments passed in from the command line. */
    private String[] _args;

    /** The file being affected by checkout. */
    private File _f;

}
