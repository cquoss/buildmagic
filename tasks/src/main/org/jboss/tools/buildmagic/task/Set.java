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

import java.io.File;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;

/**
 * Sets variables from nested elements.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Set extends VariableTask
{
   /** The list of nested elements. */
   protected List elements = new LinkedList();

   /**
    * Default to references.
    */
   public Set()
   {
      type = REFERENCE;
   }

   /**
    * Execute the task, set the variable in name.
    */
   public void execute() throws BuildException
   {
      if (name == null)
         throw new MissingAttributeException("name", this);

      // if the type is a ref, then it can be any object
      if (type == REFERENCE)
      {
         Object value = getObject();
         getProject().getReferences().put(name, value);
         log.verbose("set reference " + name + "=" + value);
      }
      else
      {
         // else it must be converted to a string
         String value = getString();
         String ptype = null;
         Map map = null;

         switch (type)
         {
            case PROPERTY :
               map = getProject().getProperties();
               ptype = "project";
               break;

            case USER_PROPERTY :
               map = getProject().getUserProperties();
               ptype = "user";
               break;

            case SYSTEM_PROPERTY :
               map = System.getProperties();
               ptype = "system";
               break;
         }

         map.put(name, value);
         log.verbose("set " + ptype + " property " + name + "=" + value);
      }
   }

   /**
    * Return a single object that represents the elements.
    */
   protected Object getObject()
   {
      Path path = new Path(getProject());

      Iterator iter = elements.iterator();
      while (iter.hasNext())
      {
         Object obj = iter.next();
         if (obj instanceof Path)
         {
            path.append((Path) obj);
         }
         else
         {
            path.append(new RPath(getProject(), obj.toString()));
         }
      }

      return path;
   }

   /**
    * Return a string that represents the elements.
    */
   protected String getString()
   {
      return getObject().toString();
   }

   /**
    * Create a nested path element.
    */
   public Path createPath()
   {
      Path path = new Path(getProject());
      elements.add(path);
      return path;
   }

   /**
    * A Path extentsion that will automatically resolve its contents
    * for property values.
    *
    * <p>NOTE: This is currently disabled due to its parrent prepending
    *          the project root to things it thinks are relative.
    *          Unfortunatly the bits required to fix this are in statics
    *          and/or hidden in the Project object such that it is 
    *          impossible to fix this with out a large amount of pain.
    *
    *          I can't wait for Ant2.
    */
   protected class RPath extends Path
   {
      protected Project project;

      public RPath(final Project project)
      {
         super(project);
         this.project = project;
      }

      public RPath(final Project project, final String path)
      {
         super(project, path);
      }

      public String[] list()
      {
         String[] values = super.list();
         Map props = project.getProperties();

         for (int i = 0; i < values.length; i++)
         {
            values[i] = ResolveProperties.subst(values[i], props, false);
         }

         return values;
      }
   }
}
