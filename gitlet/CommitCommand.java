package gitlet;

import static gitlet.Utils.GITLET;

/** Contains the logic needed to commit changes.
 * @author Sam Stahl
 */
public class CommitCommand extends Command {

    /** Initializes the command and uses ARGS to set the values
     *  of _message and _branch.
     */
    CommitCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        } else if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            setFailed();
        } else {
            _message = args[1];
            _branch = Utils.getHeadBranch();
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            new Commit(Utils.getHeadSha1(), _message, _branch).commit();
        }
    }

    /** The branch of the new commit. */
    private String _branch;

    /** The message of the new commit. */
    private String _message;

}
