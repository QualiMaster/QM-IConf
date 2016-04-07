/**
 */
package pipeline.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import pipeline.DataManagementElement;
import pipeline.PipelinePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Management Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.DataManagementElementImpl#getDataManagement <em>Data Management</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DataManagementElementImpl extends ProcessingElementImpl implements DataManagementElement {
    /**
	 * The default value of the '{@link #getDataManagement() <em>Data Management</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getDataManagement()
	 * @generated
	 * @ordered
	 */
    protected static final Integer DATA_MANAGEMENT_EDEFAULT = new Integer(-1);

    /**
	 * The cached value of the '{@link #getDataManagement() <em>Data Management</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getDataManagement()
	 * @generated
	 * @ordered
	 */
    protected Integer dataManagement = DATA_MANAGEMENT_EDEFAULT;

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    protected DataManagementElementImpl() {
		super();
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    protected EClass eStaticClass() {
		return PipelinePackage.Literals.DATA_MANAGEMENT_ELEMENT;
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public Integer getDataManagement() {
		return dataManagement;
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public void setDataManagement(Integer newDataManagement) {
		Integer oldDataManagement = dataManagement;
		dataManagement = newDataManagement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT, oldDataManagement, dataManagement));
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PipelinePackage.DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT:
				return getDataManagement();
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
			case PipelinePackage.DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT:
				setDataManagement((Integer)newValue);
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
			case PipelinePackage.DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT:
				setDataManagement(DATA_MANAGEMENT_EDEFAULT);
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
			case PipelinePackage.DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT:
				return DATA_MANAGEMENT_EDEFAULT == null ? dataManagement != null : !DATA_MANAGEMENT_EDEFAULT.equals(dataManagement);
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
		result.append(" (dataManagement: ");
		result.append(dataManagement);
		result.append(')');
		return result.toString();
	}

} //DataManagementElementImpl
