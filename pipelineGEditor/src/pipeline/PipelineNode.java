/**
 */
package pipeline;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.PipelineNode#getParallelism <em>Parallelism</em>}</li>
 *   <li>{@link pipeline.PipelineNode#getNumtasks <em>Numtasks</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getPipelineNode()
 * @model abstract="true"
 * @generated
 */
public interface PipelineNode extends PipelineElement {

	/**
	 * Returns the value of the '<em><b>Parallelism</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parallelism</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parallelism</em>' attribute.
	 * @see #setParallelism(Integer)
	 * @see pipeline.PipelinePackage#getPipelineNode_Parallelism()
	 * @model default="1"
	 * @generated
	 */
	Integer getParallelism();

	/**
	 * Sets the value of the '{@link pipeline.PipelineNode#getParallelism <em>Parallelism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parallelism</em>' attribute.
	 * @see #getParallelism()
	 * @generated
	 */
	void setParallelism(Integer value);

	/**
	 * Returns the value of the '<em><b>Numtasks</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Numtasks</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Numtasks</em>' attribute.
	 * @see #setNumtasks(Integer)
	 * @see pipeline.PipelinePackage#getPipelineNode_Numtasks()
	 * @model default="1"
	 * @generated
	 */
	Integer getNumtasks();

	/**
	 * Sets the value of the '{@link pipeline.PipelineNode#getNumtasks <em>Numtasks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Numtasks</em>' attribute.
	 * @see #getNumtasks()
	 * @generated
	 */
	void setNumtasks(Integer value);
} // PipelineNode
