public abstract class RbacElement {
    public String rbacType;
    public int number;

    public RbacElement (String name) {
        rbacType = name.replaceAll("\\d+", "");
        number = Integer.parseInt(name.replaceAll("\\D+", ""));
    }

    public RbacElement (RbacElement other) {
        this.rbacType = other.rbacType;
        this.number = other.number;
    }

    @Override
    public String toString () {
        return rbacType + number;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RbacElement)) {
            return false;
        }

        RbacElement that = (RbacElement)o;

        return this.rbacType.equals(that.rbacType) && number == that.number;
    }

    @Override
    public int hashCode () {
        return this.toString().hashCode();
    }
}
