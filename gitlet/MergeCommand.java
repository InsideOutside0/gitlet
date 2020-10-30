package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Initiates the merging of two branches.
 * @author Sam Stahl
 */
public class MergeCommand extends Command {

    /** Initializes the merge command by ensuring there are an appropriate
     *  amount of ARGS to begin a merge.
     */
    MergeCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
        Stage s = new Stage();
        if (!s.getAddedFiles().isEmpty() || !s.getRemovedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            setFailed();
        }
        _branch = args[1];
        if (_branch.equals((getHeadBranch()))) {
            System.out.println("Cannot merge a branch with itself.");
            setFailed();
        }
        if (!branchExists(_branch)) {
            System.out.println("A branch with that name does not exist.");
            setFailed();
        }
        for (File f : CWD.listFiles()) {
            if (untracked(f)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                setFailed();
            }
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            Commit otherHead = getHead(_branch);
            String message = "Merged " + _branch + " into "
                    + getHeadBranch() + ".";
            Commit merge = new Commit(getHeadSha1(), message, getHeadBranch());
            merge.merge(otherHead, _branch);
        }
    }

    /** The branch of the other commit. */
    private String _branch;

}
