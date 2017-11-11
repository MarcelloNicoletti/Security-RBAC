import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Acts as the access matrix giving roles' rights to objects. Built from, and
 * maintaining, a role hierarchy.
 */
public class RoleObjectMatrix {
    private static final int TERM_WIDTH = 80;
    private static final RbacComparator RBAC_COMPARATOR = new RbacComparator();

    private Map<RbacRole, Map<RbacObject, Map<RbacPermission, Set<RbacRole>>>>
        matrix;
    private Set<RbacObject> objects;
    private RoleHierarchy roleHierarchy;

    /**
     * Constructs a new Role-Object Matrix from copies of a RoleHierarchy and
     * a set of objects.
     *
     * @param roleHierarchy The originating RoleHierarchy
     * @param objects       The set of objects for the system.
     */
    public RoleObjectMatrix (RoleHierarchy roleHierarchy,
        Set<RbacObject> objects) {
        this.objects = new HashSet<>(objects);
        this.roleHierarchy = roleHierarchy.getCopy();

        matrix = new HashMap<>();
        for (RbacRole role : this.roleHierarchy.getAllRoles()) {
            Map<RbacObject, Map<RbacPermission, Set<RbacRole>>> domain = new
                HashMap<>();
            for (RbacObject object : this.objects) {
                domain.put(object, new HashMap<>());
            }
            matrix.put(role, domain);
        }
    }

    static Set<RbacObject> getResourceObjectsFromFile (
        String filename) {
        Set<RbacObject> objects;
        do {
            objects = readObjectsFromFile(filename);
            Main.displayEditMessageIfNull(objects);
        } while (objects == null);
        return objects;
    }

    private static Set<RbacObject> readObjectsFromFile (String filename) {
        Set<RbacObject> objects = new HashSet<>();
        File file = new File(filename);
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.err.printf("The resource objects file, %s, does not exist.",
                filename);
            return null;
        }

        String[] rawObjects = input.nextLine().split("\\s+");
        for (String rawObject : rawObjects) {
            RbacObject rbacObject = new RbacObject(rawObject);
            if (objects.contains(rbacObject)) {
                System.out.printf("Duplicate object found: %s", rawObject);
                return null;
            }
            objects.add(rbacObject);
        }

