package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/** Contains the logic needed to create a branch.
 * @author Sam Stahl
 */
public class BranchCommand extends Command {

    /** The command that creates a new branch.
     *  ARGS[1] is the name of the new branch.
     */
    BranchCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        } else {
            _name = args[1];
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            File newBranch = join(GITLET, "branches", _name + ".txt");
            if (newBranch.exists()) {
                System.out.println("A branch with that name "
                        + "already exists.");
                return;
            }
            try {
                newBranch.createNewFile();
            } catch (IOException e) {
                throw error(e.getMessage());
            }
            writeContents(newBranch, getHeadSha1());
        }
    }

    /** The name of the new branch. */
    private String _name;

}
