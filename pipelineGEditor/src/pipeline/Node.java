/**
 */
package pipeline;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.Node#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getNode()
 * @model abstract="true"
 *        annotation="gmf.node label='name' label.icon='false' figure='svg' label.placement='external'"
 * @generated
 */
public interface Node extends EObject
{
  /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see pipeline.PipelinePackage#getNode_Name()
     * @model
     * @generated
     */
  String getName();

  /**
     * Sets the value of the '{@link pipeline.Node#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
  void setName(String value);

} // Node
