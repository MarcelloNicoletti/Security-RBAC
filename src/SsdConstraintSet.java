import java.util.HashSet;
import java.util.Set;

public class SsdConstraintSet {
    private Set<SsdConstraint> constraints;

    /**
     * Constructs a new empty set of constraints.
     */
    public SsdConstraintSet () {
        this.constraints = new HashSet<>();
    }

    /**
     * Adds a new constraint to this set.
     *
     * @param n The cardinality of the new constraint.
     * @param roles The set the new constraint applies to.
     * @return True if the constraint was added.
     */
    public boolean addConstraint (int n, Set<RbacRole> roles) {
        return addConstraint(new SsdConstraint(n, roles));
    }

    /**
     * Adds the constraint to this set.
     *
     * @param constraint The constraint to add.
     * @return True if the constraint was added.
     */
    public boolean addConstraint (SsdConstraint constraint) {
        return constraints.add(constraint);
    }

    /**
     * Tests a set of roles against all constraints.
     *
     * @param roles The set of roles to test.
     * @return True if set satisfies all constraints.
     */
    public boolean testAgainstAll (Set<RbacRole> roles) {
        return constraints.stream().allMatch(constraint ->
                constraint.test(roles));
    }
}
