import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class SsdConstraintSet {
    private Set<SsdConstraint> constraints;

    /**
     * Constructs a new empty set of constraints.
     */
    public SsdConstraintSet () {
        this.constraints = new LinkedHashSet<>();
    }

    public static SsdConstraintSet getConstraintSetFromFile (String filename) {
        SsdConstraintSet constraints;
        do {
            constraints = readConstraintsFromFile(filename);
            if (constraints == null) {
                System.out.println("Edit the file and press <enter> to " +
                    "continue.");
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (constraints == null);
        return constraints;
    }

    private static SsdConstraintSet readConstraintsFromFile (String filename) {
        // TODO: Add reading code
        return null;
    }

    /**
     * Adds a new constraint to this set.
     *
     * @param n     The cardinality of the new constraint.
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

    public int indexOfFirstBrokenConstraint (Set<RbacRole> roles) {
        int idx = -1;
        int i = 0;
        for (SsdConstraint constraint : constraints) {
            if (!constraint.test(roles)) {
                idx = i;
                break;
            }
            i++;
        }
        return idx;
    }

    public void printConstraints () {
        // TODO: Add printing logic
    }
}
