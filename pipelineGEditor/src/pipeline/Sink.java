/**
 */
package pipeline;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sink</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.Sink#getSink <em>Sink</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getSink()
 * @model annotation="gmf.node figure='svg' svg.uri='platform:/plugin/pipelineGEditor/svg/sink.svg' size='55,55'"
 * @generated
 */
public interface Sink extends PipelineNode
{
  /**
	 * Returns the value of the '<em><b>Sink</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sink</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Sink</em>' attribute.
	 * @see #setSink(Integer)
	 * @see pipeline.PipelinePackage#getSink_Sink()
	 * @model default="-1"
	 * @generated
	 */
    Integer getSink();

/**
	 * Sets the value of the '{@link pipeline.Sink#getSink <em>Sink</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sink</em>' attribute.
	 * @see #getSink()
	 * @generated
	 */
    void setSink(Integer value);

} // Sink
