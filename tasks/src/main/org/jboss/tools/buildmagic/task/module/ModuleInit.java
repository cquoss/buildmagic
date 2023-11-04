/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.tools.buildmagic.task.module;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.jboss.tools.buildmagic.task.MissingAttributeException;
import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * A helper for initializing a modules environment.
 *
 * <p>Usage:
 * <xmp>
 *
 *
 *
 * </xmp>
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ModuleInit extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String name;

   protected String title;

   protected String version;

   public void setName(final String name)
   {
      this.name = name;
   }

   public void setTitle(final String title)
   {
      this.title = title;
   }

   public void setVersion(final String version)
   {
      this.version = version;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException    Attributes are not valid.
    */
   protected void validate() throws BuildException
   {
      if (name == null)
         throw new MissingAttributeException("name", this);
   }

   /**
    * Execute the task.
    *
    * @throws BuildException    Failed to execute.
    */
   public void execute() throws BuildException
   {
      validate();

      getProject().setProperty("module.name", name);
      log.debug("name: " + name);

      if (title == null)
      {
         title = name;
      }
      getProject().setProperty("module.title", name);
      getProject().setProperty("module.Name", name); // deprecated
      log.debug("title: " + title);

      if (version == null)
      {
         version = "DEV";
      }
      getProject().setProperty("module.version", version);
      log.debug("version: " + version);
   }
}
