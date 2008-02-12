/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.internal.cdo;

import org.eclipse.emf.cdo.CDOSession;
import org.eclipse.emf.cdo.CDOSessionInvalidationEvent;
import org.eclipse.emf.cdo.CDOView;
import org.eclipse.emf.cdo.internal.protocol.id.CDOIDTempMetaImpl;
import org.eclipse.emf.cdo.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.protocol.id.CDOID;
import org.eclipse.emf.cdo.protocol.id.CDOIDMetaRange;
import org.eclipse.emf.cdo.protocol.id.CDOIDObject;
import org.eclipse.emf.cdo.protocol.id.CDOIDObjectFactory;
import org.eclipse.emf.cdo.protocol.id.CDOIDTemp;
import org.eclipse.emf.cdo.protocol.id.CDOIDUtil;
import org.eclipse.emf.cdo.protocol.model.CDOClass;
import org.eclipse.emf.cdo.protocol.model.CDOClassRef;
import org.eclipse.emf.cdo.protocol.model.CDOPackage;
import org.eclipse.emf.cdo.protocol.revision.CDORevision;
import org.eclipse.emf.cdo.protocol.util.TransportException;
import org.eclipse.emf.cdo.util.CDOUtil;

import org.eclipse.emf.internal.cdo.bundle.OM;
import org.eclipse.emf.internal.cdo.protocol.CDOClientProtocol;
import org.eclipse.emf.internal.cdo.protocol.OpenSessionRequest;
import org.eclipse.emf.internal.cdo.protocol.OpenSessionResult;
import org.eclipse.emf.internal.cdo.protocol.QueryObjectTypesRequest;
import org.eclipse.emf.internal.cdo.protocol.ViewsChangedRequest;
import org.eclipse.emf.internal.cdo.util.ModelUtil;
import org.eclipse.emf.internal.cdo.util.ProxyResolverURIResourceMap;

import org.eclipse.net4j.channel.IChannel;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.internal.util.container.Container;
import org.eclipse.net4j.internal.util.event.Event;
import org.eclipse.net4j.internal.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.net4j.internal.util.om.trace.ContextTracer;
import org.eclipse.net4j.signal.failover.IFailOverEvent;
import org.eclipse.net4j.signal.failover.IFailOverStrategy;
import org.eclipse.net4j.util.ImplementationError;
import org.eclipse.net4j.util.WrappedException;
import org.eclipse.net4j.util.event.EventUtil;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.io.ExtendedDataInput;
import org.eclipse.net4j.util.lifecycle.ILifecycle;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Eike Stepper
 */
/**
 * @author Eike Stepper
 */
