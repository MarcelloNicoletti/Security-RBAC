import java.io.IOException;
import java.util.Set;

public class Main {

    public static void main (String[] args) throws IOException {
        RoleObjectMatrix roleObjectMatrix = initializeRoleObjectMatrix();
        UserRoleMatrix userRoleMatrix = initializeUserRoleMatrix();

        RbacController controller = new RbacController(roleObjectMatrix,
            userRoleMatrix);
    }

    private static RoleObjectMatrix initializeRoleObjectMatrix () {
        RoleHierarchy roleHierarchy =
            RoleHierarchy.getRoleHierarchyFromFile("roleHierarchy.txt");
        Set<RbacObject> objects =
            RoleObjectMatrix.getResourceObjectsFromFile("resourceObjects.txt");

        RoleObjectMatrix roleObjectMatrix =
            new RoleObjectMatrix(roleHierarchy, objects);

        System.out.println("\nInitial Role-Object Matrix:");
        roleObjectMatrix.printMatrix(5);

        roleObjectMatrix.applyRoleHierarchyPermissions();
        roleObjectMatrix.applyPermissionsFromFile("permissionsToRoles.txt");

        System.out.println("\nRole-Object Matrix after adding permissions " +
            "from file:");
        roleObjectMatrix.printMatrix(5);

        return roleObjectMatrix;
    }

    private static UserRoleMatrix initializeUserRoleMatrix () {
        SsdConstraintSet constraints =
            SsdConstraintSet.getConstraintSetFromFile("roleSetsSSD.txt");
        constraints.printConstraints();

        UserRoleMatrix userRoleMatrix = new UserRoleMatrix(constraints);

        userRoleMatrix.addUsersFromFile("userRoles.txt");

        return userRoleMatrix;
    }

    public static void displayEditMessageIfNull (Object obj) {
        if (obj == null) {
            System.out.println("Edit the file and press <enter> to " +
                "continue.");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
