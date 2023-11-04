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

import org.apache.tools.ant.Task;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Base class for other variable related tasks.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class VariableTask extends Task
{
   /** Property variable. */
   public static final int PROPERTY = 0;

   /** User property variable. */
   public static final int USER_PROPERTY = 1;

   /** System property variable. */
   public static final int SYSTEM_PROPERTY = 2;

   /** Reference variable. */
   public static final int REFERENCE = 3;

   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** The name of the variable. */
   protected String name;

   /** The type of variable. */
   protected int type;

   /**
    * Set the name of the variable.
    */
   public void setName(final String name)
   {
      this.name = name;
   }

   /**
    * Set the type of the variable.
    */
   public void setType(String type)
   {
      type = type.toLowerCase();

      if (type.equals("prop") || type.equals("property"))
      {
         this.type = PROPERTY;
      }
      else if (type.equals("userprop") || type.equals("userproperty"))
      {
         this.type = USER_PROPERTY;
      }
      else if (type.equals("sysprop") || type.equals("systemproperty"))
      {
         this.type = SYSTEM_PROPERTY;
      }
      else if (type.equals("ref") || type.equals("reference"))
      {
         this.type = REFERENCE;
      }
      else
      {
         throw new IllegalAttributeException("type", this);
      }
   }
}
