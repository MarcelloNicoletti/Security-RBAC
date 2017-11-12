import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SsdConstraintSet {
    private static final RbacComparator RBAC_COMPARATOR = new RbacComparator();
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
        SsdConstraintSet constraintSet = new SsdConstraintSet();
        File file = new File(filename);
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.err.printf("The resource objects file, %s, does not exist.",
                filename);
            return null;
        }

        int lineNum = 1;
        while (input.hasNextLine()) {
            int n = input.nextInt();
            String[] line = input.nextLine().trim().split("\\s+");
            Set<RbacRole> roles = Arrays.stream(line).map(RbacRole::new)
                .collect(Collectors.toSet());
            if (n < 2) {
                System.out.printf("Invalid line found in %s: line %d",
                    filename, lineNum);
                return null;
            }
            constraintSet.addConstraint(n, roles);
            lineNum++;
        }
        return constraintSet;
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
        int i = 1;
        for (SsdConstraint constraint : constraints) {
            String roles = getRolesString(constraint.getRoleSet());
            System.out.printf("Constraint %d, n = %d, set of roles = %s%n",
                i, constraint.getN(), roles);
        }
    }

    private String getRolesString (Set<RbacRole> roleSet) {
        List<RbacRole> roleList = new ArrayList<>(roleSet);
        roleList.sort(RBAC_COMPARATOR);
        StringBuilder sb = new StringBuilder("{");
        sb.append(roleList.get(0).toString());

        long toSkip = 1;
        for (RbacRole rbacRole : roleList) {
            if (toSkip > 0) {
                toSkip--;
                continue;
            }
            sb.append(", ").append(rbacRole.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
