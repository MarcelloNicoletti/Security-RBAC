public class RbacController {
    private RoleObjectMatrix roleObjectMatrix;
    private UserRoleMatrix userRoleMatrix;

    public RbacController (RoleObjectMatrix roleObjectMatrix, UserRoleMatrix
        userRoleMatrix) {
        this.roleObjectMatrix = roleObjectMatrix;
        this.userRoleMatrix = userRoleMatrix;
    }
}
