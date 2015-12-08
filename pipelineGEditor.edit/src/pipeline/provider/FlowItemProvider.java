/**
 */
package pipeline.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

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

import pipeline.Flow;
import pipeline.PipelinePackage;

/**
 * This is the item provider adapter for a {@link pipeline.Flow} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class FlowItemProvider
  extends PipelineElementItemProvider
{
  /**
     * This constructs an instance from a factory and a notifier.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public FlowItemProvider(AdapterFactory adapterFactory)
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

            addSourcePropertyDescriptor(object);
            addDestinationPropertyDescriptor(object);
            addGroupingPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

  /**
     * This adds a property descriptor for the Source feature.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  protected void addSourcePropertyDescriptor(Object object)
  {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Flow_source_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Flow_source_feature", "_UI_Flow_type"),
                 PipelinePackage.Literals.FLOW__SOURCE,
                 true,
                 false,
                 true,
                 null,
                 null,
                 null));
    }

  /**
     * This adds a property descriptor for the Destination feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addDestinationPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Flow_destination_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Flow_destination_feature", "_UI_Flow_type"),
                 PipelinePackage.Literals.FLOW__DESTINATION,
                 true,
                 false,
                 true,
                 null,
                 null,
                 null));
    }

/**
     * This adds a property descriptor for the Grouping feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addGroupingPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_Flow_grouping_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_Flow_grouping_feature", "_UI_Flow_type"),
                 PipelinePackage.Literals.FLOW__GROUPING,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

/**
     * This returns Flow.gif.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public Object getImage(Object object)
  {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/Flow"));
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
        String label = ((Flow)object).getName();
        return label == null || label.length() == 0 ?
            getString("_UI_Flow_type") :
            getString("_UI_Flow_type") + " " + label;
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

        switch (notification.getFeatureID(Flow.class)) {
            case PipelinePackage.FLOW__GROUPING:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
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
    }

}
