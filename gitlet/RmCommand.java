package gitlet;

import java.io.File;

import static gitlet.Utils.CWD;
import static gitlet.Utils.GITLET;

/** Contains the logic needed to remove a file from the stage.
 * @author Sam Stahl
 */
public class RmCommand extends Command {

    /** Initializes the remove command with ARGS to ensure
     *  that only one file gets removed.
     */
    RmCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length == 1 || args.length > 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
        _file = Utils.join(CWD, args[1]);
        _stage = new Stage();
        if (!_stage.trackedOrContains(_file)) {
            System.out.println("No reason to remove the file.");
            setFailed();
        }
    }

    @Override
    void execute() {
        if (failed()) {
            return;
        }
        _stage.remove(_file);
    }

    /** The file to be removed. */
    private File _file;

    /** The stage from which the file will be remvoed. */
    private Stage _stage;

}
