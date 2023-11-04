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

import java.util.*;

import org.apache.tools.ant.*;

/**
 * A scripted container task.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class ScriptedContainerTask extends ScriptedTask
{
   /** The list of tasks to execute. */
   protected List tasks = new LinkedList();

   /**
    * Set the target to execute when expr is true.
    *
    * @param target  The target to execute.
    */
   public void setTarget(final String target)
   {
      CallTarget call = createCall();
      call.setTarget(target);
      tasks.add(call);
   }

   /**
    * Set the script for this target.
    *
    * @param script  The script for this target.
    */
   public void setScript(final String script)
   {
      Script task = createScript();
      task.addText(script);
      tasks.add(task);
   }

   /**
    * Create a nested <tt>call</tt> target.
    */
   public CallTarget createCall()
   {
      log.verbose("creating nested <call> task");
      CallTarget task = (CallTarget) getProject().createTask("call");
      tasks.add(task);
      log.verbose("tasks: " + tasks);
      return task;
   }

   /**
    * Create a nested <tt>script</tt> target.
    */
   public Script createScript()
   {
      log.verbose("creating nested <script> task");
      Script task = (Script) getProject().createTask("script");
      tasks.add(task);
      log.verbose("tasks: " + tasks);
      return task;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException  Attributes are not valid.
    */
   protected void validate() throws BuildException
   {
      super.validate();

      if (tasks.size() == 0)
         throw new MissingElementException("call' or 'script", this);
   }

   /**
    * Execute all of the sub-tasks.
    */
   protected void executeTasks() throws BuildException
   {
      Iterator iter = tasks.iterator();

      while (iter.hasNext())
      {
         Task task = (Task) iter.next();
         log.debug("executing sub-task: " + task);
         task.execute();
      }
   }
}
