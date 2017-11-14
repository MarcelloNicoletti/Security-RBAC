import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main (String[] args) throws IOException {
        RoleObjectMatrix roleObjectMatrix = initializeRoleObjectMatrix();
        Set<RbacRole> roles = roleObjectMatrix.getRoles();
        UserRoleMatrix userRoleMatrix = initializeUserRoleMatrix(roles);

        RbacController controller = new RbacController(roleObjectMatrix,
            userRoleMatrix);

        doQueryLoop(controller);
    }

    private static void doQueryLoop (RbacController controller) {
        boolean doQuery = true;
        Scanner in = new Scanner(System.in);
        while (doQuery) {
            System.out.print("\nPlease enter the user for query: ");
            RbacUser user = new RbacUser(in.nextLine());

            System.out.print("Enter object to query (empty to get all): ");
            String objString = in.nextLine();
            RbacObject object = new RbacObject(objString);
            if (objString.isEmpty()) {
                object = null;
            }

            System.out.print("Enter object to query (empty to get all): ");
            String rightString = in.nextLine();
            RbacPermission permission = new RbacPermission(rightString);
            if (rightString.isEmpty()) {
                permission = null;
            }

            boolean hasRight = controller.query(user, object, permission);

            System.out
                .println("Would you like to continue for the next query? ");
            String answer = in.nextLine();
            doQuery = answer.charAt(0) == 'y' || answer.charAt(0) == 'Y';
        }
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

    private static UserRoleMatrix initializeUserRoleMatrix (Set<RbacRole>
        roles) {
        SsdConstraintSet constraints =
            SsdConstraintSet.getConstraintSetFromFile("roleSetsSSD.txt");
        constraints.printConstraints();

        UserRoleMatrix userRoleMatrix = new UserRoleMatrix(constraints, roles);

        userRoleMatrix.addUsersFromFile("userRoles.txt");

        System.out.println("\nUser-Role matrix");
        userRoleMatrix.printMatrix();

        return userRoleMatrix;
    }
}
