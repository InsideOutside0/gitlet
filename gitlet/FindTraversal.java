package gitlet;

/** A traversal of the commit tree used to find
 *  specific commits by message.
 * @author Sam Stahl
 */
public class FindTraversal extends Traversal {

    /** Initializes the traversal by setting its
     *  MESSAGE and setting its count to 0.
     */
    FindTraversal(String message) {
        _message = message;
        _count = 0;
    }

    @Override
    void visit(Commit c) {
        if (c.message().equals(_message)) {
            System.out.println(c.id());
            _count += 1;
        }
    }

    /** Returns the number of commits found with the message. */
    int count() {
        return _count;
    }

    /** The message used to find commits. */
    private String _message;

    /** The number of commits that have the given message. */
    private int _count;

}
