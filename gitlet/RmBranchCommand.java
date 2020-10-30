package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Contains the logic needed to remove a branch.
 * @author Sam Stahl
 */
public class RmBranchCommand extends Command {

    /** Initializes the command with ARGS to make sure there are
     *  only two arguments.
     */
    RmBranchCommand(String[] args) {
        _args = args;
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (_args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            File branch = join(GITLET, "branches", _args[1] + ".txt");
            if (!branch.exists()) {
                System.out.println("A branch with that name does not exist.");
                return;
            }
            if (_args[1].equals(getHead().branch())) {
                System.out.println("Cannot remove the current branch.");
                return;
            }
            branch.delete();
        }
    }

    /** The arguments fed into the command line. */
    private String[] _args;

}
