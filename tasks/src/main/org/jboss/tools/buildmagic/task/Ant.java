/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.jboss.tools.buildmagic.task;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;

import org.apache.tools.ant.taskdefs.Property;
   
/**
 * Call Ant in a sub-project
 *
 *  <pre>
 *    <target name="foo" depends="init">
 *    <ant antfile="build.xml" target="bar" >
 *      <property name="property1" value="aaaaa" />
 *      <property name="foo" value="baz" />
 *     </ant>
 *  </target>
 *
 * <target name="bar" depends="init">
 *    <echo message="prop is ${property1} ${foo}" />
 * </target>
 * </pre>
 *
 *
 * @author costin@dnt.ro
 */
public class Ant
   extends org.apache.tools.ant.taskdefs.Ant
{
   protected File dir = null;
   protected String antFile = null;
   protected String target = null;
   protected String output = null;
   protected Vector properties=new Vector();
   protected boolean inheritAll = true;
   
   protected Project project;
   
   public static final String DEFAULT_ANT_BUILD_FILE = "build.xml";

   public void init() {
      project = new Project();
      project.setJavaVersionProperty();
      project.addTaskDefinition("property", 
                           (Class)getProject().getTaskDefinitions().get("property"));
   }

   protected void reinit() {
      init();
      for (int i=0; i<properties.size(); i++) {
         Property p = (Property) properties.elementAt(i);
         Property newP = (Property) project.createTask("property");
         newP.setName(p.getName());
         if (p.getValue() != null) {
            newP.setValue(p.getValue());
         }
         if (p.getFile() != null) {
            newP.setFile(p.getFile());
         } 
         if (p.getResource() != null) {
            newP.setResource(p.getResource());
         }
         properties.setElementAt(newP, i);
      }
   }

   protected void initializeProject() {
      Vector listeners = getProject().getBuildListeners();
      for (int i = 0; i < listeners.size(); i++) {
         project.addBuildListener((BuildListener)listeners.elementAt(i));
      }

      if (output != null) {
         try {
            PrintStream out = new PrintStream(new FileOutputStream(output));
            DefaultLogger logger = new DefaultLogger();
            logger.setMessageOutputLevel(Project.MSG_INFO);
            logger.setOutputPrintStream(out);
            logger.setErrorPrintStream(out);
            project.addBuildListener(logger);
         }
         catch( IOException ex ) {
            log( "Ant: Can't set output to " + output );
         }
      }

      Hashtable taskdefs = getProject().getTaskDefinitions();
      Enumeration et = taskdefs.keys();
      while (et.hasMoreElements()) {
         String taskName = (String) et.nextElement();
         Class taskClass = (Class) taskdefs.get(taskName);
         project.addTaskDefinition(taskName, taskClass);
      }

      Hashtable typedefs = getProject().getDataTypeDefinitions();
      Enumeration e = typedefs.keys();
      while (e.hasMoreElements()) {
         String typeName = (String) e.nextElement();
         Class typeClass = (Class) typedefs.get(typeName);
         project.addDataTypeDefinition(typeName, typeClass);
      }

      // set user-define properties
      Hashtable prop1;
      if (inheritAll) {
         prop1 = getProject().getProperties();
      }
      else {
         prop1 = getProject().getUserProperties();
         project.setSystemProperties();
      }
      
      e = prop1.keys();
      while (e.hasMoreElements()) {
         String arg = (String) e.nextElement();
         String value = (String) prop1.get(arg);

         if (inheritAll) {
            project.setProperty(arg, value);
         }
         else {
            project.setUserProperty(arg, value);
         }
      }
   }

   public void setInheritAll(boolean inherit) {
      inheritAll = inherit;
   }
   
   public File getBuildFile() throws BuildException {
      String filename = antFile;
      if (filename == null) {
         filename = DEFAULT_ANT_BUILD_FILE;
      }
      else {
         filename = ResolveProperties.subst(filename, getProject());
      }

      File file = new File( filename );
      if (!file.isAbsolute()) {
         filename = (new File(dir, filename)).getAbsolutePath();
         file = new File(filename);
         if( ! file.isFile() ) {
            throw new BuildException("Build file " + file + " not found.");
         }
      }

      return file;
   }
   
   /**
    * Do the execution.
    */
   public void execute() throws BuildException {
      try {
         if (project == null) {
            reinit();
         }
        
         if(dir == null) 
            dir = getProject().getBaseDir();

         initializeProject();

         project.setBaseDir(dir);
         project.setUserProperty("basedir" , dir.getAbsolutePath());
            
         // Override with local-defined properties
         Enumeration e = properties.elements();
         while (e.hasMoreElements()) {
            Property p=(Property) e.nextElement();
            p.execute();
         }

         if (antFile == null) {
            antFile = DEFAULT_ANT_BUILD_FILE;
         }
         else {
            antFile = ResolveProperties.subst(antFile, getProject());
         }

         File file = new File(antFile);
         if (!file.isAbsolute()) {
            antFile = (new File(dir, antFile)).getAbsolutePath();
            file = (new File(antFile)) ;
            if( ! file.isFile() ) {
               throw new BuildException("Build file " + file + " not found.");
            }
         }

         project.setUserProperty( "ant.file" , antFile );
         ProjectHelper.getProjectHelper().parse(project, new File(antFile));
            
         if (target == null) {
            target = project.getDefaultTarget();
         }

         // Are we trying to call the target in which we are defined?
         if (project.getBaseDir().equals(getProject().getBaseDir()) &&
             project.getProperty("ant.file").equals(getProject().getProperty("ant.file")) &&
             getOwningTarget() != null &&
             target.equals(this.getOwningTarget().getName())) { 

            throw new BuildException("ant task calling its own parent target");
         }

         project.executeTarget(target);
      } finally {
         // help the gc
         project = null;
      }
   }

   public Project getTargetProject() {
      return project;
   }

   public void addBuildListener(BuildListener l) {
      project.addBuildListener(l);
   }
   
   public void setDir(File d) {
      this.dir = d;
   }

   public void setAntfile(String s) {
      this.antFile = s;
   }

   public void setTarget(String s) {
      this.target = s;
   }

   public void setOutput(String s) {
      this.output = s;
   }

   public Property createProperty() {
      if (project == null) {
         reinit();
      }

      Property p=(Property)project.createTask("property");
      properties.addElement( p );
      return p;
   }
}
