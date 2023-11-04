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

package org.jboss.tools.buildmagic.task.config;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import org.jboss.tools.buildmagic.task.MissingAttributeException;
import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * A helper task to setup a path from a set of library elements.
 *
 * <p>Usage:
 * <xmp>
 *
 *   <libraryset name="library" root="${project.thirdparty}">
 *
 *    <!-- Java API for XML Processing (JAXP) -->
 *    <library vendor="sun" name="jaxp"/>
 *
 *    <!-- IBM Bean Scripting Framework (BSF) -->
 *    <library vendor="ibm" name="bsf"/>
 *
 *    <!-- JUnit -->
 *    <library vendor="junit" name="junit"/>
 *
 *    <!-- Log4j -->
 *    <library vendor="apache" name="log4j"/>
 *
 *  </libraryset>
 *
 * </xmp>
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class LibrarySet
   extends Task 
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String pathID;
   protected File root;
   protected List libraries;

   protected Path classpath;

   public void setPathID(final String pathID)
   {
      this.pathID = pathID;
   }

   public void setRoot(final File root)
   {
      this.root = root;
   }

   public Library createLibrary() 
   {
      Library lib = (Library)getProject().createTask("library");

      if (libraries == null) {
         libraries = new ArrayList();
      }
      libraries.add(lib);

      return lib;
   }

   public Library createModuleLibrary()
   {
      Library lib = (Library)getProject().createTask("modulelibrary");

      if (libraries == null) {
         libraries = new ArrayList();
      }
      libraries.add(lib);

      return lib;
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException    Attributes are not valid.
    */
   protected void validate() throws BuildException 
   {
      if (pathID == null)
         throw new MissingAttributeException("pathID", this);
   }

   /**
    * Execute the task.
    *
    * @throws BuildException    Failed to execute.
    */
   public void execute() throws BuildException 
   {
      // short circuit if we are done
      if (classpath != null) return;

      validate();

      log.debug("pathID: " + pathID);
      log.debug("root: " + root);

      // setup the classpath
      classpath = new Path(getProject());

      if (libraries != null) {
         Iterator iter = libraries.iterator();
         while (iter.hasNext()) {
            Library lib = (Library)iter.next();

            // set the root if we have one, else use the libs
            if (root != null) {
               lib.setRoot(root);
            }

            // execute
            lib.execute();
         
            // append the classpath
            classpath.append(lib.getClasspath());
         }

         log.debug("classpath: " + classpath);
      }

      // set classpath ref
      Map refs = getProject().getReferences();
      refs.put(pathID, classpath);

      // help gc
      pathID = null;
      root = null;
      libraries = null;
   }

   /**
    * For helper tasks.
    */
   public Path getClasspath()
   {
      return classpath;
   }
}
