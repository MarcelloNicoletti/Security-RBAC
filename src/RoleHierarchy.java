import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Acts like a graph relating roles represented by string keys to their place
 * in the hierarchy. A role lends its permissions to its descendant. A role
 * can only have one descendant, but many ascendants. That makes this graph a
 * tree, or possibly a forrest, directed from leaves to root(s).
 */
public class RoleHierarchy {
    private static final RbacComparator RBAC_COMPARATOR = new RbacComparator();

    private Set<RbacRole> roles;
    private Map<RbacRole, Set<RbacRole>> ascendants;
    private Map<RbacRole, RbacRole> descendants;

    /**
     * Initializes a new hierarchy with no relationships yet.
     */
    public RoleHierarchy () {
        this.roles = new HashSet<>();
        this.ascendants = new HashMap<>();
        this.descendants = new HashMap<>();
    }

    public static RoleHierarchy getRoleHierarchyFromFile (String filename) {
        RoleHierarchy roleHierarchy;
        do {
            roleHierarchy = readRolesFromFile(filename);
            if (roleHierarchy == null) {
                System.out.println("Edit the file and press <enter> to " +
                    "continue.");
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (roleHierarchy == null);

        System.out.println("\nRole Hierarchy:");
        roleHierarchy.printAscendantRelationships();
        return roleHierarchy;
    }

    private static RoleHierarchy readRolesFromFile (String filename) {
        RoleHierarchy rh = new RoleHierarchy();
        File file = new File(filename);
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.err.printf("The role hierarchy file, %s, does not exist.%n",
                filename);
            return null;
        }

        int line = 1;
        while (input.hasNextLine()) {
            String[] roles = input.nextLine().split("\\s+");
            if (roles.length > 0) {
                boolean added = rh.addRelationship(new RbacRole(roles[0]),
                    new RbacRole(roles[1]));
                if (!added) {
                    System.out.printf("Invalid line found in %s on line " +
                        "%d%n", filename, line);
                    input.close();
                    return null;
                }
            }
            line++;
        }

        input.close();

        return rh;
    }

    /**
     * Adds a new relationship to the hierarchy. Adding either role as needed.
     *
     * @param ascendant  The ascendant role. Lends it's permissions to
     *                   descendant
     * @param descendant The descendant role. Inherits permissions from
     *                   ascendant.
     * @return True if relationship is added. False if ascendant already has
     * a descendant.
     */
    public boolean addRelationship (RbacRole ascendant, RbacRole descendant) {
        if (descendants.get(ascendant) != null) {
            return false;
        }

        descendants.put(ascendant, descendant);
        ascendants.computeIfAbsent(descendant, k -> new HashSet<>());
        ascendants.get(descendant).add(ascendant);

        roles.add(ascendant);
        roles.add(descendant);

        return true;
    }

    /**
     * Gives a set of all roles in this hierarchy.
     *
     * @return The set of all roles.
     */
    public Set<RbacRole> getAllRoles () {
        return this.roles;
    }

    /**
     * Gives the descendant of a given role, if any.
     *
     * @param role The ascendant role.
     * @return The descendant role, or {@code null} if no descendant.
     */
    public RbacRole getDescendant (RbacRole role) {
        return descendants.get(role);
    }

    /**
     * Prints roles' ascendants from the top of the hierarchy. If role has no
     * ascendants it will only appears as another's ascendant.
     */
    public void printAscendantRelationships () {
        List<RbacRole> current = new ArrayList<>(getRootRoles());
        current.sort(RBAC_COMPARATOR);
        List<RbacRole> next = new ArrayList<>();

        while (current.size() > 0) {
            for (RbacRole role : current) {
                Set<RbacRole> ascendantsSet = this.ascendants.get(role);
                if (ascendantsSet == null) {
                    continue;
                }
                List<RbacRole> ascendants = new ArrayList<>(ascendantsSet);
                ascendants.sort(RBAC_COMPARATOR);
                if (ascendants.size() > 0) {
                    System.out.printf("%s ---> ", role);
                    for (int i = 0; i < ascendants.size(); i++) {
                        next.add(ascendants.get(i));
                        System.out.print(ascendants.get(i));
                        if (i + 1 != ascendants.size()) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println();
                }
            }
            current = next;
            current.sort(RBAC_COMPARATOR);
            next = new ArrayList<>();
        }
    }

    /**
     * Gives a copy of this RoleHierarchy.
     *
     * @return A copy of this RoleHierarchy.
     */
    public RoleHierarchy getCopy () {
        RoleHierarchy copy = new RoleHierarchy();
        for (RbacRole role : this.roles) {
            RbacRole descendant = descendants.get(role);
            if (descendant != null) {
                copy.addRelationship(role, descendant);
            }
        }
        return copy;
    }

    /**
     * Gives the root of every tree in the forest represented by this
     * hierarchy. Used to help print hierarchy in row order (all roots on
     * same row.)
     *
     * @return The set of tree roots.
     */
    private Set<RbacRole> getRootRoles () {
        return this.roles.stream()
            .filter(role -> this.descendants.get(role) == null)
            .collect(Collectors.toSet());
    }
}
