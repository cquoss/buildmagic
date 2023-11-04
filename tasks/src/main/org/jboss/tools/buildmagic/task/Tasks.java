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

import java.util.Map;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

/**
 * A collection of task utilities.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Tasks
{
   /**
    * Determine if a task should execute based on the given if and unless
    * condtions.
    *
    * @param map       The properties map.
    * @param _if       The <em>if</em> condition.
    * @param unless    The <em>unless</em> condition.
    * @return          True if the task can execute.
    */
   public static boolean canExecute(final Map props, final String _if, final String unless)
   {
      if (_if != null && !props.containsKey(_if))
      {
         return false;
      }

      if (unless != null && props.containsKey(unless))
      {
         return false;
      }

      return true;
   }

   /**
    * Determine if a task should execute based on the given if and unless
    * condtions.
    *
    * @param project   The project to get properties from.
    * @param _if       The <em>if</em> condition.
    * @param unless    The <em>unless</em> condition.
    * @return          True if the task can execute.
    */
   public static boolean canExecute(final Project project, final String _if, final String unless)
   {
      return canExecute(project.getProperties(), _if, unless);
   }

   /**
    * Determine if a task should execute based on the given if and unless
    * condtions.
    *
    * @param task      The task to check.
    * @param _if       The <em>if</em> condition.
    * @param unless    The <em>unless</em> condition.
    * @return          True if the task can execute.
    */
   public static boolean canExecute(final Task task, final String _if, final String unless)
   {
      return canExecute(task.getProject(), _if, unless);
   }
}
