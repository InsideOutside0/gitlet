package gitlet;

import static gitlet.Utils.GITLET;

/** Contains the logic needed to log the head commit and its parents.
 * @author Sam Stahl
 */
public class LogCommand extends Command {

    /** Initializes the log command with ARGS to ensure
     *  that there are no additional arguments.
     */
    LogCommand(String[] args) {
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
            Commit c = Utils.getHead();
            while (c != null) {
                log(c);
                c = c.parent();
            }
        }
    }

    /** Returns the shortened form of C's parent's
     *  and merge parent's ids.
     */
    private static String mergeIDs(Commit c) {
        return c.id().substring(0, 7) + " " + c.mergeParentID().substring(0, 7);
    }

    /** Logs all details of commit C. */
    static void log(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.id());
        if (c.mergeParentID() != null) {
            System.out.println("Merge: " + mergeIDs(c));
        }
        System.out.println("Date: " + c.date());
        System.out.println(c.message());
        System.out.println();
    }

}
