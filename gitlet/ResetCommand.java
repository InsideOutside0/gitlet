package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Contains the logic needed to reset the working
 *  directory to a given commit.
 * @author Sam Stahl
 */
public class ResetCommand extends Command {

    /** Initializes the command with ARGS such that there are only
     *  2 arguments, setting the second one to _id and using that
     *  to get _commit.
     */
    ResetCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
        _id = args[1];
        _commit = getCommitByID(_id);
        if (_commit == null) {
            System.out.println("No commit with that id exists.");
            setFailed();
        }
        for (File f : CWD.listFiles()) {
            if (untracked(f) && !_commit.toBeRemoved(f)) {
                System.out.println("There is an "
                        + "untracked file in the way; delete it, "
                        + "or add and commit it first");
                setFailed();
                break;
            }
        }
    }

    /** Resets the CWD to the given commit. Can also be called after
     *  merging to allow certain files to be present after resetting
     *  if ALLOWEXCEPTIONS is true.
     */
    void reset(boolean allowExceptions) {
        for (File f :_commit.getContents().keySet()) {
            String[] checkoutArgs = {"checkout", _id, "--", f.getName()};
            new CheckoutCommand(checkoutArgs).execute();
        }
        for (File f : CWD.listFiles()) {
            if (!_commit.getContents().containsKey(f)) {
                if (!(allowExceptions && _commit.hasExceptedFile(f))) {
                    f.delete();
                }
            }
        }
        setHead(_commit, getHeadBranch());
        new Stage().clear();
    }

    @Override
    void execute() {
        if (!failed()) {
            reset(false);
        }
    }

    /** The ID of the commit given in the command. */
    private String _id;

    /** The commit pertaining to the given ID. */
    private Commit _commit;

}
