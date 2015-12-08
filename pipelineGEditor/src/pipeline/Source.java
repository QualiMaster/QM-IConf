/**
 */
package pipeline;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.Source#getSource <em>Source</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getSource()
 * @model annotation="gmf.node figure='svg' svg.uri='platform:/plugin/pipelineGEditor/svg/source.svg' size='60,60'"
 * @generated
 */
public interface Source extends PipelineNode
{
  /**
     * Returns the value of the '<em><b>Source</b></em>' attribute.
     * The default value is <code>"-1"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Source</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Source</em>' attribute.
     * @see #setSource(Integer)
     * @see pipeline.PipelinePackage#getSource_Source()
     * @model default="-1"
     * @generated
     */
    Integer getSource();

/**
     * Sets the value of the '{@link pipeline.Source#getSource <em>Source</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Source</em>' attribute.
     * @see #getSource()
     * @generated
     */
    void setSource(Integer value);

} // Source
