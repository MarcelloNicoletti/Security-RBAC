import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main (String[] args) throws IOException {
        RoleObjectMatrix roleObjectMatrix = initializeRoleObjectMatrix();
    }

    private static RoleObjectMatrix initializeRoleObjectMatrix () throws
            IOException {
        RoleHierarchy roleHierarchy = getRoleHierarchy();
        Set<RbacObject> objects = getResourceObjects();

        RoleObjectMatrix roleObjectMatrix =
                new RoleObjectMatrix(roleHierarchy, objects);

        System.out.println("\nInitial Role-Object Matrix:");
        roleObjectMatrix.printMatrix(5);

        applyRoleHierarchyPermissions(roleObjectMatrix);
        applyFilePermissions("permissionsToRoles.txt", roleObjectMatrix);

        System.out.println("\nRole-Object Matrix after adding permissions " +
                "from file:");
        roleObjectMatrix.printMatrix(5);

        return roleObjectMatrix;
    }

    private static void applyFilePermissions (String filename,
            RoleObjectMatrix roleObjectMatrix) {
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
            roleObjectMatrix.addPermission(new RbacRole(row[0]),
                    new RbacObject(row[2]), new RbacPermission(row[1]));
        }
    }

    private static void applyRoleHierarchyPermissions (
            RoleObjectMatrix roleObjectMatrix) {
        for (RbacRole role : roleObjectMatrix.getRoles()) {
            RbacObject roleAsObject = new RbacObject(role);
            RbacPermission permission = new RbacPermission("control");
            roleObjectMatrix.addObject(roleAsObject);
            roleObjectMatrix.addPermission(role, roleAsObject, permission);
        }
        for (RbacRole role : roleObjectMatrix.getRoles()) {
            RbacRole descendant =
                    roleObjectMatrix.getRoleHierarchy().getDescendant(role);
            RbacObject roleAsObject = new RbacObject(role);
            RbacPermission permission = new RbacPermission("own");
            if (descendant != null) {
                roleObjectMatrix
                        .addPermission(descendant, roleAsObject, permission);
            }
        }
    }

    private static RoleHierarchy getRoleHierarchy () throws IOException {
        RoleHierarchy roleHierarchy;
        do {
            roleHierarchy = readRolesFromFile("roleHierarchy.txt");
            if (roleHierarchy == null) {
                System.out.println("Edit the file and press <enter> to " +
                        "continue.");
                System.in.read();
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

    private static Set<RbacObject> getResourceObjects () throws IOException {
        Set<RbacObject> objects;
        do {
            objects = readObjectsFromFile("resourceObjects.txt");
            if (objects == null) {
                System.out.println("Edit the file and press <enter> to " +
                        "continue.");
                System.in.read();
            }
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
}
