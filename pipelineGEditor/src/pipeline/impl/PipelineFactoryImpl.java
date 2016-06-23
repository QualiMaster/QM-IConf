/**
 */
package pipeline.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import pipeline.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PipelineFactoryImpl extends EFactoryImpl implements PipelineFactory
{
  /**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public static PipelineFactory init()
  {
		try {
			PipelineFactory thePipelineFactory = (PipelineFactory)EPackage.Registry.INSTANCE.getEFactory(PipelinePackage.eNS_URI);
			if (thePipelineFactory != null) {
				return thePipelineFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new PipelineFactoryImpl();
	}

  /**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public PipelineFactoryImpl()
  {
		super();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  public EObject create(EClass eClass)
  {
		switch (eClass.getClassifierID()) {
			case PipelinePackage.PIPELINE: return createPipeline();
			case PipelinePackage.FLOW: return createFlow();
			case PipelinePackage.SOURCE: return createSource();
			case PipelinePackage.SINK: return createSink();
			case PipelinePackage.REPLAY_SINK: return createReplaySink();
			case PipelinePackage.FAMILY_ELEMENT: return createFamilyElement();
			case PipelinePackage.DATA_MANAGEMENT_ELEMENT: return createDataManagementElement();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public Pipeline createPipeline()
  {
		PipelineImpl pipeline = new PipelineImpl();
		return pipeline;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public Flow createFlow()
  {
		FlowImpl flow = new FlowImpl();
		return flow;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public Source createSource()
  {
		SourceImpl source = new SourceImpl();
		return source;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public Sink createSink()
  {
		SinkImpl sink = new SinkImpl();
		return sink;
	}

  /**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReplaySink createReplaySink() {
		ReplaySinkImpl replaySink = new ReplaySinkImpl();
		return replaySink;
	}

		/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public FamilyElement createFamilyElement() {
		FamilyElementImpl familyElement = new FamilyElementImpl();
		return familyElement;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public DataManagementElement createDataManagementElement() {
		DataManagementElementImpl dataManagementElement = new DataManagementElementImpl();
		return dataManagementElement;
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public PipelinePackage getPipelinePackage()
  {
		return (PipelinePackage)getEPackage();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
  @Deprecated
  public static PipelinePackage getPackage()
  {
		return PipelinePackage.eINSTANCE;
	}

} //PipelineFactoryImpl
