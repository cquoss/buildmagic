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

package org.jboss.tools.buildmagic.task.module;

import java.util.Map;

import org.apache.tools.ant.Task;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Abstract task for project and module info tasks.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class AbstractInfo extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String _if;

   protected String unless;

   public void setIf(final String property)
   {
      this._if = property;
   }

   public void setUnless(final String property)
   {
      this.unless = property;
   }

   protected boolean canContinue()
   {
      Map map = getProject().getProperties();
      if (_if != null && !map.containsKey(_if))
         return false;
      else if (unless != null && map.containsKey(unless))
         return false;

      return true;
   }

   protected String getProperty(String name)
   {
      return getProject().getProperty(name);
   }
}
