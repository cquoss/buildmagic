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
import java.util.Set;
import java.util.HashSet;

import org.apache.tools.ant.*;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Resolves all property values.  This task adds pseudo late-binding for
 * entries in the <code>Project.getProperties()</code> hashtable.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ResolveProperties extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** Flag to force resolution of properties. */
   protected boolean force; // = false

   /**
    * Set the force flag.
    */
   public void setForce(boolean force)
   {
      this.force = force;
   }

   /**
    * Resolve all property values.
    */
   public void execute() throws BuildException
   {
      log.verbose("Resolving all properties");
      if (force)
         log.verbose("Unset properties will be forced resolve");

      Map props = getProject().getProperties();

      // iterator over all of the properties
      Iterator iter = props.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         String value = (String) props.get(key);

         log.debug("property: " + key);
         log.debug("  before: " + value);
         value = subst(value, props, force);
         log.debug("   after: " + value);

         // replace the property
         props.put(key, value);
      }
   }

   /**
    * Replace any properties in the expression with their values contained
    * in the properties map.
    * 
    * @param expression
    * @param props
    * @param force If set to true, a null property will be replaced with a space.
    * @return
    * @throws BuildException
    */
   public static String subst(final String expression, final Map props, final boolean force) throws BuildException
   {
      StringBuffer buff = new StringBuffer();
      Set occured = new HashSet(10);
      int prev = 0;
      int pos;

      while ((pos = expression.indexOf("$", prev)) >= 0)
      {
         if (pos > 0)
         {
            buff.append(expression.substring(prev, pos));
         }
         if (pos == (expression.length() - 1))
         {
            buff.append('$');
            prev = pos + 1;
         }
         else if (expression.charAt(pos + 1) != '{')
         {
            buff.append(expression.charAt(pos + 1));
            prev = pos + 2;
         }
         else
         {
            int endName = expression.indexOf('}', pos);
            if (endName < 0)
            {
               throw new BuildException("Syntax error in prop: " + expression);
            }

            // get the name of the property
            String n = expression.substring(pos + 2, endName);
            String v = null;

            if (!occured.contains(n))
            {
               occured.add(n);
               if (props.containsKey(n))
               {
                  v = (String) props.get(n);
                  v = subst(v, props, force);
               }
               occured.remove(n);
            }

            if (v == null && !force)
            {
               v = "${" + n + "}";
            }

            if (v != null)
               buff.append(v);

            prev = endName + 1;
         }
      }
      if (prev < expression.length())
      {
         buff.append(expression.substring(prev));
      }

      return buff.toString();
   }

   public static String subst(final String value, final Project project, final boolean force)
   {
      return subst(value, project.getProperties(), force);
   }

   public static String subst(final String value, final Project project)
   {
      return subst(value, project.getProperties(), false);
   }
}
