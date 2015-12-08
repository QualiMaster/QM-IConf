package de.uni_hildesheim.sse.qmApp.model;

/**
 * Constraint Utils.
 */
public class ConstraintUtils {

    /**
     * Splits the (possibly multiple) <code>constraints</code> into individual strings.
     * 
     * @param constraints the constraints to split (IVML syntax, separated by {@link PipelineDiagramUtils#SEPARATOR})
     * @return the individual constraints as array
     */
    public static String[] splitConstraints(String constraints) {
        return constraints.split(PipelineDiagramUtils.CONSTRAINT_SEPARATOR);
    }

    /**
     * Combines individual constraints given as Strings in IVML syntax.
     * 
     * @param constraints the constraints to be combined 
     * @return the combined constraints string (using {@link PipelineDiagramUtils#SEPARATOR} as separator)
     */
    public static String combineConstraints(String[] constraints) {
        StringBuilder tmp = new StringBuilder();
        for (int c = 0; c < constraints.length; c++) {
            String constraint = constraints[c].trim();
            if (constraint.length() > 0) {
                if (tmp.length() > 0) {
                    tmp.append(PipelineDiagramUtils.CONSTRAINT_SEPARATOR);
                }
                tmp.append(constraint);
            }
        }
        return tmp.toString();
    }

}
