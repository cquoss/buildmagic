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

package org.jboss.tools.buildmagic.task.util;

import org.apache.tools.ant.*;

/**
 * A helper class to make logging from a task nicer.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class TaskLogger
{
   protected Task task;

   public TaskLogger(final Task task)
   {
      this.task = task;
   }

   public void info(final String message)
   {
      task.log(message, Project.MSG_INFO);
   }

   public void error(final String message)
   {
      task.log(message, Project.MSG_ERR);
   }

   public void warning(final String message)
   {
      task.log(message, Project.MSG_WARN);
   }

   public void verbose(final String message)
   {
      task.log(message, Project.MSG_VERBOSE);
   }

   public void debug(final String message)
   {
      task.log(message, Project.MSG_DEBUG);
   }
}
