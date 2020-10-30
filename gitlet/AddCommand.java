package gitlet;

import java.io.File;

import static gitlet.Utils.CWD;
import static gitlet.Utils.GITLET;

/** Contains the logic needed to add a file to the stage.
 * @author Sam Stahl
 */
public class AddCommand extends Command {

    /** The command that adds a file to the stage.
     *  ARGS[1] is the file to be added.
     */
    AddCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length == 1 || args.length > 2) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
        _file = Utils.join(CWD, args[1]);
        if (!_file.exists()) {
            System.out.println("File does not exist.");
            setFailed();
        }
    }

    @Override
    void execute() {
        if (!failed()) {
            new Stage().addToStage(_file);
        }
    }

    /** The file to be added to the stage. */
    private File _file;

}
