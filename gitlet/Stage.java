package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gitlet.Utils.*;

/** The object representation of the files in the stage.
 * @author Sam Stahl
 */
public class Stage {

    /** The folder that houses the stage. */
    private static final File STAGE = join(GITLET, "stage");

    /** The folder that houses the added files. */
    private static final File ADD = join(STAGE, "add");

    /** The folder that houses the removed files. */
    private static final File RM = join(STAGE, "rm");

    /** Initializes the stage by updating its contents based on the
     *  files present.
     */
    Stage() {
        updateContents();
    }

    /** Updates the lists for files to be added and removed based
     *  on the contents of the add and rm folders inside the stage folder.
     */
    void updateContents() {
        _add = new ArrayList<>();
        _remove = new ArrayList<>();
        File[] toAdd = ADD.listFiles(), toRemove = RM.listFiles();
        if (toAdd != null) {
            _add.addAll(Arrays.asList(toAdd));
        }
        if (toRemove != null) {
            _remove.addAll(Arrays.asList(toRemove));
        }
    }

    /** Adds File F to the stage by adding a version
     *  of it to the add folder within stage, if possible.
     */
    void addToStage(File f) {
        Commit head = getHead();
        File toStage = join(ADD, f.getName());
        if (_remove.contains(join(RM, f.getName()))) {
            _remove.remove(join(RM, f.getName()));
            join(RM, f.getName()).delete();
        }
        if (head.contains(f)) {
            File fromCommit = head.getVersionOf(f);
            if (contentsMatch(f, fromCommit)) {
                remove(f, true);
                return;
            }
        }
        try {
            toStage.createNewFile();
        } catch (IOException e) {
            throw Utils.error(e.getMessage());
        }
        String fromF = Utils.readContentsAsString(f);
        Utils.writeContents(toStage, fromF);
        updateContents();
    }

    /** Either removes F from the add directory or slates
     *  it for removal from the CWD. SOFT removals only
     *  affect the add directory.
     */
    private void remove(File f, boolean soft) {
        File inAdd = join(ADD, f.getName());
        if (inAdd.exists()) {
            inAdd.delete();
            return;
        }
        if (!soft) {
            File toStage = join(RM, f.getName());
            try {
                toStage.createNewFile();
            } catch (IOException e) {
                throw Utils.error(e.getMessage());
            }
            if (f.exists()) {
                String fromF = Utils.readContentsAsString(f);
                Utils.writeContents(toStage, fromF);
                f.delete();
            }
        }
        updateContents();
    }

    /** Shortcut to do hard removals of file F. */
    void remove(File f) {
        remove(f, false);
    }


    /** Returns true if file F is tracked or
     * if the stage contains the file.
     */
    boolean trackedOrContains(File f) {
        File inStage = join(ADD, f.getName());
        return (_add.contains(inStage) || getHead().contains(f));
    }

    /** Returns all added files. */
    List<File> getAddedFiles() {
        return _add;
    }

    /** Returns all files slated for removal. */
    List<File> getRemovedFiles() {
        return _remove;
    }

    /** Clear's the stage's persistence. */
    void clear() {
        for (File f : _add) {
            f.delete();
        }
        for (File f : _remove) {
            f.delete();
        }
        updateContents();
    }

    /** List of files to be added. */
    private List<File> _add;

    /** List of files to be removed. */
    private List<File> _remove;

}