        return objects;
    }

    public void applyPermissionsFromFile (String filename) {
        File permissionsFile = new File(filename);
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream(permissionsFile));
        } catch (FileNotFoundException e) {
            System.err.printf("Permissions file %s not found.", filename);
            System.exit(1);
        }

        while (input.hasNextLine()) {
            String[] row = input.nextLine().split("\\s+");
            this.addPermission(new RbacRole(row[0]),
                new RbacObject(row[2]), new RbacPermission(row[1]));
        }
    }

    public void applyRoleHierarchyPermissions () {
        for (RbacRole role : this.getRoles()) {
            RbacObject roleAsObject = new RbacObject(role);
            RbacPermission permission = new RbacPermission("control");
            this.addObject(roleAsObject);
            this.addPermission(role, roleAsObject, permission);
        }
        for (RbacRole role : this.getRoles()) {
            RbacRole descendant =
                this.getRoleHierarchy().getDescendant(role);
            RbacObject roleAsObject = new RbacObject(role);
            RbacPermission permission = new RbacPermission("own");
            if (descendant != null) {
                this.addPermission(descendant, roleAsObject, permission);
            }
        }
    }

    /**
     * Adds a given permission for an object to a role and it's descendants.
     *
     * @param role       The base role to give the permission.
     * @param object     The object on which the permission is given.
     * @param permission The permission to give.
     * @return false if permission was a duplicate or role or object didn't
     * exist, true if permission successfully added.
     */
    public boolean addPermission (RbacRole role, RbacObject object,
        RbacPermission permission) {
        return (roleExists(role) && objectExists(object)) &&
            propagatePermission(role, object, permission, role);
    }

    /**
     * Prints the matrix with up to cols columns per sub-matrix.
     *
     * @param cols The maximum number of columns per sub-matrix.
     */
    public void printMatrix (int cols) {
        int numSubMatrix = (this.objects.size() / cols) + 1;
        int colWidth = TERM_WIDTH / (cols + 1);
        for (int i = 0; i < numSubMatrix; i++) {
            printSubMatrix(i * cols, ((i + 1) * cols), colWidth);
            System.out.println();
        }
    }

    /**
     * Adds an object (column) to this system with no permissions yet.
     *
     * @param object The object to add to the system.
     * @return true if the object was added, false if it was a duplicate.
     */
    public boolean addObject (RbacObject object) {
        if (!this.objects.add(object)) {
            return false;
        }
        for (RbacRole role : this.roleHierarchy.getAllRoles()) {
            matrix.get(role).put(object, new HashMap<>());
        }
        return true;
    }

    /**
     * Gives the set of roles for this system. Transparent accessor to
     * underlying RoleHierarchy.
     *
     * @return The set of roles for this system.
     */
    public Set<RbacRole> getRoles () {
        return this.roleHierarchy.getAllRoles();
    }

    /**
     * Gives the underlying RoleHierarchy.
     *
     * @return The underlying RoleHierarchy.
     */
    public RoleHierarchy getRoleHierarchy () {
        return this.roleHierarchy;
    }

    /**
     * Checks if a given role exists in the system.
     *
     * @param role The role to check.
     * @return true if the role exists, false otherwise
     */
    private boolean roleExists (RbacRole role) {
        return roleHierarchy.getAllRoles().contains(role);
    }

    /**
     * Checks if a given object exists in the system.
     *
     * @param object The object to check.
     * @return true if the object exists, false otherwise
     */
    private boolean objectExists (RbacObject object) {
        return this.objects.contains(object);
    }

    /**
     * Propagates a permission on an object to a role and its descendants,
     * maintaining the set of contributing originating roles.
     *
     * @param role       The base role to give the permission.
     * @param object     The object on which the permission is given.
     * @param permission The permission to give.
     * @param source     The originating role.
     * @return true if permission was added, false if permission was
     * previously added.
     */
    private boolean propagatePermission (RbacRole role, RbacObject object,
        RbacPermission permission, RbacRole source) {
        boolean added;
        Set<RbacRole> currentSources =
            matrix.get(role).get(object).get(permission);
        if (currentSources == null) {
            Set<RbacRole> sources = new HashSet<>();
            sources.add(source);
            matrix.get(role).get(object).put(permission, sources);
            added = true;
        } else {
            added = currentSources.add(source);
        }

        RbacRole descendant = roleHierarchy.getDescendant(role);
        if (descendant != null) {
            propagatePermission(descendant, object, permission, source);
        }
        return added;
    }

    /**
     * Prints a sub-matrix from column startCol inclusive to column endCol
     * exclusive.
     *
     * @param startCol The first column in the sub-matrix, inclusive.
     * @param endCol   The last column in the sub-matrix, exclusive.
     * @param colWidth The minimum width to print a column.
     */
    private void printSubMatrix (int startCol, int endCol, int colWidth) {
        List<RbacObject> sortedObjects = new ArrayList<>(this.objects);
        sortedObjects.sort(RBAC_COMPARATOR);
        List<RbacRole> sortedRoles =
            new ArrayList<>(this.roleHierarchy.getAllRoles());
        sortedRoles.sort(RBAC_COMPARATOR);

        endCol = Math.min(endCol, objects.size());
        System.out.printf("%" + colWidth + "s ", "\\");
        for (int i = startCol; i < endCol; i++) {
            System.out.printf("%" + colWidth + "s ", sortedObjects.get(i));
        }
        System.out.println();
        for (RbacRole role : sortedRoles) {
            System.out.printf("%" + colWidth + "s ", role);
            for (int i = startCol; i < endCol; i++) {
                RbacObject object = sortedObjects.get(i);
                Map<RbacPermission, Set<RbacRole>> permissions =
                    matrix.get(role).get(object);
                String permissionsDisplay =
                    getDisplayString(permissions.keySet());
                System.out.printf("%" + colWidth + "s ", permissionsDisplay);
            }
            System.out.println();
        }
    }

    /**
     * Give the string representation of a set of permissions.
     *
     * @param permissions The set of permissions.
     * @return The string representation of a set of permissions.
     */
    private String getDisplayString (Set<RbacPermission> permissions) {
        StringBuilder current = new StringBuilder();
        int countLeft = permissions.size();
        for (RbacPermission permission : permissions) {
            current.append(permission);
            if (--countLeft > 0) {
                current.append(", ");
            }
        }
        return current.toString();
    }
}
