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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

import org.jboss.tools.buildmagic.task.MissingAttributeException;
import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * A helper task to setup library the properties and path
 * elements for using a thirdparty library.  
 *
 * <p>This will setup the following properties:
 * <ul>
 *   <li><tt><em>vendor</em>.<em>name</em>.root</tt>
 *   <li><tt><em>vendor</em>.<em>name</em>.lib</tt>
 * </ul>
 *
 * <p>As well as the following path, based on set attributes
 *    and nested elements:
 * <ul>
 *   <li><tt><em>vendor</em>.<em>name</em>.classpath</tt>
 *
 * <p>Usage:
 * <xmp>
 *
 *   <library vendor="sun" name="jmx" root="${project.thirdparty}">
 *     <classpath>
 *       <include name="*.jar"/>
 *     </classpath>
 *   </library>
 *
 *   <library vendor="sun" name="jaxp" root="${project.thirdparty}">
 *     <include name="*.jar"/>
 *   </library>
 *
 *   <library vendor="sun" name="jaax" root="${project.thirdparty}" includes="*.jar"/>
 *
 * </xmp>
 *
 * <p>If no includes/excludes or classpath elements are specified,
 *    then a default includes of "*" is used.
 *  
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Library
   extends Task 
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String vendor;
   protected String name;
   protected File root;
   protected FileSet classpathSet;

   protected Path classpath;

   public void setVendor(final String vendor)
   {
      this.vendor = vendor;
   }

   public void setName(final String name)
   {
      this.name = name;
   }

   public void setRoot(final File root)
   {
      this.root = root;
   }

   public void setIncludes(final String include)
   {
      // add to classpath
      FileSet fs = createClasspath();
      PatternSet.NameEntry entry = fs.createInclude();
      entry.setName(include);
   }

   public void setExcludes(final String exclude)
   {
      // add to classpath
      FileSet fs = createClasspath();
      PatternSet.NameEntry entry = fs.createExclude();
      entry.setName(exclude);
   }

   public FileSet createClasspath() 
   {
      if (classpathSet == null) {
         classpathSet = new FileSet();
      }

      return classpathSet;
   }

   /** Helpers */
   public PatternSet.NameEntry createInclude() 
   {
      return createClasspath().createInclude();
   }

   /** Helpers */
   public PatternSet.NameEntry createExclude() 
   {
      return createClasspath().createExclude();
   }

   /**
    * Validate the attributes for this task.
    *
    * @throws BuildException    Attributes are not valid.
    */
   protected void validate() throws BuildException 
   {
      if (vendor == null)
         throw new MissingAttributeException("vendor", this);
      if (name == null)
         throw new MissingAttributeException("name", this);
      if (root == null)
         throw new MissingAttributeException("root", this);
   }

   protected File getVendorDir()
   {
	  File vendorDir = new File( root, vendor);
	  File vendorNameDir = new File( vendorDir, name );
	  return vendorNameDir;
   }

   protected File getLibraryDir()
   {
	  File libraryDir = new File( getVendorDir(), "lib" );
	  return libraryDir;
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

      log.debug("vendor: " + vendor);
      log.debug("name: " + name);
      log.debug("root: " + root);
      
      File vendorDir = getVendorDir();
      getProject().setProperty(vendor + "." + name + ".root", vendorDir.toString());
      log.debug("vendorDir: " + vendorDir);

      // make lib dir
      File libDir = getLibraryDir();
      getProject().setProperty(vendor + "." + name + ".lib", libDir.toString());
      log.debug("libDir: " + libDir);

      // assemple classpath
      if (classpathSet == null) {
         setIncludes("*");
      }

      log.debug("classpathSet: " + classpathSet);
      classpathSet.setDir(libDir);
      classpath = new Path(getProject());
      classpath.addFileset(classpathSet);
      log.debug("classpath: " + classpath);

      // set classpath ref
      Map refs = getProject().getReferences();
      refs.put(vendor + "." + name + ".classpath", classpath);

      // help gc
      vendor = null;
      name = null;
      root = null;
      classpathSet = null;
   }

   /**
    * For helper tasks.
    */
   public Path getClasspath()
   {
      return classpath;
   }
}