public class CDOSessionImpl extends Container<CDOView> implements CDOSession, CDOIDObjectFactory
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_SESSION, CDOSessionImpl.class);

  private int sessionID;

  private boolean disableLegacyObjects;

  private int referenceChunkSize;

  private IFailOverStrategy failOverStrategy;

  private IListener failOverStrategyListener = new IListener()
  {
    public void notifyEvent(IEvent event)
    {
      if (event instanceof IFailOverEvent)
      {
        IFailOverEvent e = (IFailOverEvent)event;
        handleFailOver(e.getOldChannel(), e.getNewChannel(), e.getNewConnector());
      }
    }
  };

  private IConnector connector;

  private IChannel channel;

  private String repositoryName;

  private String repositoryUUID;

  private CDOPackageRegistryImpl packageRegistry;

  private CDOSessionPackageManagerImpl packageManager;

  private CDORevisionManagerImpl revisionManager;

  private Map<CDOID, InternalEObject> idToMetaInstanceMap = new HashMap<CDOID, InternalEObject>();

  private Map<InternalEObject, CDOID> metaInstanceToIDMap = new HashMap<InternalEObject, CDOID>();

  private ConcurrentMap<CDOID, CDOClass> types = new ConcurrentHashMap<CDOID, CDOClass>();

  private Map<ResourceSet, CDOViewImpl> views = new HashMap<ResourceSet, CDOViewImpl>();

  private transient int lastViewID;

  private transient int lastTempMetaID;

  private IListener channelListener = new LifecycleEventAdapter()
  {
    @Override
    protected void onDeactivated(ILifecycle lifecycle)
    {
      close();
    }
  };

  private CDOIDObjectFactory cdoidObjectFactory;

  public CDOSessionImpl(EPackage.Registry delegate)
  {
    packageRegistry = createPackageRegistry(delegate);
    packageManager = createPackageManager();
    revisionManager = createRevisionManager();
    referenceChunkSize = OM.PREF_REFERENCE_CHUNK_SIZE.getValue();
  }

  public int getSessionID()
  {
    return sessionID;
  }

  public Class<?>[] getCDOIDObjectClasses()
  {
    return cdoidObjectFactory.getCDOIDObjectClasses();
  }

  public CDOIDObject createCDOIDObject(ExtendedDataInput in)
  {
    return cdoidObjectFactory.createCDOIDObject(in);
  }

  public boolean isDisableLegacyObjects()
  {
    return disableLegacyObjects;
  }

  public void setDisableLegacyObjects(boolean disableLegacyObjects)
  {
    checkInactive();
    this.disableLegacyObjects = disableLegacyObjects;
  }

  public int getReferenceChunkSize()
  {
    return referenceChunkSize;
  }

  public void setReferenceChunkSize(int referenceChunkSize)
  {
    if (referenceChunkSize <= 0)
    {
      referenceChunkSize = CDORevision.UNCHUNKED;
    }

    this.referenceChunkSize = referenceChunkSize;
  }

  public IFailOverStrategy getFailOverStrategy()
  {
    return failOverStrategy == null ? IFailOverStrategy.NOOP : failOverStrategy;
  }

  public void setFailOverStrategy(IFailOverStrategy failOverStrategy)
  {
    if (this.failOverStrategy != null)
    {
      this.failOverStrategy.removeListener(failOverStrategyListener);
    }

    this.failOverStrategy = failOverStrategy;
    if (this.failOverStrategy != null)
    {
      this.failOverStrategy.addListener(failOverStrategyListener);
    }
  }

  public IConnector getConnector()
  {
    return connector;
  }

  public void setConnector(IConnector connector)
  {
    this.connector = connector;
  }

  public IChannel getChannel()
  {
    return channel;
  }

  public CDOClientProtocol getProtocol()
  {
    return (CDOClientProtocol)channel.getReceiveHandler();
  }

  public String getRepositoryName()
  {
    return repositoryName;
  }

  public void setRepositoryName(String repositoryName)
  {
    this.repositoryName = repositoryName;
  }

  public String getRepositoryUUID()
  {
    return repositoryUUID;
  }

  public boolean isOpen()
  {
    return channel != null;
  }

  public void close()
  {
    deactivate();
  }

  public CDOPackageRegistryImpl getPackageRegistry()
  {
    return packageRegistry;
  }

  public CDOSessionPackageManagerImpl getPackageManager()
  {
    return packageManager;
  }

  public CDORevisionManagerImpl getRevisionManager()
  {
    return revisionManager;
  }

  public CDOTransactionImpl openTransaction(ResourceSet resourceSet)
  {
    CDOTransactionImpl transaction = createTransaction(++lastViewID);
    attach(resourceSet, transaction);
    return transaction;
  }

  protected CDOTransactionImpl createTransaction(int id)
  {
    return new CDOTransactionImpl(id, this);
  }

  public CDOTransactionImpl openTransaction()
  {
    return openTransaction(createResourceSet());
  }

  public CDOViewImpl openView(ResourceSet resourceSet)
  {
    CDOViewImpl view = createView(++lastViewID);
    attach(resourceSet, view);
    return view;
  }

  protected CDOViewImpl createView(int id)
  {
    return new CDOViewImpl(id, this);
  }

  public CDOViewImpl openView()
  {
    return openView(createResourceSet());
  }

  public CDOAuditImpl openAudit(ResourceSet resourceSet, long timeStamp)
  {
    CDOAuditImpl audit = createAudit(++lastViewID, timeStamp);
    attach(resourceSet, audit);
    return audit;
  }

  protected CDOAuditImpl createAudit(int id, long timeStamp)
  {
    return new CDOAuditImpl(id, this, timeStamp);
  }

  public CDOAuditImpl openAudit(long timeStamp)
  {
    return openAudit(createResourceSet(), timeStamp);
  }

  public CDOView getView(int viewID)
  {
    for (CDOViewImpl view : getViews())
    {
      if (view.getViewID() == viewID)
      {
        return view;
      }
    }

    return null;
  }

  public CDOViewImpl[] getViews()
  {
    Collection<CDOViewImpl> values;
    synchronized (views)
    {
      values = views.values();
    }

    return values.toArray(new CDOViewImpl[values.size()]);
  }

  public CDOView[] getElements()
  {
    return getViews();
  }

  @Override
  public boolean isEmpty()
  {
    return views.isEmpty();
  }

  public void viewDetached(CDOViewImpl view)
  {
    ResourceSet resourceSet = view.getResourceSet();
    synchronized (views)
    {
      if (views.remove(resourceSet) == null)
      {
        return;
      }
    }

    try
    {
      new ViewsChangedRequest(channel, view.getViewID(), CDOProtocolConstants.VIEW_CLOSED).send();
    }
    catch (Exception ex)
    {
      throw WrappedException.wrap(ex);
    }

    fireElementRemovedEvent(view);
  }

  public synchronized CDOIDMetaRange getTempMetaIDRange(int count)
  {
    CDOIDTemp lowerBound = new CDOIDTempMetaImpl(lastTempMetaID + 1);
    lastTempMetaID += count;
    return CDOIDUtil.createMetaRange(lowerBound, count);
  }

  public InternalEObject lookupMetaInstance(CDOID id)
  {
    InternalEObject metaInstance = idToMetaInstanceMap.get(id);
    if (metaInstance == null)
    {
      CDOPackage[] cdoPackages = packageManager.getPackages();
      for (CDOPackage cdoPackage : cdoPackages)
      {
        CDOIDMetaRange metaIDRange = cdoPackage.getMetaIDRange();
        if (metaIDRange != null && metaIDRange.contains(id))
        {
          EPackage ePackage = ModelUtil.getEPackage(cdoPackage, packageRegistry);
          registerEPackage(ePackage);
          metaInstance = idToMetaInstanceMap.get(id);
          break;
        }
      }
    }

    return metaInstance;
  }

  public CDOID lookupMetaInstanceID(InternalEObject metaInstance)
  {
    return metaInstanceToIDMap.get(metaInstance);
  }

  public void registerEPackage(EPackage ePackage, CDOIDMetaRange metaIDRange)
  {
    if (metaIDRange.isTemporary())
    {
      throw new IllegalArgumentException("metaIDRange.isTemporary()");
    }

    CDOIDMetaRange range = CDOIDUtil.createMetaRange(metaIDRange.getLowerBound(), 0);
    range = registerMetaInstance((InternalEObject)ePackage, range);
    if (range.size() != metaIDRange.size())
    {
      throw new IllegalStateException("range.getCount() != metaIDRange.getCount()");
    }
  }

  public CDOIDMetaRange registerEPackage(EPackage ePackage)
  {
    CDOIDTemp lowerBound = new CDOIDTempMetaImpl(lastTempMetaID + 1);
    CDOIDMetaRange range = CDOIDUtil.createMetaRange(lowerBound, 0);
    range = registerMetaInstance((InternalEObject)ePackage, range);
    lastTempMetaID = ((CDOIDTemp)range.getUpperBound()).getIntValue();
    return range;
  }

  /**
   * TODO Synchronize?
   */
  private CDOIDMetaRange registerMetaInstance(InternalEObject metaInstance, CDOIDMetaRange range)
  {
    range = range.increase();
    CDOID id = range.getUpperBound();
    if (TRACER.isEnabled())
    {
      TRACER.format("Registering meta instance: {0} <-> {1}", id, metaInstance);
    }

    idToMetaInstanceMap.put(id, metaInstance);
    metaInstanceToIDMap.put(metaInstance, id);

    for (EObject content : metaInstance.eContents())
    {
      range = registerMetaInstance((InternalEObject)content, range);
    }

    return range;
  }

  public void remapMetaInstance(CDOID oldID, CDOID newID)
  {
    InternalEObject metaInstance = idToMetaInstanceMap.remove(oldID);
    if (metaInstance == null)
    {
      throw new IllegalArgumentException("Unknown meta instance id: " + oldID);
    }

    if (TRACER.isEnabled())
    {
      TRACER.format("Remapping meta instance: {0} --> {1} <-> {2}", oldID, newID, metaInstance);
    }

    idToMetaInstanceMap.put(newID, metaInstance);
    metaInstanceToIDMap.put(metaInstance, newID);
  }

  public CDOClass getObjectType(CDOID id)
  {
    return types.get(id);
  }

  public CDOClass requestObjectType(CDOID id)
  {
    try
    {
      QueryObjectTypesRequest request = new QueryObjectTypesRequest(channel, Collections.singletonList(id));
      CDOClassRef[] typeRefs = getFailOverStrategy().send(request);
      CDOClass type = typeRefs[0].resolve(packageManager);
      registerObjectType(id, type);
      return type;
    }
    catch (Exception ex)
    {
      throw new TransportException(ex);
    }
  }

  public void registerObjectType(CDOID id, CDOClass type)
  {
    types.put(id, type);
  }

  public void notifyInvalidation(long timeStamp, Set<CDOID> dirtyOIDs, CDOViewImpl excludedView)
  {
    dirtyOIDs = Collections.unmodifiableSet(dirtyOIDs);
    for (CDOViewImpl view : getViews())
    {
      if (view != excludedView)
      {
        try
        {
          view.notifyInvalidation(timeStamp, dirtyOIDs);
        }
        catch (RuntimeException ex)
        {
          OM.LOG.error(ex);
        }
      }
    }

    fireInvalidationEvent(timeStamp, dirtyOIDs, excludedView);
  }

  public void fireInvalidationEvent(long timeStamp, Set<CDOID> dirtyOIDs, CDOViewImpl excludedView)
  {
    fireEvent(new InvalidationEvent(excludedView, timeStamp, dirtyOIDs));
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("CDOSession[{0}/{1}]", connector, repositoryName);
  }

  protected CDOPackageRegistryImpl createPackageRegistry(EPackage.Registry delegate)
  {
    return new CDOPackageRegistryImpl(this, delegate);
  }

  protected CDOSessionPackageManagerImpl createPackageManager()
  {
    return new CDOSessionPackageManagerImpl(this);
  }

  protected CDORevisionManagerImpl createRevisionManager()
  {
    return new CDORevisionManagerImpl(this);
  }

  protected ResourceSet createResourceSet()
  {
    return new ResourceSetImpl();
  }

  protected void attach(ResourceSet resourceSet, CDOViewImpl view)
  {
    if (CDOUtil.getView(resourceSet) != null)
    {
      throw new IllegalStateException("CDO view already open");
    }

    resourceSet.setPackageRegistry(new EPackageRegistryImpl(packageRegistry));
    CDOUtil.prepareResourceSet(resourceSet);

    Map<URI, Resource> resourceMap = null;
    if (resourceSet instanceof ResourceSetImpl)
    {
      ResourceSetImpl rs = (ResourceSetImpl)resourceSet;
      resourceMap = rs.getURIResourceMap();
      rs.setURIResourceMap(new ProxyResolverURIResourceMap(view, resourceMap));
    }
    else
    {
      throw new ImplementationError("Not a " + ResourceSetImpl.class.getName() + ": "
          + resourceSet.getClass().getName());
    }

    synchronized (views)
    {
      views.put(resourceSet, view);
    }

    resourceSet.eAdapters().add(view);
    sendViewsNotification(view);
    fireElementAddedEvent(view);
  }

  protected void handleFailOver(IChannel oldChannel, IChannel newChannel, IConnector newConnector)
  {
    EventUtil.removeListener(oldChannel, channelListener);
    EventUtil.addListener(newChannel, channelListener);

    channel = newChannel;
    connector = newConnector;
  }

  @Override
  protected void doBeforeActivate() throws Exception
  {
    super.doBeforeActivate();
    if (channel == null && connector == null)
    {
      throw new IllegalStateException("channel == null && connector == null");
    }

    if (repositoryName == null)
    {
      throw new IllegalStateException("repositoryName == null");
    }
  }

  @Override
  protected void doActivate() throws Exception
  {
    super.doActivate();
    if (channel == null)
    {
      channel = connector.openChannel(CDOProtocolConstants.PROTOCOL_NAME, this);
    }

    OpenSessionRequest request = new OpenSessionRequest(channel, repositoryName, disableLegacyObjects);
    OpenSessionResult result = request.send();

    sessionID = result.getSessionID();
    repositoryUUID = result.getRepositoryUUID();
    cdoidObjectFactory = createCDOIDFactory(result.getCDOIDObjectFactoryClass(), result.getCDOIDObjectClasses());
    packageManager.addPackageProxies(result.getPackageInfos());
    packageManager.activate();
    revisionManager.activate();
    EventUtil.addListener(channel, channelListener);
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    EventUtil.removeListener(channel, channelListener);
    revisionManager.deactivate();
    packageManager.deactivate();
    synchronized (views)
    {
      for (CDOViewImpl view : getViews())
      {
        try
        {
          view.close();
        }
        catch (RuntimeException ignore)
        {
        }
      }
    }

    channel.close();
    super.doDeactivate();
  }

  private CDOIDObjectFactory createCDOIDFactory(Class<?> objectFactoryClass, List<Class<?>> objectClasses)
  {
    // ClassLoader classLoader = new CDOIDObjectFactoryClassLoader();

    // return new CDOIDObjectFactoryImpl();

    try
    {
      return (CDOIDObjectFactory)objectFactoryClass.newInstance();
    }
    catch (Exception ex)
    {
      throw WrappedException.wrap(ex);
    }
  }

  private void sendViewsNotification(CDOViewImpl view)
  {
    try
    {
      int id = view.getViewID();
      byte kind = getKind(view);
      new ViewsChangedRequest(channel, id, kind).send();
    }
    catch (Exception ex)
    {
      throw WrappedException.wrap(ex);
    }
  }

  private byte getKind(CDOViewImpl view)
  {
    CDOView.Type type = view.getViewType();
    switch (type)
    {
    case TRANSACTION:
      return CDOProtocolConstants.VIEW_TRANSACTION;
    case READONLY:
      return CDOProtocolConstants.VIEW_READONLY;
    case AUDIT:
      return CDOProtocolConstants.VIEW_AUDIT;
    }

    throw new ImplementationError("Invalid view type: " + type);
  }

  /**
   * @author Eike Stepper
   */
  private final class InvalidationEvent extends Event implements CDOSessionInvalidationEvent
  {
    private static final long serialVersionUID = 1L;

    private CDOViewImpl view;

    private long timeStamp;

    private Set<CDOID> dirtyOIDs;

    public InvalidationEvent(CDOViewImpl view, long timeStamp, Set<CDOID> dirtyOIDs)
    {
      super(CDOSessionImpl.this);
      this.view = view;
      this.timeStamp = timeStamp;
      this.dirtyOIDs = dirtyOIDs;
    }

    public CDOSession getSession()
    {
      return CDOSessionImpl.this;
    }

    public CDOViewImpl getView()
    {
      return view;
    }

    public long getTimeStamp()
    {
      return timeStamp;
    }

    public Set<CDOID> getDirtyOIDs()
    {
      return dirtyOIDs;
    }

    @Override
    public String toString()
    {
      return "CDOSessionInvalidationEvent" + dirtyOIDs;
    }
  }
}
