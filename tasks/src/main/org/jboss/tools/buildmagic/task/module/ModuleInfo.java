/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.tools.buildmagic.task.module;

import org.apache.tools.ant.BuildException;

/**
 * Displays module information.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ModuleInfo extends AbstractInfo
{
   /**
    * Execute this task.
    *
    * @throws BuildException    Failed to execute task.
    */
   public void execute() throws BuildException
   {
      if (!canContinue())
         return;

      log.info("Project root: " + getProperty("project.root"));
      log.info(" Module root: " + getProperty("module.root"));
   }
}
