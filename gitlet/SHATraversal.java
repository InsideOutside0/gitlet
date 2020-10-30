package gitlet;

import java.util.ArrayList;
import java.util.List;

/** Finds a commit based on its shortened id.
 * @author Sam Stahl
 */
public class SHATraversal extends Traversal {

    /** Initializes the traversal by setting the SHORTID and
     *  initializing the list of potential ids.
     */
    SHATraversal(String shortID) {
        _shortID = shortID;
        _ids = new ArrayList<>();
    }

    @Override
    void visit(Commit c) {
        if (_shortID.equals(c.id().substring(0, _shortID.length()))) {
            _ids.add(c.id());
        }
    }

    /** Returns the first ID in the list of ids. */
    String id() {
        if (_ids.size() == 0) {
            return null;
        }
        return _ids.get(0);
    }

    /** The shortened form of the ID being sought after. */
    private String _shortID;

    /** List of candidate ids for which the short id can belong. */
    private List<String> _ids;
}
