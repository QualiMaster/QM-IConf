/**
 */
package pipeline;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.PipelineElement#getName <em>Name</em>}</li>
 *   <li>{@link pipeline.PipelineElement#getConstraints <em>Constraints</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getPipelineElement()
 * @model abstract="true"
 *        annotation="gmf.node label='name' label.icon='false' figure='svg' label.placement='external'"
 * @generated
 */
public interface PipelineElement extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * The default value is <code>""</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see pipeline.PipelinePackage#getPipelineElement_Name()
     * @model default=""
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link pipeline.PipelineElement#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Constraints</b></em>' attribute.
     * The default value is <code>""</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Constraints</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Constraints</em>' attribute.
     * @see #setConstraints(String)
     * @see pipeline.PipelinePackage#getPipelineElement_Constraints()
     * @model default=""
     * @generated
     */
    String getConstraints();

    /**
     * Sets the value of the '{@link pipeline.PipelineElement#getConstraints <em>Constraints</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Constraints</em>' attribute.
     * @see #getConstraints()
     * @generated
     */
    void setConstraints(String value);

} // PipelineElement
