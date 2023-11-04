/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.tools.buildmagic.task;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;

/**
 * An abstract BuildListener.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class AbstractBuildListener implements BuildListener
{
   /**
    *  Fired before any targets are started.
    */
   public void buildStarted(BuildEvent event)
   {
   }

   /**
    *  Fired after the last target has finished. This event
    *  will still be thrown if an error occured during the build.
    *
    *  @see BuildEvent#getException()
    */
   public void buildFinished(BuildEvent event)
   {
   }

   /**
    *  Fired when a target is started.
    *
    *  @see BuildEvent#getTarget()
    */
   public void targetStarted(BuildEvent event)
   {
   }

   /**
    *  Fired when a target has finished. This event will
    *  still be thrown if an error occured during the build.
    *
    *  @see BuildEvent#getException()
    */
   public void targetFinished(BuildEvent event)
   {
   }

   /**
    *  Fired when a task is started.
    *
    *  @see BuildEvent#getTask()
    */
   public void taskStarted(BuildEvent event)
   {
   }

   /**
    *  Fired when a task has finished. This event will still
    *  be throw if an error occured during the build.
    *
    *  @see BuildEvent#getException()
    */
   public void taskFinished(BuildEvent event)
   {
   }

   /**
    *  Fired whenever a message is logged.
    *
    *  @see BuildEvent#getMessage()
    *  @see BuildEvent#getPriority()
    */
   public void messageLogged(BuildEvent event)
   {
   }
}
