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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Find a root directory by searching upwards for a suffix.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FindRoot
   extends Task
{
   protected TaskLogger log;
   protected String basedir = ".";
   protected String suffix;
   protected String property;

   public FindRoot() {
      this.log = new TaskLogger(this);
   }
   
   public FindRoot(TaskLogger log) {
      this.log = log;
   }
   
   public void setSuffix(String suffix) {
      this.suffix = suffix;
   }

   public void setBasedir(String basedir) {
      this.basedir = basedir;
   }

   public void setProperty(String property) {
      this.property = property;
   }
   
   public File getRoot() throws BuildException {
      if (suffix == null)
         throw new MissingAttributeException("suffix", this);
      if (property == null)
         throw new MissingAttributeException("property", this);

      File root = findRoot(basedir, suffix);
      log.verbose("using root: " + root);

      return root;
   }

   /**
    * Execute the task.
    */
   public void execute() throws BuildException {
      File root = getRoot();
      getProject().getProperties().put(property, root.getAbsolutePath());
   }
   
   private File getParentFile(File file) {
      String filename = file.getAbsolutePath();
      file = new File(filename);
      filename = file.getParent();
      return (filename == null) ? null : new File(filename);
   }
   
   private File findRoot(String start, String suffix)
      throws BuildException
   {
      log.verbose("Searching for " + suffix + " ...");

      File parent = new File(new File(start).getAbsolutePath());
      File file = new File(parent, suffix);
        
      // check if the target file exists in the current directory
      while (!file.exists()) {
         // change to parent directory
         parent = getParentFile(parent);
            
         // if parent is null, then we are at the root of the fs,
         // complain that we can't find the build file.
         if (parent == null) {
            throw new BuildException("Could not locate a " + suffix);
         }
            
         // refresh our file handle
         file = new File(parent, suffix);
      }
        
      return parent;
   }
}
