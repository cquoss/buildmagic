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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.tools.ant.Task;

import org.jboss.tools.buildmagic.task.Tasks;

/**
 * A helper for creating tasks which can have one or more nested conditions.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ConditionalExecution
{
   protected List conditions = new LinkedList();

   protected Task task;

   public ConditionalExecution(final Task task)
   {
      this.task = task;
   }

   public Condition createCondition()
   {
      Condition cond = new Condition();
      conditions.add(cond);
      return cond;
   }

   public boolean canExecute()
   {
      Iterator iter = conditions.iterator();
      boolean can = true;
      while (iter.hasNext())
      {
         Condition cond = (Condition) iter.next();
         if (!cond.canExecute())
            can = false;
      }

      return can;
   }

   public class Condition
   {
      protected String _if;

      protected String unless;

      public void setIf(String property)
      {
         _if = property;
      }

      public void setUnless(String property)
      {
         unless = property;
      }

      public boolean canExecute()
      {
         return Tasks.canExecute(task, _if, unless);
      }
   }
}
