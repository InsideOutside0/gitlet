package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

 /** Assorted utilities.
 *  @author P. N. Hilfinger and Sam Stahl
 */
class Utils {

    /* SAM'S METHODS AND FIELDS. */

    /** A magic number that is the length of the SHA-1 IDs. */
    static final int ID_LENGTH = 40;

    /** The current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** The main directory for the Gitlet repository. */
    static final File GITLET = Utils.join(CWD, ".gitlet");

    /** Returns the name of the current head branch. */
    public static String getHeadBranch() {
        File head = join(GITLET, "HEAD.txt");
        return readContentsAsString(head);
    }

    /** Returns the SHA-1 ID of the head commit of BRANCH. */
    public static String getHeadSha1(String branch) {
        File headCommit = join(GITLET, "branches", branch + ".txt");
        return readContentsAsString(headCommit);
    }

    /** Returns the SHA-1 ID of the head commit of the head branch. */
    public static String getHeadSha1() {
        return getHeadSha1(getHeadBranch());
    }

    /** Returns the head commit of given BRANCH. */
    public static Commit getHead(String branch) {
        String c = getHeadSha1(branch);
        File commit = join(GITLET, "commits", c + ".txt");
        return readObject(commit, Commit.class);
    }

    /** Returns the current head commit. */
    public static Commit getHead() {
        return getHead(getHeadBranch());
    }

    /** Sets the head commit of BRANCH to C. */
    public static void setHead(Commit c, String branch) {
        File headCommit = join(GITLET, "branches", branch + ".txt");
        if (!headCommit.exists()) {
            try {
                headCommit.createNewFile();
            } catch (IOException e) {
                throw Utils.error(e.getMessage());
            }
        }
        writeContents(headCommit, c.id());
    }

    /** Returns the file F's name without the .whatever extension.
     *  For whatever reason the .split function did not work.
     */
    public static String filenameWithoutExtension(String f) {
        String out = "";
        for (int i = 0; i < f.length(); i += 1) {
            if (f.charAt(i) == '.') {
                break;
            }
            out += f.charAt(i);
        }
        return out;
    }

    /** Returns true if branch B exists. */
    public static boolean branchExists(String b) {
        File branch = join(GITLET, "branches", b + ".txt");
        return branch.exists();
    }

    /** Returns the commit object when given its full or shortened SHA-1 ID. */
    public static Commit getCommitByID(String id) {
        if (id.length() < ID_LENGTH) {
            SHATraversal t = new SHATraversal(id);
            t.traverse();
            id = t.id();
            if (id == null) {
                return null;
            }
        }
        File f = join(GITLET, "commits", id + ".txt");
        if (!f.exists()) {
            return null;
        }
        return readObject(f, Commit.class);
    }

    /** Returns true if the contents of files A and B match. */
    public static boolean contentsMatch(File a, File b) {
        if ((a == null || !a.exists()) || (b == null || !b.exists())) {
            return false;
        }
        return (readContentsAsString(a).equals(readContentsAsString(b)));
    }

    /** Returns true if F is untracked. */
    public static boolean untracked(File f) {
        if (f.equals(GITLET)) {
            return false;
        }
        Stage s = new Stage();
        Commit head = getHead();
        File inStageAdd = join(GITLET, "stage", "add", f.getName());
        File inStageRm = join(GITLET, "stage", "rm", f.getName());
        return ((!s.getAddedFiles().contains(inStageAdd)
                && head.getVersionOf(f) == null)
            || (s.getRemovedFiles().contains(inStageRm)));
    }

    /** Returns true if the Current Working Directory contains file F. */
    public static boolean cwdContains(File f) {
        return Arrays.asList(CWD.listFiles()).contains(f);
    }

    /* SHA-1 HASH VALUES. */

    /** The length of a complete SHA-1 UID as a hexadecimal numeral. */
    static final int UID_LENGTH = 40;

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems. */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Write OBJ to FILE. */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     *  method. */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     *  method. */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */

    /** Returns a byte array containing the serialized contents of OBJ. */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /** Return a GitletException whose message is composed from MSG and ARGS as
     *  for the String.format method. */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed from MSG and ARGS as for the String.format
     *  method, followed by a newline. */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }
}
