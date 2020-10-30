package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static gitlet.Utils.*;

/** The object representation of each commit in a gitlet repository.
 * @author Sam Stahl
 */
public class Commit implements Serializable {

    /** The folder that stores all commit files. */
    private static final File COMMITS = join(GITLET, "commits");

    /** The folder that stores all blobs. */
    private static final File FILES = join(GITLET, "files");

    /** The constructor for the Commit object. Takes in its PARENT 's SHA-1 ID,
     *  its MESSAGE, and it's BRANCH, and sets those values. As well, it assigns
     *  its date to the current time, and adds all its parent's contents to its
     *  contents map, as well as establishing the exceptions list and
     *  _hasConflict in the event of a merge.
     */
    Commit(String parent, String message, String branch) {
        _message = message;
        _branch = branch;
        _date = new Date();
        _parent = parent;
        _contents = new HashMap<>();
        if (_parent != null) {
            for (Map.Entry<File, String> e
                    : parent()._contents.entrySet()) {
                _contents.put(e.getKey(), e.getValue());
            }
        }
        _exceptions = new ArrayList<>();
        _delFromMerge = new ArrayList<>();
        _hasConflict = false;
    }

    /** Returns the SHA-1 ID of this commit. */
    String id() {
        return sha1(serialize(this));
    }

    /** Returns the date of this commit in the acceptable format for logging. */
    String date() {
        SimpleDateFormat d = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return d.format(_date);
    }

    /** Returns the contents of this commit. */
    Map<File, String> getContents() {
        return _contents;
    }

    /** Returns the commit's message. */
    String message() {
        return _message;
    }

    /** Returns the commit's parent commit object. */
    Commit parent() {
        if (_parent == null) {
            return null;
        }
        File parent = join(COMMITS, _parent + ".txt");
        return readObject(parent, Commit.class);
    }

    /** Returns the commit's merge parent's SHA-1 id, if it has one. */
    String mergeParentID() {
        return _mergeParent;
    }

    /** Sets the merge parent value to commit C's id. */
    void setMergeParent(Commit c) {
        _mergeParent = c.id();
    }

    /** Returns true if the commit is tracking the file F. */
    boolean contains(File f) {
        return _contents.containsKey(f);
    }

    /** Returns the commit's branch. */
    String branch() {
        return _branch;
    }

    /** Returns true if the commit has file F in its exceptions. */
    boolean hasExceptedFile(File f) {
        return _exceptions.contains(f);
    }

    /** Returns true if the commit has file F in its exceptions. */
    boolean toBeRemoved(File f) {
        return _delFromMerge.contains(f);
    }

    /** Returns the commit's version of file F. */
    File getVersionOf(File f) {
        if (!_contents.containsKey(f)) {
            return null;
        }
        String sha1 = _contents.get(f);
        return join(FILES, sha1 + ".txt");
    }

    /** Creates the initial commit for a repository.
     * It always has the same id no matter what
     */
    public static void initialCommit() {
        Commit c = new Commit(null, "initial commit", "master");
        c._date = new Date(0);
        c.commit(true);
    }

