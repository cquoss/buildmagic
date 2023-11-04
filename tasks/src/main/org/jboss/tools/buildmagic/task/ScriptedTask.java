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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.tools.buildmagic.task.util.TaskLogger;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/**
 * Base class for tasks which use scripting.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class ScriptedTask extends Task implements Cloneable
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** Map of bean name -> bean value */
   protected final Map beans = new HashMap();

   /** The BSF manager object. */
   protected BSFManager manager;

   /** The name of the scripting language to use. */
   protected String language = "jpython";

   /**
    * Set the scripting language that will be used.
    */
   public void setLanguage(final String language)
   {
      this.language = language;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException     Attributes are not valid.
    */
   protected void validate() throws BuildException
   {
      if (language == null)
         throw new MissingAttributeException("language", this);
   }

   /**
    * Get a reference to the BSF manager.
    *
    * @throws BSFException    Failed to create BSF manager.
    */
   protected BSFManager getManager() throws BSFException
   {
      if (manager == null)
      {
         manager = new BSFManager();
         log.debug("created bsf manager: " + manager);
      }

      return manager;
   }

   /**
    * Create a scripting language engine for the configured language.
    *
    * @return  Scripting language engine.
    *
    * @throws BSFException    Failed to create engine.
    */
   protected BSFEngine createEngine() throws BSFException
   {
      BSFManager manager = getManager();

      // update the namespace
      addBeans(getProject().getProperties());
      addBeans(getProject().getUserProperties());
      addBeans(getProject().getTargets());
      addBeans(getProject().getReferences());

      beans.put("project", getProject());

      Iterator iter = beans.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         Object value = beans.get(key);

         manager.declareBean(key, value, value.getClass());
         log.debug("declared bean: " + key + " = " + value);
      }

      BSFEngine engine = manager.loadScriptingEngine(language);
      log.debug("created engine: " + engine);

      return engine;
   }

   /**
    * Return the source name of the script.
    *
    * @return  Source name of the script.
    */
   protected String getSourceName()
   {
      return "<SCRIPT>";
   }

   /**
    * Check if a given string is a valid identifier.
    *
    * @param valie   String to check.
    * @return        True if the string is a valid identifier.
    */
   protected boolean isValidIdentifier(String value)
   {
      boolean valid = value.length() > 0 && Character.isJavaIdentifierStart(value.charAt(0));

      for (int i = 1; valid && i < value.length(); i++)
      {
         valid = Character.isJavaIdentifierPart(value.charAt(i));
      }

      return valid;
   }

   /**
    * Add each element in the given map to the beans map if the key
    * is a valid identifier.
    *
    * @param map  Map to add elements from.
    */
   protected void addBeans(final Map map)
   {
      Iterator iter = map.keySet().iterator();

      while (iter.hasNext())
      {
         String key = (String) iter.next();
         if (isValidIdentifier(key))
         {
            beans.put(key, map.get(key));
         }
         else
         {
            log.debug("not a valid identifier: " + key);
         }
      }
   }

   /**
    * Execute the scripted task.
    *
    * <p>Invokes {@link #execute(BSFEngine)} and handles any
    *    <tt>BSFException</tt>s that are thrown.
    */
   public void execute() throws BuildException
   {
      validate();

      try
      {
         BSFEngine engine = createEngine();
         execute(engine);
      }
      catch (BSFException e)
      {
         Throwable nested = e.getTargetException();

         if (nested != null)
         {
            if (nested instanceof BuildException)
            {
               throw (BuildException) nested;
            }
            else
            {
               nested = e;
            }
         }
         throw new BuildException(nested);
      }
   }

   /**
    * Called to actually execute the scripted task.
    *
    * @param engine  The script specific engine.
    */
   protected abstract void execute(BSFEngine engine) throws BSFException, BuildException;

   /**
    * Return a cloned copy of this object.
    *
    * @return   Cloned object.
    */
   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new InternalError();
      }
   }
}
