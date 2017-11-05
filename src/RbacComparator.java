import java.util.Comparator;

/**
 * A comparator for comparing the string representing RBAC elements. These
 * are in the format XY where X is one or more letters, denoting type, and Y
 * is a number. Ordering these elements should be by type then by number,
 * numerically, when type is the same.
 */
public class RbacComparator implements Comparator<RbacElement> {
    /**
     * Compares two RBAC elements. Compares by type, and if type is the same
     * then by number. Returns negative, zero, or positive if o1 is less
     * than, equal to, or greater than o2.
     *
     * @param o1 The first RBAC element.
     * @param o2 The second RBAC element.
     * @return Negative if o1 is less than o2, zero is o1 is equal to o2, or
     * positive if o1 is greater than o2.
     */
    @Override
    public int compare (RbacElement o1, RbacElement o2) {
//        String letters1 = o1.replaceAll("\\d+", "");
//        String letters2 = o2.replaceAll("\\d+", "");
//        if (letters1.compareTo(letters2) != 0) {
//            return letters1.compareTo(letters2);
//        } else {
//            int number1 = Integer.parseInt(o1.replaceAll("\\D+", ""));
//            int number2 = Integer.parseInt(o2.replaceAll("\\D+", ""));
//            return number1 - number2;
//        }
        if (o1.rbacType.compareTo(o2.rbacType) == 0) {
            return o1.number - o2.number;
        } else {
            return o1.rbacType.compareTo(o2.rbacType);
        }
    }
}
