package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** A traversal of every single commit in a repository.
 * @author Sam Stahl
 */
public abstract class Traversal {

    /** The folder containing all commits in the repo. */
    private static final File COMMITS = join(GITLET, "commits");

    /** Traverses every single commit in the commit tree while only
     *  visiting each commit once.
     */
    void traverse() {
        File[] commits = COMMITS.listFiles();
        for (File f : commits) {
            Commit c = readObject(f, Commit.class);
            visit(c);
        }
    }

    /** Performs a specific task upon visiting commit C during a traversal. */
    abstract void visit(Commit c);
}
