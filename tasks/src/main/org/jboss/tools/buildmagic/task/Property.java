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
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.Path;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * A custom property task, which adds some additional behavior.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Property extends org.apache.tools.ant.taskdefs.Property
{
   public static final int STRING = 0;

   public static final int INTEGER = 1;

   public static final int FLOAT = 2;

   public static final int BOOLEAN = 3;

   public static final int PATH = 4;

   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String type;

   protected String separator;

   protected boolean override;

   protected boolean filter;

   protected boolean resolve;

   protected boolean force;

   protected boolean system;

   //
   // HACK
   //
   protected FindRoot findRoot;

   public void setType(final String type)
   {
      this.type = type;
   }

   public void setSeparator(final String sep)
   {
      this.separator = sep;
   }

   public void setOverride(final boolean override)
   {
      this.override = override;
   }

   public void setFilter(final boolean filter)
   {
      this.filter = filter;
   }

   public void setSystem(final boolean system)
   {
      this.system = system;
   }

   public void setResolve(final String resolve)
   {
      if (resolve.equals("force"))
      {
         this.resolve = true;
         this.force = true;
      }
      else
      {
         this.resolve = new Boolean(resolve).booleanValue();
      }
   }

   //
   // HACK
   //
   public FindRoot createFindRoot()
   {
      findRoot = new FindRoot(log);
      return findRoot;
   }

   /**
    * Add some raw text to the property.
    *
    * @param text    A line of text to add to the property.
    */
   public void addText(final String text)
   {
      if (value == null)
      {
         value = text;
      }
      else
      {
         value += text;
      }
   }

   /**
    * Create an <tt>Element</tt> element.
    */
   public Element createElement()
   {
      return new Element();
   }

   /**
    * Create an <em>appending</em> <tt>Element</tt>.
    */
   public Element createAppend()
   {
      return new Element(Element.APPEND);
   }

   /**
    * Create a <em>prepending</em> <tt>Element</tt>.
    */
   public Element createPrepend()
   {
      return new Element(Element.PREPEND);
   }

   public void execute() throws BuildException
   {
      //
      // HACK
      //
      if (findRoot != null)
      {
         findRoot.setProperty(getName());
         File root = findRoot.getRoot();
         value = root.getAbsolutePath();
      }

      if (resolve)
      {
         log.debug("resolving property: " + name + "(force: " + force + ")");
         Hashtable props = getProject().getProperties();
         value = ResolveProperties.subst(value, props, force);
      }

      // perform any type converstions
      convertType();

      if (system)
      {
         log.verbose("setting system proeprty: " + name + "=" + value);
         System.setProperty(name, value);
         getProject().setUserProperty(name, value);
      }

      // perform the default operation
      super.execute();

      if (filter)
      {
         log.debug("adding filter for property: " + name);
         getProject().getGlobalFilterSet().addFilter(name, value);
      }
   }

   protected void addProperty(String n, String v)
   {
      if (userProperty)
      {
         if (override || getProject().getUserProperty(n) == null)
         {
            getProject().setUserProperty(n, v);
            if (override)
               log.debug("overrode user property value for " + n);
         }
         else
         {
            log.debug("Override ignored for " + n);
         }
      }
      else
      {
         if (override || getProject().getProperty(n) == null)
         {
            getProject().setProperty(n, v);
            if (override)
               log.debug("overrode property value for " + n);
         }
         else
         {
            log.debug("Override ignored for " + n);
         }
      }
   }

   /**
    * Parse the type attribute and return a type code.
    */
   protected int parseType() throws BuildException
   {
      log.debug("parsing type: " + type);

      if (type == null || type.equals("string"))
      {
         return STRING;
      }
      else if (type.equals("integer"))
      {
         return INTEGER;
      }
      else if (type.equals("float"))
      {
         return FLOAT;
      }
      else if (type.equals("boolean"))
      {
         return BOOLEAN;
      }
      else if (type.equals("path") || type.equals("file") || type.equals("dir"))
      {
         return PATH;
      }
      else
      {
         throw new BuildException("invalid property type: " + type);
      }
   }

   protected void convertType() throws BuildException
   {
      int code = parseType();

      switch (code)
      {
         case INTEGER :
            try
            {
               log.debug("converting value to an integer");
               value = new Long(value).toString();
            }
            catch (Exception e)
            {
               throw new BuildException("property value is not an integer: " + value);
            }
            break;

         case FLOAT :
            try
            {
               log.debug("converting value to an floating point");
               value = new Double(value).toString();
            }
            catch (Exception e)
            {
               throw new BuildException("property value is not a floating point number: " + value);
            }
            break;

         case BOOLEAN :
            value = new Boolean(value).toString();
            break;

         case PATH :
            log.debug("converting value to a path");
            // do not specify a project here, so properties that have not been
            // set yet will not have the project basedir prepended to them.
            Path path = new Path(/*project*/null, value);
            value = path.toString();
            break;

         default :
            // no converstion
      }

      log.debug("converted value: " + value);
   }

   /**
    * Nested element to modify the current property value.
    */
   public class Element
   {
      public static final int APPEND = 0;

      public static final int PREPEND = 1;

      protected int mode = APPEND;

      public Element()
      {
      }

      public Element(final int mode)
      {
         this.mode = mode;
      }

      public void setMode(final String mode) throws BuildException
      {
         if (mode.equals("append"))
         {
            this.mode = APPEND;
         }
         else if (mode.equals("prepend"))
         {
            this.mode = PREPEND;
         }
         else
         {
            throw new BuildException("Invalid property element mode: " + mode);
         }
      }

      public int getMode()
      {
         return mode;
      }

      protected void appendCurrentValue(final StringBuffer buff)
      {
         if (value != null)
         {
            buff.append(value);
            if (separator != null)
               buff.append(separator);
         }
      }

      public void setValue(final String s)
      {
         StringBuffer buff = new StringBuffer();

         switch (mode)
         {
            case APPEND :
               appendCurrentValue(buff);
               buff.append(s);
               break;

            case PREPEND :
               buff.append(s);
               appendCurrentValue(buff);
               break;

            default :
               throw new BuildException("Invalid property element mode: " + mode);
         }

         log.debug("new value: " + buff);
         value = buff.toString();
      }
   }
}
