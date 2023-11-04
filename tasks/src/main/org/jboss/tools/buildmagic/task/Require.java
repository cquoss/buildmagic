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
import org.apache.tools.ant.types.*;

import java.io.*;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Task to check for required resources and complain if they are not found.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Require extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** Required property name */
   protected String property;

   /** Required class name */
   protected String classname;

   /** Required resource name */
   protected String resource;

   /** Required file */
   protected File file;

   /** The classpath to use when looking for classes or resources. */
   protected Path classpath;

   /** The class loaded for the specified classpath. */
   protected AntClassLoader loader;

   /** The message buffer. */
   protected StringBuffer message;

   /**
    * Set the message.
    */
   public void setMessage(final String text)
   {
      addText(text);
   }

   /**
    * Add some text to the message.
    */
   public void addText(final String text)
   {
      if (message == null)
         message = new StringBuffer();
      message.append(text);
   }

   /**
    * Set the property name.
    *
    * @param property   Property name.
    */
   public void setProperty(final String property)
   {
      this.property = property;
   }

   /**
    * Set the classname.
    *
    * @param classname  Class name.
    */
   public void setClass(final String classname)
   {
      this.classname = classname;
   }

   /**
    * Set the resource name.
    *
    * @param resource   Resource name.
    */
   public void setResource(final String resource)
   {
      this.resource = resource;
   }

   /**
    * Set the file.
    *
    * @param file    File.
    */
   public void setFile(final File file)
   {
      this.file = file;
   }

   /**
    * Set the classpath.
    *
    * @param classpath  Class path.
    */
   public void setClasspath(Path classpath)
   {
      if (this.classpath == null)
      {
         this.classpath = classpath;
      }
      else
      {
         this.classpath.append(classpath);
      }
   }

   /**
    * Create a nested <tt>Path</tt> element.
    */
   public Path createClasspath()
   {
      if (this.classpath == null)
      {
         this.classpath = new Path(getProject());
      }
      return this.classpath.createPath();
   }

   /**
    * Use a reference classpath.
    */
   public void setClasspathRef(Reference r)
   {
      createClasspath().setRefid(r);
   }

   /**
    * Execute this task.
    *
    * @exception BuildException  Missing required resource.
    */
   public void execute() throws BuildException
   {
      if ((property != null) && !checkProperty(property))
      {
         missingRequired("property", property);
      }

      if ((file != null) && !checkFile(file))
      {
         missingRequired("file", file);
      }

      // load the custom class path if it is not null
      if (classpath != null)
      {
         this.loader = new AntClassLoader(getProject(), classpath, false);
      }

      if ((resource != null) && !checkResource(resource))
      {
         missingRequired("resource", resource);
      }

      if ((classname != null) && !checkClass(classname))
      {
         missingRequired("classname", classname);
      }
   }

   /**
    * Throws a build exception for the given type & value.
    *
    * @param type    Required type.
    * @param value   Required type value.
    *
    * @exception BuildException  Missing required.
    */
   protected void missingRequired(final String type, final Object value) throws BuildException
   {
      StringBuffer buff = new StringBuffer();

      // Might want to allow user to pass in format string for generating
      // message.

      if (message == null)
      {
         buff.append("Missing required ").append(type).append(": ").append(value);
      }
      else
      {
         buff.append(ResolveProperties.subst(message.toString(), getProject().getProperties(), false));
      }

      throw new BuildException(buff.toString());
   }

   /**
    * Check if a property has been set.
    *
    * @param property   Property name.
    * @return           True if property exists.
    */
   protected boolean checkProperty(final String property)
   {
      boolean exists;

      // look in system first
      exists = System.getProperty(property, null) != null;

      // next look in project
      if (!exists)
      {
         Project project = getProject();

         // regular property first
         exists = project.getProperty(property) != null;

         // then user property
         if (!exists)
         {
            exists = project.getUserProperty(property) != null;
         }
      }

      return exists;
   }

   /**
    * Check if a file exists.
    *
    * @param file    File name.
    * @return        True if file exists.
    */
   protected boolean checkFile(final File file)
   {
      return file.exists();
   }

   /**
    * Check if a class exists.
    *
    * @param classname  Class name.
    * @return           True if class exists.
    */
   protected boolean checkClass(final String classname)
   {
      try
      {
         if (loader != null)
         {
            loader.loadClass(classname);
         }
         else
         {
            ClassLoader cl = this.getClass().getClassLoader();

            // Can return null to represent the bootstrap class loader.
            // see API docs of Class.getClassLoader.
            if (cl != null)
            {
               cl.loadClass(classname);
            }
            else
            {
               Class.forName(classname);
            }
         }
         return true;
      }
      catch (ClassNotFoundException e)
      {
         log.verbose("Class not found: " + classname);
         return false;
      }
      catch (NoClassDefFoundError e)
      {
         log.verbose("Class cound not be loaded: " + classname);
         return false;
      }
   }

   /**
    * Check if a resource is available.
    *
    * @param resource   Resource name.
    * @return           True if resource exists.
    */
   protected boolean checkResource(final String resource)
   {
      if (loader != null)
      {
         return (loader.getResourceAsStream(resource) != null);
      }
      else
      {
         ClassLoader cl = this.getClass().getClassLoader();
         if (cl != null)
         {
            return (cl.getResourceAsStream(resource) != null);
         }
         else
         {
            return (ClassLoader.getSystemResourceAsStream(resource) != null);
         }
      }
   }
}
