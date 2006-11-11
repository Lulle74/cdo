/***************************************************************************
 * Copyright (c) 2004, 2005, 2006 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.internal.net4j.transport.tcp;

import org.eclipse.net4j.transport.Buffer;
import org.eclipse.net4j.transport.Channel;
import org.eclipse.net4j.transport.ConnectorException;
import org.eclipse.net4j.transport.tcp.TCPConnector;
import org.eclipse.net4j.transport.tcp.TCPSelector;
import org.eclipse.net4j.transport.tcp.TCPSelectorListener;
import org.eclipse.net4j.util.Net4jUtil;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.trace.ContextTracer;

import org.eclipse.internal.net4j.bundle.Net4j;
import org.eclipse.internal.net4j.transport.AbstractConnector;
import org.eclipse.internal.net4j.transport.ChannelImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

/**
 * @author Eike Stepper
 */
public abstract class AbstractTCPConnector extends AbstractConnector implements TCPConnector,
    TCPSelectorListener.Active
{
  private static final ContextTracer TRACER = new ContextTracer(Net4j.DEBUG_CONNECTOR,
      AbstractTCPConnector.class);

  private static final long REGISTER_SELECTOR_TIMEOUT = 250000;

  private SocketChannel socketChannel;

  private TCPSelector selector;

  private SelectionKey selectionKey;

  private Buffer inputBuffer;

  private ControlChannelImpl controlChannel;

  public AbstractTCPConnector()
  {
    try
    {
      socketChannel = SocketChannel.open();
      socketChannel.configureBlocking(false);
    }
    catch (IOException ex)
    {
      Net4j.LOG.error(ex);
    }
  }

  /**
   * SocketChannel must already be non-blocking!
   */
  public AbstractTCPConnector(SocketChannel socketChannel)
  {
    this.socketChannel = socketChannel;
  }

  public TCPSelector getSelector()
  {
    return selector;
  }

  public void setSelector(TCPSelector selector)
  {
    this.selector = selector;
  }

  public SocketChannel getSocketChannel()
  {
    return socketChannel;
  }

  /**
   * Called by {@link ChannelImpl} each time a new buffer is available for
   * multiplexing. This or another buffer can be dequeued from the outputQueue
   * of the {@link ChannelImpl}.
   */
  public void multiplexBuffer(Channel channel)
  {
    selector.setWriteInterest(selectionKey, true);
  }

  public void handleConnect(TCPSelector selector, SocketChannel channel)
  {
    try
    {
      if (!channel.finishConnect())
      {
        return;
      }
    }
    catch (Exception ex)
    {
      return;
    }

    try
    {
      selector.setConnectInterest(selectionKey, false);
      setState(State.NEGOTIATING);
    }
    catch (Exception ex)
    {
      Net4j.LOG.error(ex);
      deactivate();
    }
  }

  public void handleRead(TCPSelector selector, SocketChannel socketChannel)
  {
    try
    {
      if (inputBuffer == null)
      {
        inputBuffer = getBufferProvider().provideBuffer();
      }

      ByteBuffer byteBuffer = inputBuffer.startGetting(socketChannel);
      if (byteBuffer != null)
      {
        short channelIndex = inputBuffer.getChannelIndex();
        ChannelImpl channel = channelIndex == ControlChannelImpl.CONTROL_CHANNEL_ID ? controlChannel
            : getChannel(channelIndex);
        if (channel != null)
        {
          channel.handleBufferFromMultiplexer(inputBuffer);
        }
        else
        {
          if (TRACER.isEnabled())
          {
            TRACER.trace("Discarding buffer from unknown channel"); //$NON-NLS-1$
          }

          inputBuffer.release();
        }

        inputBuffer = null;
      }
    }
    catch (ClosedChannelException ex)
    {
      deactivate();
    }
    catch (Exception ex)
    {
      Net4j.LOG.error(ex);
      deactivate();
    }
  }

  public void handleWrite(TCPSelector selector, SocketChannel socketChannel)
  {
    try
    {
      boolean moreToWrite = false;
      for (Queue<Buffer> bufferQueue : getChannelBufferQueues())
      {
        Buffer buffer = bufferQueue.peek();
        if (buffer != null)
        {
          if (buffer.write(socketChannel))
          {
            bufferQueue.poll();
            buffer.release();

            if (!moreToWrite)
            {
              moreToWrite = !bufferQueue.isEmpty();
            }
          }
          else
          {
            moreToWrite = true;
            break;
          }
        }
      }

      if (!moreToWrite)
      {
        selector.setWriteInterest(selectionKey, false);
      }
    }
    catch (NullPointerException ignore)
    {
      ;
    }
    catch (ClosedChannelException ex)
    {
      deactivate();
    }
    catch (Exception ex)
    {
      Net4j.LOG.error(ex);
      deactivate();
    }
  }

  @Override
  protected List<Queue<Buffer>> getChannelBufferQueues()
  {
    List<Queue<Buffer>> queues = super.getChannelBufferQueues();
    Queue<Buffer> controlQueue = controlChannel.getSendQueue();
    if (!controlQueue.isEmpty())
    {
      queues.add(controlQueue);
    }

    return queues;
  }

  @Override
  protected void registerChannelWithPeer(short channelIndex, String protocolID)
      throws ConnectorException
  {
    if (!controlChannel.registerChannel(channelIndex, protocolID))
    {
      throw new ConnectorException("Failed to register channel with peer"); //$NON-NLS-1$
    }
  }

  @Override
  protected void removeChannel(ChannelImpl channel)
  {
    if (isConnected())
    {
      controlChannel.deregisterChannel(channel.getChannelIndex());
    }

    super.removeChannel(channel);
  }

  @Override
  protected void onAboutToActivate() throws Exception
  {
    super.onAboutToActivate();
    if (selector == null)
    {
      selector = Net4jUtil.createTCPSelector();
      LifecycleUtil.activate(selector);
    }
  }

  @Override
  protected void onActivate() throws Exception
  {
    super.onActivate();
    controlChannel = new ControlChannelImpl(this);
    controlChannel.activate();

    selectionKey = selector.register(socketChannel, this, REGISTER_SELECTOR_TIMEOUT);
    if (selectionKey == null)
    {
      throw new TimeoutException("Unable to register channel with selector");
    }

    if (getType() == Type.SERVER)
    {
      selector.setConnectInterest(selectionKey, false);
    }
  }

  @Override
  protected void onDeactivate() throws Exception
  {
    Exception exception = null;

    try
    {
      controlChannel.deactivate();
    }
    catch (Exception ex)
    {
      if (exception == null)
      {
        exception = ex;
      }
    }
    finally
    {
      controlChannel = null;
    }

    try
    {
      socketChannel.close();
    }
    catch (Exception ex)
    {
      if (exception == null)
      {
        exception = ex;
      }
    }
    finally
    {
      socketChannel = null;
    }

    try
    {
      super.onDeactivate();
    }
    catch (Exception ex)
    {
      if (exception == null)
      {
        exception = ex;
      }
    }

    if (exception != null)
    {
      throw exception;
    }
  }
}
