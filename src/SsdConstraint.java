import java.util.HashSet;
import java.util.Set;

public class SsdConstraint {
    private final int n;
    private final Set<RbacRole> roleSet;

    /**
     * Constructs a new constraint on a set of roles with a cardinality of N.
     * A set of roles "passes" a constraint as long as less than N roles match.
     * N = 2 is a mutually exclusive constraint as a roleSet may only have one
     * from this constraint that matches to pass.
     *
     * @param n       The Cardinality of the constraint.
     * @param roleSet The set of roles under constraint.
     */
    public SsdConstraint (int n, Set<RbacRole> roleSet) {
        if (n < 2) {
            throw new IllegalArgumentException("N must be greater than or " +
                "equal to 2.");
        }
        this.n = n;
        this.roleSet = roleSet;
    }

    /**
     * Tests a set of roles against this constraint. Constraint is violated
     * if N or more roles exist in this set.
     *
     * @param testRoles The set of roles to test.
     * @return true if this constraint is satisfied by the passed roles.
     */
    public boolean test (Set<RbacRole> testRoles) {
        Set<RbacRole> intersection = new HashSet<>(roleSet);
        intersection.retainAll(testRoles);

        return intersection.size() < n;
    }

    public int getN () {
        return n;
    }

    public Set<RbacRole> getRoleSet () {
        return roleSet;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SsdConstraint)) {
            return false;
        }

        SsdConstraint that = (SsdConstraint)o;

        return n == that.n && roleSet.equals(that.roleSet);
    }

    @Override
    public int hashCode () {
        int result = n;
        result = 31 * result + roleSet.hashCode();
        return result;
    }
}
