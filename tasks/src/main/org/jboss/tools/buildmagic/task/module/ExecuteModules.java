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

package org.jboss.tools.buildmagic.task.module;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Property;
import org.jboss.tools.buildmagic.task.AbstractBuildListener;
import org.jboss.tools.buildmagic.task.Ant;
import org.jboss.tools.buildmagic.task.ResolveProperties;
import org.jboss.tools.buildmagic.task.util.TaskLogger;
import org.jboss.util.Strings;

/**
 * Executes a set of modules (sub-projects).
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ExecuteModules extends Task
{

   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   /** The target name to execute. */
   private String target;

   /** The buildfile to use. */
   private String antfile;

   /** The list of module names to exectue. */
   private List moduleNames;

   /** The root directory that contains modules. */
   private String root;

   /** User properties that will be passed to each module. */
   private List properties = new LinkedList();

   /** A internal project to hold property defs. */
   private Project internalProject;

   private String moduleProperty = "module";
   
   private String modulesReference = "modules";

   private String targetProperty = "target";

   /** Skip over modules that do not exist. */
   private boolean skipMissing = false;

   private boolean inheritAll = false;

   /** Tasks to echo some output before and after each module executes. */
   private List headers = new LinkedList();

   private List footers = new LinkedList();

   private List beforeHooks = new LinkedList();

   private List afterHooks = new LinkedList();

   private ModuleBuildListener listener = new ModuleBuildListener();

   private List exportProperties = new LinkedList();

   private String module;

   private boolean threading = false;

   public void setThreading(boolean flag)
   {
      threading = flag;
   }

   public void setExportproperties(String list)
   {
      StringTokenizer stok = new StringTokenizer(list, ",");
      while (stok.hasMoreTokens())
      {
         exportProperties.add(stok.nextToken().trim());
      }
   }

   public String getModulesReference()
   {
      return modulesReference;
   }

   public void setModulesReference(String modulesReference)
   {
      this.modulesReference = modulesReference;
   }

   public void setModuleproperty(String property)
   {
      moduleProperty = property;
   }

   public void setTargetproperty(String property)
   {
      targetProperty = property;
   }

   public void setInheritAll(boolean flag)
   {
      inheritAll = flag;
   }

   /** Setup the internal project. */
   public void init()
   {
      internalProject = new Project();
      internalProject.setJavaVersionProperty();
      Map tasks = getProject().getTaskDefinitions();
      internalProject.addTaskDefinition("property", (Class) tasks.get("property"));
      internalProject.addTaskDefinition("echo", (Class) tasks.get("echo"));
   }

   /** Re-initialize the internal project. */
   protected void reinit()
   {
      init();

      // copy the list of properties to the new internal project
      for (int i = 0; i < properties.size(); i++)
      {
         Property a = (Property) properties.get(i);
         Property b = (Property) internalProject.createTask("property");
         copy(a, b);
         properties.set(i, b);
      }
   }

   /** Copy a property values to another property. */
   protected void copy(final Property a, final Property b)
   {
      b.setName(a.getName());

      if (a.getValue() != null)
      {
         b.setValue(a.getValue());
      }

      if (a.getFile() != null)
      {
         b.setFile(a.getFile());
      }

      if (a.getResource() != null)
      {
         b.setResource(a.getResource());
      }
   }

   /** Set the target name. */
   public void setTarget(final String target)
   {
      this.target = target;
   }

   /** Set the buildfile to use. */
   public void setAntfile(final String antfile)
   {
      this.antfile = antfile;
   }

   /** Set the modules to execute. */
   public void setModules(String names)
   {
      names = ResolveProperties.subst(names, getProject(), true);
      StringTokenizer stok = new StringTokenizer(names, ",");
      moduleNames = new LinkedList();
      while (stok.hasMoreTokens())
      {
         moduleNames.add(stok.nextToken().trim());
      }
   }

   public void setSkipmissing(boolean flag)
   {
      skipMissing = flag;
   }

   /** Set the root. */
   public void setRoot(final String dir) throws BuildException
   {
      this.root = dir;
   }

   /**
    * Execute this task.
    *
    * @throws BuildException    Failed to execute task.
    */
   public void execute() throws BuildException
   {
      // need the root directory
      if (root == null)
      {
         throw new BuildException("Root directory not specified", getLocation());
      }

      List modules = (List) getProject().getReference(this.modulesReference);

      if (modules != null)
      {
         Iterator iter = modules.iterator();
         while (iter.hasNext())
         {
            executeModule((Module) iter.next());
         }
      }
      else if (moduleNames != null)
      {
         Iterator iter = moduleNames.iterator();
         while (iter.hasNext())
         {
            executeModule((String) iter.next(), false);
         }
      }
      else
      {
         throw new BuildException("No module names were specified", getLocation());
      }
   }

   public void executeModule(Module module)
   {
      executeModule( module.getName(), module.isExternalBuild() );
   }
   
   /**
    * Execute a single module.
    */
   public void executeModule(final String module, final boolean externalBuild) throws BuildException
   {
      this.module = module;

      // create and setup the ant task
      final Ant ant = (Ant) getProject().createTask("Ant");
      ant.setInheritAll(inheritAll);
      ant.init();

      // add a property for the name of the module (our name that is)
      internalProject.setProperty(moduleProperty, module);

      // add a property for the target of the module (our name that is)
      String tempTargetName = (target == null) ? "<default>" : target;
      evaluateExpression(tempTargetName);
      internalProject.setProperty(targetProperty, tempTargetName);

      ant.setLocation(getLocation());
      tempTargetName = ResolveProperties.subst(root, getProject());
      tempTargetName = Strings.subst("@MODULE@", module, tempTargetName);
      tempTargetName = Strings.subst("@TARGET@", target, tempTargetName);

      File moduleRoot = new File(tempTargetName);
      log.verbose("module root: " + moduleRoot);
      ant.setDir(moduleRoot);
      ant.setAntfile(antfile);

      if (target != null)
      {
         log.verbose("using target: " + target);
         ant.setTarget(target);
      }

      Map props = getProject().getProperties();

      // Workaround for ant 1.7 compatibility [JBBUILD-432]
      if (props.get("ant.version").toString().contains("1.7"))
      {
         Property antVersion = ant.createProperty();
         antVersion.setName("ant.version");
         antVersion.setValue(props.get("ant.version").toString());
         antVersion.execute();

         Property antCoreLib = ant.createProperty();
         antCoreLib.setName("ant.core.lib");
         antCoreLib.setValue(props.get("ant.core.lib").toString());
         antCoreLib.execute();
      }
      // End workaround

      // see if this is a valid ant file
      try
      {
         if ( ! externalBuild ) 
         {
            ant.getBuildFile();
         }
      }
      catch (BuildException e)
      {
         log.verbose("exception: " + e);
         if (skipMissing)
         {
            listener.skipped(module);
            log.warning("Missing build file; skipping module: " + module);

            props.remove(moduleProperty);
            props.remove(targetProperty);
            return;
         }

         log.error("Missing build file: " + module);
         throw e;
      }

      runHooks( beforeHooks );

      ant.addBuildListener(listener);

      // set up any properties
      Iterator iter = properties.iterator();
      while (iter.hasNext())
      {
         Property a = (Property) iter.next();
         Property b = ant.createProperty();
         copy(a, b);
         a.execute();
      }

      // export any listed property names
      iter = exportProperties.iterator();
      while (iter.hasNext())
      {
         String name = (String) iter.next();
         String value = (String) props.get(name);
         Property p = ant.createProperty();
         p.setName(name);
         p.setValue(value);

         log.verbose("Exported property " + name + "=" + value);
      }

      final String targetName = tempTargetName;

      Runnable runner = new Runnable()
      {
         public void run()
         {
            // execute the task
            printMessages(headers);
            log.verbose("Executing " + targetName + " in module '" + module + "'...");

            if ( externalBuild )
            {
               Echo skipMessage = new Echo( );
               skipMessage.setProject( getProject() );
               skipMessage.setTaskName("echo");
               skipMessage.setMessage("Skipping ant module build due to externalBuild setting.");
               skipMessage.execute();
            }
            else
            {
               ant.execute();
            }

            log.verbose("Finished with " + targetName + " in module '" + module + "'...");
            printMessages(footers);

            runHooks(afterHooks);
         }
      };

      if (threading)
      {
         new Thread(runner, "Module Runner (" + module + ":" + targetName + ")").start();
      }
      else
      {
         runner.run();
      }

   }

   protected void printMessages(List messages)
   {
      Iterator iter = messages.iterator();
      while (iter.hasNext())
      {
         MyEcho header = (MyEcho) iter.next();
         header.execute();
      }
   }

   /** Create a nested property. */
   public Property createProperty()
   {
      Property prop = createProperty(properties);
      return prop;
   }

   /** Create a nested property. */
   public Property createProperty(List list)
   {
      if (internalProject == null)
      {
         reinit();
      }

      Property prop = (Property) internalProject.createTask("property");

      if (list != null)
      {
         list.add(0, prop);
      }

      return prop;
   }

   /** Create a nested header. */
   public Echo createHeader()
   {
      if (internalProject == null)
      {
         reinit();
      }

      MyEcho header = new MyEcho(getProject());
      headers.add(header);
      return header;
   }

   /** Create a nested footer. */
   public Echo createFooter()
   {
      if (internalProject == null)
      {
         reinit();
      }

      MyEcho footer = new MyEcho(getProject());
      footers.add(footer);
      return footer;
   }

   /**
    * Takes a string which may contain properties and
    * resolves the properties to their values.
    * 
    * @param expr
    * @return
    */
   public String evaluateExpression(String expr)
   {
      Map props = getProject().getUserProperties();
      expr = ResolveProperties.subst(expr, props, false);

      props = internalProject.getUserProperties();
      expr = ResolveProperties.subst(expr, props, false);

      props = internalProject.getProperties();
      expr = ResolveProperties.subst(expr, props, false);

      return expr;
   }

   /*public void printProperties(Map props, String header)
   {
      Iterator keys = props.keySet().iterator();
      System.out.println(header);
      while(keys.hasNext())
      {
   	   Object key = keys.next();
   	   System.out.println(key + ":" + props.get(key));
      }
   }*/

   /**
    * Nested echo class to hold header and footer data.
    */
   protected class MyEcho extends Echo
   {
      Project project;

      public MyEcho(Project project)
      {
         this.project = project;
      }

      public void addText(String msg)
      {
         message += msg;
      }

      public void execute() throws BuildException
      {
         String temp = evaluateExpression(message);

         temp = Strings.subst("@MODULE@", module, temp);
         temp = Strings.subst("@TARGET@", ExecuteModules.this.target, temp);

         project.log(temp, logLevel);
      }

      public String getMessage()
      {
         return this.message;
      }
   }

   public Hook createBefore()
   {
      Hook hook = new Hook(this);
      beforeHooks.add(hook);
      return hook;
   }

   public Hook createAfter()
   {
      Hook hook = new Hook(this);
      afterHooks.add(hook);
      return hook;
   }

   protected class Hook
   {
      public ExecuteModules task;

      public String target;

      public Hook(ExecuteModules task)
      {
         this.task = task;
      }

      public void setTarget(String target)
      {
         this.target = target;
      }

      public void execute() throws BuildException
      {
         Project project = task.getProject();
         String t = task.evaluateExpression(target);

         t = Strings.subst("@MODULE@", module, t);
         t = Strings.subst("@TARGET@", ExecuteModules.this.target, t);

         if (project.getTargets().containsKey(t))
         {
            project.executeTarget(t);
         }
         else
         {
            log.verbose("skipping missing hook: " + t);
         }
      }
   }

   protected void runHooks(List list) throws BuildException
   {
      log.verbose("executing hooks");
      log.debug("list: " + list);

      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         Hook hook = (Hook) iter.next();
         log.debug("executing hook: " + hook);
         hook.execute();
      }
   }

   /**
    * This is meant to provide the ability to generate an xml file
    * which contains what modules we have invoked, their targets and
    * output.
    *
    * <p>The goal was to allow this to be applied to a stylesheet
    *    to produce documentaion for the build, only showing the relevant
    *    bits.
    */
   protected class ModuleBuildListener extends AbstractBuildListener
   {
      //
      // hookup xml output here
      //
      public void skipped(String module)
      {
         // System.out.println("skipped module: " + module);
      }

      public void buildStarted(BuildEvent event)
      {
         // System.out.println("started build: " + event);
      }

      public void buildFinished(BuildEvent event)
      {
         // System.out.println("finished build: " + event);
      }

      public void targetStarted(BuildEvent event)
      {
         // System.out.println("started target: " + event.getTarget());
      }

      public void targetFinished(BuildEvent event)
      {
         // System.out.println("finished target: " + event.getTarget());
      }

      public void taskStarted(BuildEvent event)
      {
         // System.out.println("started task: " + event.getTask());
      }

      public void taskFinished(BuildEvent event)
      {
         // System.out.println("finished task: " + event.getTask());
      }

      public void messageLogged(BuildEvent event)
      {
         Throwable t = event.getException();
         int pri = event.getPriority();
         String message = event.getMessage();

         if (t != null)
         {
            // System.out.println("exception: " + t);
         }

         switch (pri)
         {
            case Project.MSG_ERR :
               // System.out.println("error: " + message);
               break;
            case Project.MSG_WARN :
               // System.out.println("warning: " + message);
               break;
            case Project.MSG_INFO :
            case Project.MSG_VERBOSE :
            case Project.MSG_DEBUG :
         }
      }
   }
}