    /** Adds the files and their sha-1 ids from the STAGE to the commit,
     *  as well as creating new blobs as needed.
     */
    private void addBlobs(List<File> stage) {
        for (File f : stage) {
            String sha1 = sha1(readContentsAsString(f));
            File wdFile = join(Utils.CWD, f.getName());
            _contents.put(wdFile, sha1);
        }
        for (Map.Entry<File, String> e : _contents.entrySet()) {
            File f = join(FILES, e.getValue() + ".txt");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                    writeContents(f, readContentsAsString(e.getKey()));
                } catch (IOException err) {
                    throw error(err.getMessage());
                }
            }
        }
    }

    /** Removes the given files in STAGE from the commit so they are
     *  no longer tracked.
     */
    private void stopTracking(List<File> stage) {
        for (File f : stage) {
            _contents.remove(join(CWD, f.getName()));
        }
    }

    /** Stores the commit data. Accesses the stage to determine if changes
     *  should be made, or ignores it outright during initial commits
     *  or merges if IGNORESTAGE is true. Adds all the necessary blobs
     *  to the files folder and updates the contents of the commit,
     *  and creates a new file that stores the data for the commit,
     *  as well as setting the head of the commit's branch to this commit.
     */
    void commit(boolean ignoreStage) {
        Stage s = new Stage();
        List<File> addedFiles = s.getAddedFiles(),
                removedFiles = s.getRemovedFiles();
        if (addedFiles.isEmpty() && removedFiles.isEmpty() && !ignoreStage) {
            System.out.println("No changes added to the commit.");
            return;
        }
        addBlobs(addedFiles);
        stopTracking(removedFiles);
        s.clear();
        File commit = Utils.join(COMMITS, id() + ".txt");
        try {
            commit.createNewFile();
            writeObject(commit, this);
        } catch (IOException e) {
            throw error(e.getMessage());
        }
        setHead(this, getHeadBranch());
        if (_hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** A normal commit that uses the contents of the stage. */
    void commit() {
        commit(false);
    }

    /** Returns the minimum value integer in the given map M. */
    int minValue(Map<String, Integer> m) {
        int min = Integer.MAX_VALUE;
        for (int n : m.values()) {
            if (n < min) {
                min = n;
            }
        }
        return min;
    }

    /** Returns the first commit found in map M with value N
     *  and returns null if nothing is found.
     */
    Commit getFromValue(Map<String, Integer> m, int n) {
        for (String c : m.keySet()) {
            if (m.get(c) == n) {
                return getCommitByID(c);
            }
        }
        return null;
    }

    /** Returns a latest common ancestor between the head of the current branch
     *  and the OTHERBRANCH. Can be multiple so chooses one arbitrarily.
     */
    private Commit latestCommonAncestor(String otherBranch) {
        Map<String, Integer> candidates = new HashMap<>(),
                mainPath = new HashMap<>(), otherPath = new HashMap<>();
        Commit lca, currentMain = getHead(), currentOther
                = getHead(otherBranch);
        int distance = 0;
        boolean finished = false;
        while (!finished) {
            if (currentMain != null) {
                mainPath.put(currentMain.id(), distance);
                if (currentMain.mergeParentID() != null) {
                    String mergeParent = currentMain.mergeParentID();
                    mainPath.put(mergeParent, distance + 1);
                    if (otherPath.containsKey(mergeParent)) {
                        candidates.put(mergeParent, distance + 1);
                        finished = true;
                    }
                }
                if (otherPath.containsKey(currentMain.id())) {
                    candidates.put(currentMain.id(), distance);
                    finished = true;
                }
                currentMain = currentMain.parent();
            }
            if (currentOther != null) {
                otherPath.put(currentOther.id(), distance);
                if (currentOther.mergeParentID() != null) {
                    String mergeParent = currentOther.mergeParentID();
                    otherPath.put(mergeParent, distance + 1);
                    if (otherPath.containsKey(mergeParent)) {
                        candidates.put(mergeParent, distance + 1);
                        finished = true;
                    }
                }
                if (mainPath.containsKey(currentOther.id())) {
                    candidates.put(currentOther.id(), distance);
                    finished = true;
                }
                currentOther = currentOther.parent();
            }
            if (currentMain == null && currentOther == null) {
                finished = true;
            }
            distance += 1;
        }
        lca = getFromValue(candidates, minValue(candidates));
        if (lca == null) {
            return null;
        } else if (lca.id().equals(getHead(otherBranch).id())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch");
            return null;
        } else if (lca.id().equals(getHead().id())) {
            System.out.println("Current branch fast-forwarded.");
            String[] checkoutArgs = {"checkout", otherBranch};
            new CheckoutCommand(checkoutArgs).execute();
            return null;
        }
        return lca;
    }

    /** Returns true if F has the same contents in this branch and the
     *  SPLITPOINT, but differs between the OTHER branch and the SPLITPOINT.
     */
    private boolean onlyChangedInOtherBranch(File f, Commit splitPoint,
                                             Commit other) {
        return (contains(f) && splitPoint.contains(f)
                && contentsMatch(splitPoint.getVersionOf(f),
                getVersionOf(f)) && other.contains(f)
                && !contentsMatch(splitPoint.getVersionOf(f),
                other.getVersionOf(f)));
    }

    /** Returns true if file F is present in the SPLITPOINT but removed
     *  in the current branch and the OTHER branch.
     */
    private boolean presentInCWDRemovedInBoth(File f, Commit splitPoint,
                                              Commit other) {
        return (!contains(f) && !other.contains(f) && splitPoint.contains(f)
                && cwdContains(f));
    }

    /** Returns true if the file F is only present in the branch of
     *  the OTHER commit, and not in the SPLITPOINT or in the current branch.
     */
    private boolean onlyInGivenBranch(File f, Commit splitPoint, Commit other) {
        return (!contains(f) && !splitPoint.contains(f) && other.contains(f));
    }

    /** Returns true if File F is unmodified from the SPLITPOINT
     *  in the current branch, but is absent in the OTHER branch.
     */
    private boolean ummodCurrentAbsentGiven(File f, Commit splitPoint,
                                            Commit other) {
        return (splitPoint.contains(f) && !other.contains(f)
            && contentsMatch(getVersionOf(f), splitPoint.getVersionOf(f)));
    }

    /** Returns true if file F is in conflict between the current branch and
     *  the OTHER branch from the SPLITPOINT. This occurs if either the file
     *  has different contents in each branch, or if it has been modified in
     *  one branch and removed in the other.
     */
    private boolean inConflict(File f, Commit splitPoint, Commit other) {
        boolean differentChanges = (contains(f) && other.contains(f)
                && !contentsMatch(getVersionOf(f), other.getVersionOf(f))),
                oneAbsence = (splitPoint.contains(f)
                && ((!contains(f) && other.contains(f)
                && !contentsMatch(splitPoint.getVersionOf(f),
                        other.getVersionOf(f)))
                || (!other.contains(f) && contains(f)
                && !contentsMatch(splitPoint.getVersionOf(f),
                        getVersionOf(f)))));
        return differentChanges || oneAbsence;
    }

    /** Articulates merge conflicts among file F between
     *  this branch and the OTHER branch.
     */
    void mergeConflict(File f, Commit other) {
        _hasConflict = true;
        File current = getVersionOf(f),
                fromBranch = other.getVersionOf(f);
        String mergeContents, newName,
                cContents = "", otherContents = "";
        if (current != null && current.exists()) {
            cContents = readContentsAsString(current);
        }
        if (fromBranch != null && fromBranch.exists()) {
            otherContents = readContentsAsString(fromBranch);
        }
        mergeContents = "<<<<<<< HEAD\n" + cContents + "=======\n"
                + otherContents + ">>>>>>>\n";
        newName = sha1(mergeContents);
        _contents.put(f, newName);
        File merged = join(FILES, newName + ".txt");
        try {
            merged.createNewFile();
            writeContents(merged, mergeContents);
        } catch (IOException e) {
            throw error(e.getMessage());
        }
    }

    /** Adds to, modifies, and removes from the contents of the merge commit
     *  based on the differences between the current branch, the SPLITPOINT,
     *  and the OTHER branch.
     */
    private void resolveFilesAtSplitPoint(Commit splitPoint, Commit other) {
        for (File f : other.getContents().keySet()) {
            if (onlyInGivenBranch(f, splitPoint, other)) {
                _contents.put(f, other.getContents().get(f));
            }
        }
        for (File f : splitPoint.getContents().keySet()) {
            if (onlyChangedInOtherBranch(f, splitPoint, other)) {
                _contents.put(f, other.getVersionOf(f).getName());
            } else if (presentInCWDRemovedInBoth(f, splitPoint, other)) {
                _exceptions.add(f);
            } else if (ummodCurrentAbsentGiven(f, splitPoint, other)) {
                _contents.remove(f);
                _delFromMerge.add(f);
            } else if (inConflict(f, splitPoint, other)) {
                mergeConflict(f, other);
            }
        }
        for (File f : CWD.listFiles()) {
            if (!splitPoint.contains(f) && inConflict(f, splitPoint, other)) {
                mergeConflict(f, other);
            }
        }
    }

    /** Merges the current branch and the branch of the OTHER
     *  commit together in a new commit, and then checks out the commit.
     *  OTHERBRANCH is used for special cases where the other commit
     *  does not store the branch being merged in.
     */
    void merge(Commit other, String otherBranch) {
        setMergeParent(other);
        Commit splitPoint = latestCommonAncestor(otherBranch);
        if (splitPoint == null) {
            return;
        }
        resolveFilesAtSplitPoint(splitPoint, other);
        commit(true);
        String[] resetArgs = {"reset", id()};
        new ResetCommand(resetArgs).reset(true);
    }

    /** True if commit contains a merge conflict. */
    private boolean _hasConflict;

    /** The primary parent of this commit's SHA-1 ID. */
    private String _parent;

    /** The secondary parent of this commit, in the event of a merge. */
    private String _mergeParent;

    /** The files the commit contains. */
    private Map<File, String> _contents;

    /** Files not technically tracked but not deleted in event of merge. */
    private List<File> _exceptions;

    /** Files to be deleted after merge. */
    private List<File> _delFromMerge;

    /** The commit's message. */
    private String _message;

    /** The branch this commit is on. */
    private String _branch;

    /** The date and time of this commit. */
    private Date _date;

}
