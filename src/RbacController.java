import java.util.HashSet;
import java.util.Set;

public class RbacController {
    private RoleObjectMatrix roleObjectMatrix;
    private UserRoleMatrix userRoleMatrix;

    public RbacController (RoleObjectMatrix roleObjectMatrix, UserRoleMatrix
        userRoleMatrix) {
        this.roleObjectMatrix = roleObjectMatrix;
        this.userRoleMatrix = userRoleMatrix;
    }

    public boolean query (RbacUser user, RbacObject object,
        RbacPermission permission) {
        if (!userRoleMatrix.getUsers().contains(user)) {
            System.out.println("Invalid user.");
            return false;
        }
        Set<RbacRole> userRoles = userRoleMatrix.getRoles(user);

        Set<RbacObject> objects = new HashSet<>();
        if (object == null) {
            objects.addAll(roleObjectMatrix.getObjects());
        } else {
            if (!roleObjectMatrix.getObjects().contains(object)) {
                System.out.println("Invalid object.");
                return false;
            }
            objects.add(object);
        }

        boolean returnValue = true;
        for (RbacObject queryObject : objects) {
            Set<RbacPermission> objectPermissions = new HashSet<>();
            for (RbacRole queryRole : userRoles) {
                Set<RbacPermission> objectPermissionsForRole = roleObjectMatrix
                    .getObjectPermissionsForRole(queryRole, queryObject);
                objectPermissions.addAll(objectPermissionsForRole);
            }

            if (permission != null) {
                if (!objectPermissions.contains(permission)) {
                    returnValue = false;
                }
            } else {
                if (objectPermissions.size() > 0) {
                    System.out.printf("%s\t", queryObject.toString());
                    objectPermissions.stream().limit(1).forEach(x -> System.out
                        .print(x.toString()));
                    objectPermissions.stream().skip(1).forEach(x -> System.out
                        .printf(", %s", x.toString()));
                    System.out.println();
                }
            }
        }

        if (permission != null) {
            if (returnValue) {
                System.out.println("Accepted");
            } else {
                System.out.println("Rejected");
            }
        }

        return returnValue;
    }
}
