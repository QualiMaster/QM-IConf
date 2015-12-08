/**
 */
package pipeline.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import pipeline.Pipeline;
import pipeline.PipelineFactory;
import pipeline.PipelinePackage;

/**
 * This is the item provider adapter for a {@link pipeline.Pipeline} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class PipelineItemProvider
  extends ItemProviderAdapter
  implements
    IEditingDomainItemProvider,
    IStructuredItemContentProvider,
    ITreeItemContentProvider,
    IItemLabelProvider,
    IItemPropertySource
{
  /**
     * This constructs an instance from a factory and a notifier.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public PipelineItemProvider(AdapterFactory adapterFactory)
  {
        super(adapterFactory);
    }

  /**
     * This returns the property descriptors for the adapted class.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object)
  {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            addNamePropertyDescriptor(object);
            addNumworkersPropertyDescriptor(object);
            addArtifactPropertyDescriptor(object);
            addConstraintsPropertyDescriptor(object);
            addDebugPropertyDescriptor(object);
            addFastSerializationPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

  /**
     * This adds a property descriptor for the Name feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addNamePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_name_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_name_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__NAME,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Numworkers feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addNumworkersPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_numworkers_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_numworkers_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__NUMWORKERS,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Artifact feature.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected void addArtifactPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_artifact_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_artifact_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__ARTIFACT,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Constraints feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addConstraintsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_constraints_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_constraints_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__CONSTRAINTS,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Debug feature.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected void addDebugPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_debug_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_debug_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__DEBUG,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Fast Serialization feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addFastSerializationPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Pipeline_fastSerialization_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Pipeline_fastSerialization_feature", "_UI_Pipeline_type"),
                 PipelinePackage.Literals.PIPELINE__FAST_SERIALIZATION,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
     * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
     * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object)
  {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(PipelinePackage.Literals.PIPELINE__NODES);
            childrenFeatures.add(PipelinePackage.Literals.PIPELINE__FLOWS);
        }
        return childrenFeatures;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  protected EStructuralFeature getChildFeature(Object object, Object child)
  {
        // Check the type of the specified child object and return the proper feature to use for
        // adding (see {@link AddCommand}) it as a child.

        return super.getChildFeature(object, child);
    }

  /**
     * This returns Pipeline.gif.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public Object getImage(Object object)
  {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/Pipeline"));
    }

  /**
     * This returns the label text for the adapted class.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public String getText(Object object)
  {
        String label = ((Pipeline)object).getName();
        return label == null || label.length() == 0 ?
            getString("_UI_Pipeline_type") :
            getString("_UI_Pipeline_type") + " " + label;
    }

  /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached
     * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public void notifyChanged(Notification notification)
  {
        updateChildren(notification);

        switch (notification.getFeatureID(Pipeline.class)) {
            case PipelinePackage.PIPELINE__NAME:
            case PipelinePackage.PIPELINE__NUMWORKERS:
            case PipelinePackage.PIPELINE__ARTIFACT:
            case PipelinePackage.PIPELINE__CONSTRAINTS:
            case PipelinePackage.PIPELINE__DEBUG:
            case PipelinePackage.PIPELINE__FAST_SERIALIZATION:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                return;
            case PipelinePackage.PIPELINE__NODES:
            case PipelinePackage.PIPELINE__FLOWS:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                return;
        }
        super.notifyChanged(notification);
    }

  /**
     * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
     * that can be created under this object.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object)
  {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        newChildDescriptors.add
            (createChildParameter
                (PipelinePackage.Literals.PIPELINE__NODES,
                 PipelineFactory.eINSTANCE.createSource()));

        newChildDescriptors.add
            (createChildParameter
                (PipelinePackage.Literals.PIPELINE__NODES,
                 PipelineFactory.eINSTANCE.createSink()));

        newChildDescriptors.add
            (createChildParameter
                (PipelinePackage.Literals.PIPELINE__NODES,
                 PipelineFactory.eINSTANCE.createFamilyElement()));

        newChildDescriptors.add
            (createChildParameter
                (PipelinePackage.Literals.PIPELINE__NODES,
                 PipelineFactory.eINSTANCE.createDataManagementElement()));

        newChildDescriptors.add
            (createChildParameter
                (PipelinePackage.Literals.PIPELINE__FLOWS,
                 PipelineFactory.eINSTANCE.createFlow()));
    }

  /**
     * Return the resource locator for this item provider's resources.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public ResourceLocator getResourceLocator()
  {
        return PipelineEditPlugin.INSTANCE;
    }

}
