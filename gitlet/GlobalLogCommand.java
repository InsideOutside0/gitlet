package gitlet;

import static gitlet.Utils.GITLET;

/** Contains the logic needed to log every commit.
 * @author Sam Stahl
 */
public class GlobalLogCommand extends Command {

    /** Initializes the command using ARGS to ensure
     * there are no other arguments.
     */
    GlobalLogCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 1) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            new LogTraversal().traverse();
        }
    }

}
