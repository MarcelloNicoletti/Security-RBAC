public abstract class RbacElement {
    public String name;
    public int number;

    public RbacElement (String name) {
        this.name = name.replaceAll("\\d+", "");
        String numStr = name.replaceAll("\\D+", "");
        if (numStr.length() > 0) {
            number = Integer.parseInt(numStr);
        } else {
            number = -1;
        }
    }

    public RbacElement (RbacElement other) {
        this.name = other.name;
        this.number = other.number;
    }

    @Override
    public String toString () {
        return number < 0 ? name : name + number;
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

        return this.name.equals(that.name) && ((this.number < 0 && that
                .number < 0) || this.number == that.number);
    }

    @Override
    public int hashCode () {
        return this.toString().hashCode();
    }
}
