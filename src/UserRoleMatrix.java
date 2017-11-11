import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class UserRoleMatrix {
    Map<RbacUser, Set<RbacRole>> matrix;
    Map<RbacRole, Set<RbacUser>> usersPerRole;
    SsdConstraintSet constraints;

    /**
     * Constructs a new user-role matrix subject to a set of constraints.
     *
     * @param constraints The set of constraints that apply to these users.
     */
    public UserRoleMatrix (SsdConstraintSet constraints) {
        this.constraints = constraints;
    }

    void addUsersFromFile (String filename) {
        boolean error = false;
        File usersFile = new File(filename);
        Scanner input = null;
        do {
            try {
                input = new Scanner(new FileInputStream(usersFile));
            } catch (FileNotFoundException e) {
                System.err.printf("Users file %s not found.", filename);
                System.exit(1);
            }
            int lineNum = 1;

            while (input.hasNextLine()) {
                String[] row = input.nextLine().split("\\s+");
                RbacUser user = new RbacUser(row[0]);
                Set<RbacRole> roles =
                    Arrays.stream(row).skip(1).map(RbacRole::new)
                        .collect(Collectors.toSet());
                boolean added = this.giveRolesToUser(user, roles);
                if (!added) {
                    error = true;
                    String errorMsg;
                    int constraintBroken =
                        this.getConstraintSet()
                            .indexOfFirstBrokenConstraint(roles);
                    if (constraintBroken != -1) {
                        errorMsg = "Constraint #" + constraintBroken;
                    } else {
                        errorMsg = "Duplicated user " + user;
                    }
                    System.out.printf("Invalid line found in %s on line " +
                        "%d due to %s.", filename, lineNum, errorMsg);
                    Main.displayEditMessageIfNull(null);
                    break;
                }
                lineNum++;
            }
        } while (error);
    }

    public boolean giveRolesToUser (RbacUser user, Set<RbacRole> roles) {
        Set<RbacRole> testRoles = new HashSet<>(matrix.computeIfAbsent(user,
            k -> new HashSet<>()));

        if (testRoles.size() != 0) {
            return false;
        }

        testRoles.addAll(roles);
        if (constraints.testAgainstAll(testRoles)) {
            matrix.put(user, testRoles);
            roles.forEach(role -> usersPerRole.computeIfAbsent(role, k -> new
                HashSet<>()).add(user));
            return true;
        } else {
            return false;
        }
    }

    public void clearUsers () {
        matrix.clear();
        usersPerRole.clear();
    }

    public SsdConstraintSet getConstraintSet () {
        return constraints;
    }
}
