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

import java.util.Map;
import java.util.Iterator;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * Dumps lots of information
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Dump extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected boolean refs;

   protected boolean properties;

   protected boolean datatypes;

   protected boolean targets;

   protected boolean taskdefs;

   protected boolean filters;

   public void setRefs(boolean flag)
   {
      refs = flag;
   }

   public void setProperties(boolean flag)
   {
      properties = flag;
   }

   public void setProps(boolean flag)
   {
      setProperties(flag);
   }

   public void setDatatypes(boolean flag)
   {
      datatypes = flag;
   }

   public void setTargets(boolean flag)
   {
      targets = flag;
   }

   public void setTaskdefs(boolean flag)
   {
      taskdefs = flag;
   }

   public void setFilters(boolean flag)
   {
      filters = flag;
   }

   public void execute() throws BuildException
   {
      if (refs)
         dumpMap("References", getProject().getReferences());
      if (properties)
         dumpMap("Properties", getProject().getProperties());
      if (datatypes)
         dumpMap("DataTypes", getProject().getDataTypeDefinitions());
      if (targets)
         dumpMap("Targets", getProject().getTargets());
      if (taskdefs)
         dumpMap("Taskdefs", getProject().getTaskDefinitions());
      if (filters)
         dumpMap("Filters", getProject().getGlobalFilterSet().getFilterHash());
   }

   protected void dumpMap(final String name, final Map map)
   {
      log.info(name + ":");
      dumpMap(map, "  ");
      log.info("");
   }

   protected void dumpMap(final Map map, final String prefix)
   {
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         Object value = map.get(key);
         String classname = value.getClass().getName();
         log.info(prefix + key + "=" + value + " (" + classname + ")");
      }
   }
}
