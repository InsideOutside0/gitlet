package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static gitlet.Utils.*;

/** Contains the logic needed to display the current status.
 * @author Sam Stahl
 */
public class StatusCommand extends Command {

    /** Initializes the command with ARGS to ensure there is only
     *  one argument.
     */
    StatusCommand(String[] args) {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            setFailed();
        } else if (args.length != 1) {
            System.out.println("Incorrect operands.");
            setFailed();
        }
    }

    /** Displays the branches in the repository. */
    private void displayBranches() {
        System.out.println("=== Branches ===");
        File[] heads = join(GITLET, "branches").listFiles();
        System.out.println("*" + getHeadBranch());
        List<String> sortedNames = new ArrayList<>();
        if (heads != null) {
            for (File f : heads) {
                String name = filenameWithoutExtension(f.getName());
                if (!name.equals(getHeadBranch())) {
                    sortedNames.add(name);
                }
            }
            sortedNames.sort(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareTo(t1);
                }
            });
            for (String str : sortedNames) {
                System.out.println(str);
            }
        }
        System.out.println();
    }

    /** Display the staged files in the repository. */
    private void displayStaged() {
        System.out.println("=== Staged Files ===");
        Stage s = new Stage();
        List<String> sortedNames = new ArrayList<>();
        for (File f : s.getAddedFiles()) {
            sortedNames.add(f.getName());
        }
        sortedNames.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        for (String str : sortedNames) {
            System.out.println(str);
        }
        System.out.println();
    }

    /** Displays the removed files from the repository. */
    private void displayRemoved() {
        System.out.println("=== Removed Files ===");
        Stage s = new Stage();
        List<String> sortedNames = new ArrayList<>();
        for (File f : s.getRemovedFiles()) {
            sortedNames.add(f.getName());
        }
        sortedNames.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        for (String str : sortedNames) {
            System.out.println(str);
        }
        System.out.println();
    }

    /** Displays the modified yet not staged files in the repo. */
    private void displayModNotStaged() {
        Commit head = getHead();
        Stage s = new Stage();
        List<File> cwdFiles = Arrays.asList(CWD.listFiles());
        for (File f : head.getContents().keySet()) {
            if (!s.getRemovedFiles().contains(f)
                    && !cwdFiles.contains(f)) {
                System.out.println(f.getName());
            }
            if (!cwdFiles.contains(f)
                    && !contentsMatch(f, head.getVersionOf(f))) {
                System.out.println(f.getName());
            }
        }
        System.out.println();
    }

    /** Displays the untracked files in the repo. */
    private void displayUntracked() {
        File[] cwdFiles = CWD.listFiles();
        for (File f : cwdFiles) {
            if (untracked(f)) {
                System.out.println(f.getName());
            }
        }
        System.out.println();
    }

    @Override
    void execute() {
        if (!failed()) {
            displayBranches();
            displayStaged();
            displayRemoved();
            System.out.println("=== Modifications Not Staged For Commit ===");
            System.out.println();
            System.out.println("=== Untracked Files ===");
            System.out.println();
        }
    }

}
