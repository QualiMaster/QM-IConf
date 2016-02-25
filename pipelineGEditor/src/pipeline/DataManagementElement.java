/**
 */
package pipeline;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Management Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.DataManagementElement#getDataManagement <em>Data Management</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getDataManagementElement()
 * @model annotation="gmf.node figure='svg' svg.uri='platform:/plugin/pipelineGEditor/svg/datamanagement.svg' size='60,60'"
 * @generated
 */
public interface DataManagementElement extends ProcessingElement {
    /**
     * Returns the value of the '<em><b>Data Management</b></em>' attribute.
     * The default value is <code>"-1"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Data Management</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Data Management</em>' attribute.
     * @see #setDataManagement(Integer)
     * @see pipeline.PipelinePackage#getDataManagementElement_DataManagement()
     * @model default="-1"
     * @generated
     */
    Integer getDataManagement();

    /**
     * Sets the value of the '{@link pipeline.DataManagementElement#getDataManagement <em>Data Management</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Data Management</em>' attribute.
     * @see #getDataManagement()
     * @generated
     */
    void setDataManagement(Integer value);

} // DataManagementElement
