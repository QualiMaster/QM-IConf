/**
 */
package pipeline;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Flow</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.Flow#getSource <em>Source</em>}</li>
 *   <li>{@link pipeline.Flow#getDestination <em>Destination</em>}</li>
 *   <li>{@link pipeline.Flow#getGrouping <em>Grouping</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getFlow()
 * @model annotation="gmf.link label='name' source='source' target='destination' target.decoration='arrow'"
 * @generated
 */
public interface Flow extends PipelineElement
{
  /**
     * Returns the value of the '<em><b>Source</b></em>' reference.
     * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Source</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
     * @return the value of the '<em>Source</em>' reference.
     * @see #setSource(PipelineElement)
     * @see pipeline.PipelinePackage#getFlow_Source()
     * @model
     * @generated
     */
  PipelineElement getSource();

  /**
     * Sets the value of the '{@link pipeline.Flow#getSource <em>Source</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Source</em>' reference.
     * @see #getSource()
     * @generated
     */
    void setSource(PipelineElement value);

/**
     * Returns the value of the '<em><b>Destination</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Destination</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Destination</em>' reference.
     * @see #setDestination(PipelineElement)
     * @see pipeline.PipelinePackage#getFlow_Destination()
     * @model
     * @generated
     */
    PipelineElement getDestination();

/**
     * Sets the value of the '{@link pipeline.Flow#getDestination <em>Destination</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Destination</em>' reference.
     * @see #getDestination()
     * @generated
     */
    void setDestination(PipelineElement value);

/**
     * Returns the value of the '<em><b>Grouping</b></em>' attribute.
     * The default value is <code>"0"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Grouping</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Grouping</em>' attribute.
     * @see #setGrouping(Integer)
     * @see pipeline.PipelinePackage#getFlow_Grouping()
     * @model default="0"
     * @generated
     */
    Integer getGrouping();

/**
     * Sets the value of the '{@link pipeline.Flow#getGrouping <em>Grouping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Grouping</em>' attribute.
     * @see #getGrouping()
     * @generated
     */
    void setGrouping(Integer value);

} // Flow
