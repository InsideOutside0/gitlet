package gitlet;

/** Base class for all gitlet commands.
 *  @author Sam Stahl
 */
public abstract class Command {

    /** Performs the function of the desired command. */
    abstract void execute();

    /** Determines if the command is allowed to execute. */
    private boolean _failed = false;

    /** Sets _failed to true. */
    void setFailed() {
        _failed = true;
    }

    /** Returns the value of _failed. */
    boolean failed() {
        return _failed;
    }

}
