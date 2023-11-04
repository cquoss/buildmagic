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
import java.util.Iterator;

import org.apache.tools.ant.*;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Task to add a filter for properties.
 *
 * @version $Id$
 * @author  Jason Dillon <A href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</A>
 */
public class PropertyFilter extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected boolean all = true;

   public void setAll(boolean all)
   {
      this.all = all;
   }

   public void execute() throws BuildException
   {
      if (all)
      {
         log.verbose("Adding filters for all properties");

         Map props = getProject().getProperties();

         Iterator iter = props.keySet().iterator();
         while (iter.hasNext())
         {
            String key = (String) iter.next();
            String value = (String) props.get(key);

            // log.debug("adding: " + key + "=" + value);
            getProject().getGlobalFilterSet().addFilter(key, value);
         }
      }
   }
}
