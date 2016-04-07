/**
 */
package pipeline;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Family Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.FamilyElement#getFamily <em>Family</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getFamilyElement()
 * @model annotation="gmf.node figure='svg' svg.uri='platform:/plugin/pipelineGEditor/svg/familyelement.svg' size='60,60'"
 * @generated
 */
public interface FamilyElement extends ProcessingElement {
    /**
	 * Returns the value of the '<em><b>Family</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Family</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Family</em>' attribute.
	 * @see #setFamily(Integer)
	 * @see pipeline.PipelinePackage#getFamilyElement_Family()
	 * @model default="-1"
	 * @generated
	 */
    Integer getFamily();

    /**
	 * Sets the value of the '{@link pipeline.FamilyElement#getFamily <em>Family</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Family</em>' attribute.
	 * @see #getFamily()
	 * @generated
	 */
    void setFamily(Integer value);

} // FamilyElement
