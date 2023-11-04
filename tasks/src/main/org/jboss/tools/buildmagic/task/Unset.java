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

import org.apache.tools.ant.BuildException;

/**
 * A task that will unset variables.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Unset extends VariableTask
{
   /**
    * Default to references.
    */
   public Unset()
   {
      type = REFERENCE;
   }

   /**
    * Execute the task, unset the variable in name.
    */
   public void execute() throws BuildException
   {
      if (name == null)
         throw new MissingAttributeException("name", this);

      switch (type)
      {
         case PROPERTY :
            getProject().getProperties().remove(name);
            log.verbose("unset property: " + name);
            break;

         case USER_PROPERTY :
            getProject().getUserProperties().remove(name);
            log.verbose("unset user property: " + name);
            break;

         case SYSTEM_PROPERTY :
            System.getProperties().remove(name);
            log.verbose("unset system property: " + name);
            break;

         case REFERENCE :
            getProject().getReferences().remove(name);
            log.verbose("unset reference: " + name);
            break;
      }
   }
}
