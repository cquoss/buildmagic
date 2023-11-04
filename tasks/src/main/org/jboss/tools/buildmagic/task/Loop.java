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

import org.apache.tools.ant.*;

/**
 * An looping task.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Loop extends ContainerTask
{
   /** The loop count. */
   protected int count = -1;

   /**
    * Set the loop count.
    *
    * @param count   The loop count.
    */
   public void setCount(final int count)
   {
      this.count = count;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException     Attributes are not valid.
    */
   protected void validate() throws BuildException
   {
      super.validate();

      if (count <= 0)
         throw new IllegalAttributeException("count", "must be > 0", this);
   }

   /**
    * Execute the configured target the configured number of times.
    */
   public void execute() throws BuildException
   {
      validate();

      log.verbose("looping '" + getOwningTarget() + "' for count: " + count);
      for (int i = 0; i < count; i++)
      {
         log.debug("i: " + i);
         executeTasks();
      }
   }
}
