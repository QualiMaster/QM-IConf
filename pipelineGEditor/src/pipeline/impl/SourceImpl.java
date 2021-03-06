/**
 */
package pipeline.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import pipeline.Flow;
import pipeline.PipelinePackage;
import pipeline.Source;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Source</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.SourceImpl#getSource <em>Source</em>}</li>
 *   <li>{@link pipeline.impl.SourceImpl#getPermissibleParameters <em>Permissible Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SourceImpl extends PipelineNodeImpl implements Source
{
  /**
	 * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
    protected static final Integer SOURCE_EDEFAULT = new Integer(-1);

/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
    protected Integer source = SOURCE_EDEFAULT;

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
  protected SourceImpl()
  {
		super();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  protected EClass eStaticClass()
  {
		return PipelinePackage.Literals.SOURCE;
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public Integer getSource() {
		return source;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public void setSource(Integer newSource) {
		Integer oldSource = source;
		source = newSource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.SOURCE__SOURCE, oldSource, source));
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getPermissibleParameters() {
		if (permissibleParameters == null) {
			permissibleParameters = new EDataTypeUniqueEList<String>(String.class, this, PipelinePackage.SOURCE__PERMISSIBLE_PARAMETERS);
		}
		return permissibleParameters;
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
		switch (featureID) {
			case PipelinePackage.SOURCE__SOURCE:
				return getSource();
			case PipelinePackage.SOURCE__PERMISSIBLE_PARAMETERS:
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
  public void eSet(int featureID, Object newValue)
  {
		switch (featureID) {
			case PipelinePackage.SOURCE__SOURCE:
				setSource((Integer)newValue);
				return;
			case PipelinePackage.SOURCE__PERMISSIBLE_PARAMETERS:
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
  public void eUnset(int featureID)
  {
		switch (featureID) {
			case PipelinePackage.SOURCE__SOURCE:
				setSource(SOURCE_EDEFAULT);
				return;
			case PipelinePackage.SOURCE__PERMISSIBLE_PARAMETERS:
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
  public boolean eIsSet(int featureID)
  {
		switch (featureID) {
			case PipelinePackage.SOURCE__SOURCE:
				return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
			case PipelinePackage.SOURCE__PERMISSIBLE_PARAMETERS:
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
		result.append(" (source: ");
		result.append(source);
		result.append(", permissibleParameters: ");
		result.append(permissibleParameters);
		result.append(')');
		return result.toString();
	}

} //SourceImpl
