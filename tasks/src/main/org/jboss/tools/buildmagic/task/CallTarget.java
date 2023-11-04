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
import org.apache.tools.ant.taskdefs.Available;

import java.util.*;
import java.lang.reflect.*;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * A minimal replacemet for the default CallTarget (aka antcall).
 *
 * <p>Unlike antcall, this task does not create a sub-project, but
 *    instead uses the current project.
 *
 * @version $Id$
 * @author  Jason Dillon <A href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</A>
 */
public class CallTarget extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** The list of parameters/properties to load. */
   protected final List params = new LinkedList();

   /** The target to execute. */
   protected String target;

   /** Flag to force override on all generated properties. */
   protected boolean override = true;

   protected String _if;

   protected String unless;

   protected List availables = new LinkedList();

   /**
    * Set the property override default flag.
    *
    * @param override   Override flag.
    */
   public void setOverride(final boolean override)
   {
      this.override = override;
      log.debug("override flag: " + override);
   }

   /**
    * Set the target to execute.
    *
    * @param target  The target to execute.
    */
   public void setTarget(final String target)
   {
      this.target = target;
   }

   public void setIf(final String property)
   {
      this._if = property;
   }

   public void setUnless(final String property)
   {
      this.unless = property;
   }

   public Available createAvailable()
   {
      Available a = (Available) getProject().createTask("available");
      a.setProperty("null");
      availables.add(a);

      return a;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException     Attributes are not valid.
    */
   protected void validate() throws BuildException
   {
      if (target == null)
         throw new MissingAttributeException("target", this);
   }

   /** The argument types for the property override method. */
   public static final Class[] ARGUMENT_TYPES =
   {Boolean.TYPE};

   /** The argument values for the property override method. */
   public static final Object[] ARGUMENT_VALUES =
   {Boolean.TRUE};

   /**
    * Invoke the target.
    */
   public void execute() throws BuildException
   {
      validate();

      Iterator iter = availables.iterator();
      while (iter.hasNext())
      {
         Available a = (Available) iter.next();
         if (!a.eval())
            return;
      }

      Map map = getProject().getProperties();
      if (_if != null && !map.containsKey(_if))
      {
         return;
      }
      else if (unless != null && map.containsKey(unless))
      {
         return;
      }

      // initialize all nested parameters
      iter = params.iterator();
      while (iter.hasNext())
      {
         Task task = (Task) iter.next();

         if (override)
         {
            try
            {
               Class type = task.getClass();
               Method meth = type.getMethod("setOverride", ARGUMENT_TYPES);
               meth.invoke(task, ARGUMENT_VALUES);
               log.debug("set override for task: " + task);
            }
            catch (Exception e)
            {
               log.warning("failed to set override: " + e);
            }
         }

         task.execute();
      }

      getProject().executeTarget(target);
   }

   /**
    * Create a nested <tt>param</tt> element.
    */
   public Task createParam()
   {
      Task task = getProject().createTask("property");
      params.add(task);
      return task;
   }

   /**
    * Create a nested <tt>property</tt> element.
    */
   public Task createProperty()
   {
      Task task = getProject().createTask("property");
      params.add(task);
      return task;
   }
}
