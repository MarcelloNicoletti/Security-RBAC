import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public boolean giveRoleToUser (RbacUser user, RbacRole role) {
        Set<RbacRole> test = new HashSet<>(matrix.computeIfAbsent(user,
                k -> new HashSet<>()));
        boolean added = test.add(role);
        if (constraints.testAgainstAll(test)) {
            matrix.put(user, test);
            usersPerRole.computeIfAbsent(role, k -> new HashSet<>()).add(user);
            return added;
        } else {
            return false;
        }
    }

    public boolean giveRolesToUser (RbacUser user, Set<RbacRole> roles) {
        Set<RbacRole> test = new HashSet<>(matrix.computeIfAbsent(user,
                k -> new HashSet<>()));
        boolean added = test.addAll(roles);
        if (constraints.testAgainstAll(test)) {
            matrix.put(user, test);
            roles.forEach(role -> usersPerRole.computeIfAbsent(role, k -> new
                    HashSet<>()).add(user));
            return added;
        } else {
            return false;
        }
    }
}
