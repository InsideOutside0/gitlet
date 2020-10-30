package gitlet;

import static gitlet.Utils.GITLET;

/** Contains the logic needed to find a commit from it's message.
 * @author Sam Stahl
 */
public class FindCommand extends Command {

    /** Initializes the command using ARGS to set _message. */
    FindCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        } else {
            _message = args[1];
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            FindTraversal t = new FindTraversal(_message);
            t.traverse();
            if (t.count() == 0) {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    /** The message used to locate commits. */
    private String _message;

}
