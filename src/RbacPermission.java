public class RbacPermission {
    String right;

    public RbacPermission (String right) {
        this.right = right;
    }

    @Override
    public String toString () {
        return right;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RbacPermission)) {
            return false;
        }

        RbacPermission that = (RbacPermission)o;

        return this.right.equals(that.right);
    }

    @Override
    public int hashCode () {
        return right.hashCode();
    }
}
