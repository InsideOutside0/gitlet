package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.GITLET;

/** The command for initializing a Gitlet repository.
 *  @author Sam Stahl
 */
public class InitCommand extends Command {

    /** Initializes the command with ARGS to ensure that there
     *  are no other arguments.
     */
    InitCommand(String[] args) {
        if (args.length != 1) {
            setFailed();
            System.out.println("Incorrect operands.");
        } else if (GITLET.exists()) {
            setFailed();
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
        }
    }

    /** Creates every file and folder needed for using a gitlet repository. */
    private void setUp() {
        GITLET.mkdir();
        Utils.join(GITLET, "commits").mkdir();
        File stage = Utils.join(GITLET, "stage");
        stage.mkdir();
        Utils.join(stage, "add").mkdir();
        Utils.join(stage, "rm").mkdir();
        File branches = Utils.join(GITLET, "branches");
        branches.mkdir();
        File files = Utils.join(GITLET, "files");
        files.mkdir();
        try {
            File head = Utils.join(GITLET, "HEAD.txt");
            head.createNewFile();
            Utils.writeContents(head, "master");
        } catch (IOException e) {
            throw Utils.error(e.getMessage());
        }
    }

    @Override
    void execute() {
        if (failed()) {
            return;
        }
        setUp();
        Commit.initialCommit();
    }

}
