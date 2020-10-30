package gitlet;

/** A traversal that logs all commits.
 * @author Sam Stahl
 */
public class LogTraversal extends Traversal {

    @Override
    void visit(Commit c) {
        LogCommand.log(c);
    }

}
