/**
 */
package pipeline.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import pipeline.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see pipeline.PipelinePackage
 * @generated
 */
public class PipelineAdapterFactory extends AdapterFactoryImpl
{
  /**
	 * The cached model package.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected static PipelinePackage modelPackage;

  /**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public PipelineAdapterFactory()
  {
		if (modelPackage == null) {
			modelPackage = PipelinePackage.eINSTANCE;
		}
	}

  /**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
   * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
   * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
  @Override
  public boolean isFactoryForType(Object object)
  {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

  /**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected PipelineSwitch<Adapter> modelSwitch =
    new PipelineSwitch<Adapter>() {
			@Override
			public Adapter casePipeline(Pipeline object) {
				return createPipelineAdapter();
			}
			@Override
			public Adapter casePipelineElement(PipelineElement object) {
				return createPipelineElementAdapter();
			}
			@Override
			public Adapter caseFlow(Flow object) {
				return createFlowAdapter();
			}
			@Override
			public Adapter casePipelineNode(PipelineNode object) {
				return createPipelineNodeAdapter();
			}
			@Override
			public Adapter caseSource(Source object) {
				return createSourceAdapter();
			}
			@Override
			public Adapter caseSink(Sink object) {
				return createSinkAdapter();
			}
			@Override
			public Adapter caseProcessingElement(ProcessingElement object) {
				return createProcessingElementAdapter();
			}
			@Override
			public Adapter caseFamilyElement(FamilyElement object) {
				return createFamilyElementAdapter();
			}
			@Override
			public Adapter caseDataManagementElement(DataManagementElement object) {
				return createDataManagementElementAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

  /**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
  @Override
  public Adapter createAdapter(Notifier target)
  {
		return modelSwitch.doSwitch((EObject)target);
	}


  /**
	 * Creates a new adapter for an object of class '{@link pipeline.Pipeline <em>Pipeline</em>}'.
	 * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.Pipeline
	 * @generated
	 */
  public Adapter createPipelineAdapter()
  {
		return null;
	}

  /**
	 * Creates a new adapter for an object of class '{@link pipeline.PipelineElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.PipelineElement
	 * @generated
	 */
    public Adapter createPipelineElementAdapter() {
		return null;
	}

/**
	 * Creates a new adapter for an object of class '{@link pipeline.Flow <em>Flow</em>}'.
	 * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.Flow
	 * @generated
	 */
  public Adapter createFlowAdapter()
  {
		return null;
	}

  /**
	 * Creates a new adapter for an object of class '{@link pipeline.PipelineNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.PipelineNode
	 * @generated
	 */
    public Adapter createPipelineNodeAdapter() {
		return null;
	}

/**
	 * Creates a new adapter for an object of class '{@link pipeline.Source <em>Source</em>}'.
	 * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.Source
	 * @generated
	 */
  public Adapter createSourceAdapter()
  {
		return null;
	}

  /**
	 * Creates a new adapter for an object of class '{@link pipeline.Sink <em>Sink</em>}'.
	 * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.Sink
	 * @generated
	 */
  public Adapter createSinkAdapter()
  {
		return null;
	}

  /**
	 * Creates a new adapter for an object of class '{@link pipeline.ProcessingElement <em>Processing Element</em>}'.
	 * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.ProcessingElement
	 * @generated
	 */
  public Adapter createProcessingElementAdapter()
  {
		return null;
	}

  /**
	 * Creates a new adapter for an object of class '{@link pipeline.FamilyElement <em>Family Element</em>}'.
	 * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.FamilyElement
	 * @generated
	 */
    public Adapter createFamilyElementAdapter() {
		return null;
	}

/**
	 * Creates a new adapter for an object of class '{@link pipeline.DataManagementElement <em>Data Management Element</em>}'.
	 * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see pipeline.DataManagementElement
	 * @generated
	 */
    public Adapter createDataManagementElementAdapter() {
		return null;
	}

/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
   * This default implementation returns null.
   * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
  public Adapter createEObjectAdapter()
  {
		return null;
	}

} //PipelineAdapterFactory
