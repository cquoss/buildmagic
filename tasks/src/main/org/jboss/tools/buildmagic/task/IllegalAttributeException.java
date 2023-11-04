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
 * Thrown to indicate that a task attribute was set to an illegal value.
 *
 * @version $Id$
 * @author  Jason Dillon <A href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</A>
 */
public class IllegalAttributeException extends BuildException
{
   protected static String format(String name)
   {
      return "Illegal value for attribute '" + name + "'";
   }

   protected static String format(String name, String msg)
   {
      return format(name) + ": " + msg;
   }

   public IllegalAttributeException(String name, String msg, Location location)
   {
      super(format(name, msg), location);
   }

   public IllegalAttributeException(String name, String msg, Task task)
   {
      super(format(name, msg), task.getLocation());
   }

   public IllegalAttributeException(String name, Task task)
   {
      super(format(name), task.getLocation());
   }

   public IllegalAttributeException(String name, Location location)
   {
      super(format(name), location);
   }

   public IllegalAttributeException(String name)
   {
      super(format(name));
   }
}
