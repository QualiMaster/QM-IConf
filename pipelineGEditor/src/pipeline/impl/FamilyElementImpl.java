/**
 */
package pipeline.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import pipeline.FamilyElement;
import pipeline.PipelinePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Family Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.FamilyElementImpl#getFamily <em>Family</em>}</li>
 *   <li>{@link pipeline.impl.FamilyElementImpl#getIsConnector <em>Is Connector</em>}</li>
 *   <li>{@link pipeline.impl.FamilyElementImpl#getDefault <em>Default</em>}</li>
 *   <li>{@link pipeline.impl.FamilyElementImpl#getPermissibleParameters <em>Permissible Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FamilyElementImpl extends ProcessingElementImpl implements FamilyElement {
    /**
	 * The default value of the '{@link #getFamily() <em>Family</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getFamily()
	 * @generated
	 * @ordered
	 */
    protected static final Integer FAMILY_EDEFAULT = new Integer(-1);

    /**
	 * The cached value of the '{@link #getFamily() <em>Family</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getFamily()
	 * @generated
	 * @ordered
	 */
    protected Integer family = FAMILY_EDEFAULT;

    /**
	 * The default value of the '{@link #getIsConnector() <em>Is Connector</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsConnector()
	 * @generated
	 * @ordered
	 */
	protected static final Boolean IS_CONNECTOR_EDEFAULT = Boolean.FALSE;

				/**
	 * The cached value of the '{@link #getIsConnector() <em>Is Connector</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsConnector()
	 * @generated
	 * @ordered
	 */
	protected Boolean isConnector = IS_CONNECTOR_EDEFAULT;

				/**
	 * The default value of the '{@link #getDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefault()
	 * @generated
	 * @ordered
	 */
	protected static final String DEFAULT_EDEFAULT = null;

				/**
	 * The cached value of the '{@link #getDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefault()
	 * @generated
	 * @ordered
	 */
	protected String default_ = DEFAULT_EDEFAULT;

				/**
	 * The cached value of the '{@link #getPermissibleParameters() <em>Permissible Parameters</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPermissibleParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<String> permissibleParameters;

				/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    protected FamilyElementImpl() {
		super();
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    protected EClass eStaticClass() {
		return PipelinePackage.Literals.FAMILY_ELEMENT;
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public Integer getFamily() {
		return family;
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public void setFamily(Integer newFamily) {
		Integer oldFamily = family;
		family = newFamily;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FAMILY_ELEMENT__FAMILY, oldFamily, family));
	}

    /**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Boolean getIsConnector() {
		return isConnector;
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsConnector(Boolean newIsConnector) {
		Boolean oldIsConnector = isConnector;
		isConnector = newIsConnector;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FAMILY_ELEMENT__IS_CONNECTOR, oldIsConnector, isConnector));
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDefault() {
		return default_;
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefault(String newDefault) {
		String oldDefault = default_;
		default_ = newDefault;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FAMILY_ELEMENT__DEFAULT, oldDefault, default_));
	}

				/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getPermissibleParameters() {
		if (permissibleParameters == null) {
			permissibleParameters = new EDataTypeUniqueEList<String>(String.class, this, PipelinePackage.FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS);
		}
		return permissibleParameters;
	}

				/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PipelinePackage.FAMILY_ELEMENT__FAMILY:
				return getFamily();
			case PipelinePackage.FAMILY_ELEMENT__IS_CONNECTOR:
				return getIsConnector();
			case PipelinePackage.FAMILY_ELEMENT__DEFAULT:
				return getDefault();
			case PipelinePackage.FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS:
				return getPermissibleParameters();
		}
		return super.eGet(featureID, resolve, coreType);
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    @SuppressWarnings("unchecked")
				@Override
    public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PipelinePackage.FAMILY_ELEMENT__FAMILY:
				setFamily((Integer)newValue);
				return;
			case PipelinePackage.FAMILY_ELEMENT__IS_CONNECTOR:
				setIsConnector((Boolean)newValue);
				return;
			case PipelinePackage.FAMILY_ELEMENT__DEFAULT:
				setDefault((String)newValue);
				return;
			case PipelinePackage.FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS:
				getPermissibleParameters().clear();
				getPermissibleParameters().addAll((Collection<? extends String>)newValue);
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
			case PipelinePackage.FAMILY_ELEMENT__FAMILY:
				setFamily(FAMILY_EDEFAULT);
				return;
			case PipelinePackage.FAMILY_ELEMENT__IS_CONNECTOR:
				setIsConnector(IS_CONNECTOR_EDEFAULT);
				return;
			case PipelinePackage.FAMILY_ELEMENT__DEFAULT:
				setDefault(DEFAULT_EDEFAULT);
				return;
			case PipelinePackage.FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS:
				getPermissibleParameters().clear();
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
			case PipelinePackage.FAMILY_ELEMENT__FAMILY:
				return FAMILY_EDEFAULT == null ? family != null : !FAMILY_EDEFAULT.equals(family);
			case PipelinePackage.FAMILY_ELEMENT__IS_CONNECTOR:
				return IS_CONNECTOR_EDEFAULT == null ? isConnector != null : !IS_CONNECTOR_EDEFAULT.equals(isConnector);
			case PipelinePackage.FAMILY_ELEMENT__DEFAULT:
				return DEFAULT_EDEFAULT == null ? default_ != null : !DEFAULT_EDEFAULT.equals(default_);
			case PipelinePackage.FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS:
				return permissibleParameters != null && !permissibleParameters.isEmpty();
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
		result.append(" (family: ");
		result.append(family);
		result.append(", isConnector: ");
		result.append(isConnector);
		result.append(", default: ");
		result.append(default_);
		result.append(", permissibleParameters: ");
		result.append(permissibleParameters);
		result.append(')');
		return result.toString();
	}

} //FamilyElementImpl
