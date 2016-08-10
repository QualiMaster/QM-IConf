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
import pipeline.Sink;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sink</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.SinkImpl#getSink <em>Sink</em>}</li>
 *   <li>{@link pipeline.impl.SinkImpl#getPermissibleParameters <em>Permissible Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SinkImpl extends PipelineNodeImpl implements Sink
{
  /**
	 * The default value of the '{@link #getSink() <em>Sink</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getSink()
	 * @generated
	 * @ordered
	 */
    protected static final Integer SINK_EDEFAULT = new Integer(-1);
/**
	 * The cached value of the '{@link #getSink() <em>Sink</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see #getSink()
	 * @generated
	 * @ordered
	 */
    protected Integer sink = SINK_EDEFAULT;

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
  protected SinkImpl()
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
		return PipelinePackage.Literals.SINK;
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public Integer getSink() {
		return sink;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public void setSink(Integer newSink) {
		Integer oldSink = sink;
		sink = newSink;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.SINK__SINK, oldSink, sink));
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getPermissibleParameters() {
		if (permissibleParameters == null) {
			permissibleParameters = new EDataTypeUniqueEList<String>(String.class, this, PipelinePackage.SINK__PERMISSIBLE_PARAMETERS);
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
			case PipelinePackage.SINK__SINK:
				return getSink();
			case PipelinePackage.SINK__PERMISSIBLE_PARAMETERS:
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
			case PipelinePackage.SINK__SINK:
				setSink((Integer)newValue);
				return;
			case PipelinePackage.SINK__PERMISSIBLE_PARAMETERS:
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
			case PipelinePackage.SINK__SINK:
				setSink(SINK_EDEFAULT);
				return;
			case PipelinePackage.SINK__PERMISSIBLE_PARAMETERS:
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
			case PipelinePackage.SINK__SINK:
				return SINK_EDEFAULT == null ? sink != null : !SINK_EDEFAULT.equals(sink);
			case PipelinePackage.SINK__PERMISSIBLE_PARAMETERS:
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
		result.append(" (sink: ");
		result.append(sink);
		result.append(", permissibleParameters: ");
		result.append(permissibleParameters);
		result.append(')');
		return result.toString();
	}

} //SinkImpl
