/**
 */
package pipeline.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import pipeline.PipelineNode;
import pipeline.PipelinePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.PipelineNodeImpl#getParallelism <em>Parallelism</em>}</li>
 *   <li>{@link pipeline.impl.PipelineNodeImpl#getNumtasks <em>Numtasks</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class PipelineNodeImpl extends PipelineElementImpl implements PipelineNode {
    /**
	 * The default value of the '{@link #getParallelism() <em>Parallelism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParallelism()
	 * @generated
	 * @ordered
	 */
	protected static final Integer PARALLELISM_EDEFAULT = new Integer(1);
	/**
	 * The cached value of the '{@link #getParallelism() <em>Parallelism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParallelism()
	 * @generated
	 * @ordered
	 */
	protected Integer parallelism = PARALLELISM_EDEFAULT;

				/**
	 * The default value of the '{@link #getNumtasks() <em>Numtasks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumtasks()
	 * @generated
	 * @ordered
	 */
	protected static final Integer NUMTASKS_EDEFAULT = new Integer(0);
	/**
	 * The cached value of the '{@link #getNumtasks() <em>Numtasks</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumtasks()
	 * @generated
	 * @ordered
	 */
	protected Integer numtasks = NUMTASKS_EDEFAULT;
				/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    protected PipelineNodeImpl() {
		super();
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    protected EClass eStaticClass() {
		return PipelinePackage.Literals.PIPELINE_NODE;
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Integer getParallelism() {
		return parallelism;
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParallelism(Integer newParallelism) {
		Integer oldParallelism = parallelism;
		parallelism = newParallelism;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE_NODE__PARALLELISM, oldParallelism, parallelism));
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Integer getNumtasks() {
		return numtasks;
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumtasks(Integer newNumtasks) {
		Integer oldNumtasks = numtasks;
		numtasks = newNumtasks;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE_NODE__NUMTASKS, oldNumtasks, numtasks));
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PipelinePackage.PIPELINE_NODE__PARALLELISM:
				return getParallelism();
			case PipelinePackage.PIPELINE_NODE__NUMTASKS:
				return getNumtasks();
		}
		return super.eGet(featureID, resolve, coreType);
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PipelinePackage.PIPELINE_NODE__PARALLELISM:
				setParallelism((Integer)newValue);
				return;
			case PipelinePackage.PIPELINE_NODE__NUMTASKS:
				setNumtasks((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PipelinePackage.PIPELINE_NODE__PARALLELISM:
				setParallelism(PARALLELISM_EDEFAULT);
				return;
			case PipelinePackage.PIPELINE_NODE__NUMTASKS:
				setNumtasks(NUMTASKS_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PipelinePackage.PIPELINE_NODE__PARALLELISM:
				return PARALLELISM_EDEFAULT == null ? parallelism != null : !PARALLELISM_EDEFAULT.equals(parallelism);
			case PipelinePackage.PIPELINE_NODE__NUMTASKS:
				return NUMTASKS_EDEFAULT == null ? numtasks != null : !NUMTASKS_EDEFAULT.equals(numtasks);
		}
		return super.eIsSet(featureID);
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (parallelism: ");
		result.append(parallelism);
		result.append(", numtasks: ");
		result.append(numtasks);
		result.append(')');
		return result.toString();
	}

} //PipelineNodeImpl
