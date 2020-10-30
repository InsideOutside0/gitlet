package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Sam Stahl
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            return;
        }
        Command command = readLine(args);
        if (command != null) {
            command.execute();
        }
    }

    /** Returns the appropriate command type based
     *  on input to the console, stored in ARGS.
     */
    public static Command readLine(String[] args) {
        switch (args[0]) {
        case "init":
            return new InitCommand(args);
        case "add":
            return new AddCommand(args);
        case "rm":
            return new RmCommand(args);
        case "commit":
            return new CommitCommand(args);
        case "log":
            return new LogCommand(args);
        case "global-log":
            return new GlobalLogCommand(args);
        case "find":
            return new FindCommand(args);
        case "status":
            return new StatusCommand(args);
        case "checkout":
            return new CheckoutCommand(args);
        case "branch":
            return new BranchCommand(args);
        case "rm-branch":
            return new RmBranchCommand(args);
        case "reset":
            return new ResetCommand(args);
        case "merge":
            return new MergeCommand(args);
        default:
            System.out.println("No command with that name exists.");
            return null;
        }
    }

}
