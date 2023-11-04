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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.tools.buildmagic.task.MissingAttributeException;
import org.jboss.tools.buildmagic.task.MissingElementException;
import org.jboss.tools.buildmagic.task.util.ConditionalExecution;
import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Module configuration task.
 *
 * @todo Merge groups and modules. they will share namespace, but
 *       will be easier to work with.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ModuleConfig extends Task
{
   /** Instance logger. */
   private TaskLogger log = new TaskLogger(this);

   /** The list of modules. */
   private LinkedHashMap modules = new LinkedHashMap();

   /** The list of groups. */
   private List groups = new LinkedList();

   /** The name of the property that is to be set. */
   private String property = "modules";

   /** The name of the reference containing the modules. */
   //private String modulesReference = "modules";

   /** The selected groups. */
   private String selected;

   /** The conditional execution helper. */
   private ConditionalExecution cond = new ConditionalExecution(this);

   /** Set the property name. */
   public void setProperty(String property)
   {
      this.property = property;
   }

   /** Set the property name. */
   /*public void setModulesReference(String modulesReference)
   {
      this.modulesReference = modulesReference;
   }*/

   /** Set the selected group. */
   public void setSelected(String selected)
   {
      this.selected = selected;
   }

   /** Create a module. */
   public void addConfiguredModule(Module module)
   {
      modules.put(module.getName(), module);
   }

   /** Create a group. */
   public Group createGroup()
   {
      Group g = new Group( this );
      groups.add(g);
      return g;
   }

   /** Create a condition. */
   public ConditionalExecution.Condition createCondition()
   {
      return cond.createCondition();
   }

   /**
    * Execute this task.
    *
    * @throws BuildException    Failed to execute task.
    */
   public void execute() throws BuildException
   {
      if (selected == null)
         throw new MissingAttributeException("selected", this);
      if (modules.size() == 0)
         throw new MissingElementException("module", this);
      if (groups.size() == 0)
         throw new MissingElementException("group", this);

      if (!cond.canExecute())
         return;

      List moduleList = new LinkedList();
      StringTokenizer stok = new StringTokenizer(selected, ",");
      while (stok.hasMoreTokens())
      {
         String name = stok.nextToken().trim();
         log.verbose("selected group: " + name);
         Group group = getGroup(name);
         if (group == null)
            throw new BuildException("Invalid module group: " + selected);

         log.debug("group: " + group);

         List allModules = group.getModules();
         if (allModules == null)
            throw new BuildException("Invalid group: " + name);

         moduleList.addAll(allModules);
      }
      log.debug("full module list: " + moduleList);

      String moduleListString = generatePropertyValue(moduleList);
      getProject().setProperty(property, moduleListString);
      getProject().addReference(property, moduleList);
      log.verbose("Module list: " + moduleListString);
   }

   /**
    * Generate a comma separated list of the modules.
    */
   protected String generatePropertyValue(List moduleList)
   {
      StringBuffer buff = new StringBuffer();

      Iterator iter = moduleList.iterator();
      while (iter.hasNext())
      {
         Module mod = (Module) iter.next();
         if (mod == null)
            throw new BuildException("Null module in list: " + moduleList);

         buff.append(mod.getName());
         if (iter.hasNext())
         {
            buff.append(",");
         }
      }

      return buff.toString();
   }

   /**
    * Get an element by name.
    */
   protected NamedElement getNamedElement(List list, String name)
   {
      Iterator iter = list.iterator();

      while (iter.hasNext())
      {
         NamedElement elem = (NamedElement) iter.next();
         if (elem.getName().equals(name))
         {
            return elem;
         }
      }

      return null;
   }

   /**
    * Get a module by name.
    */
   protected Module getModule(String name)
   {
      return (Module) modules.get( name );
   }

   /**
    * Get a group by name.
    */
   protected Group getGroup(String name)
   {
      return (Group) getNamedElement(groups, name);
   }

   /////////////////////////////////////////////////////////////////////////
   //                             Nested Elements                         //
   /////////////////////////////////////////////////////////////////////////

   /**
    * A group element.
    */
   protected class Group implements NamedElement
   {
      /** The list of includes. */
      private List includes = new LinkedList();

      /** The conditional execution helper. */
      private ConditionalExecution cond;
      
      private Task parentTask;
      
      private String name;

      public Group(Task parentTask)
      {
         this.parentTask = parentTask;
         cond = new ConditionalExecution( parentTask );
      }

      /** Create a condition. */
      public ConditionalExecution.Condition createCondition()
      {
         return cond.createCondition();
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public Include createInclude()
      {
         Include inc = new Include(this);
         includes.add(inc);
         return inc;
      }

      public void setModules(String modules)
      {
         Include inc = new Include(this);
         inc.setModules(modules);
         includes.add(inc);
      }

      public void setGroups(String groups)
      {
         Include inc = new Include(this);
         inc.setGroups(groups);
         includes.add(inc);
      }

      public List getModules()
      {
         List list = new LinkedList();
         if (!cond.canExecute())
            return list;

         Iterator iter = includes.iterator();
         while (iter.hasNext())
         {
            Include inc = (Include) iter.next();
            log.debug("adding modules from include: " + inc);
            list.addAll(inc.getModules());
         }

         return list;
      }

      public String toString()
      {
         return "{ name=" + name + ", includes=" + includes + " }";
      }

      /**
       * An include elemnt.
       */
      protected class Include
      {
         /** The conditional execution helper. */
         protected ConditionalExecution cond;

         protected Group parentGroup;

         protected String modules = "";

         protected String groups = "";

         public Include(Group parentGroup)
         {
            this.parentGroup = parentGroup;
            cond = new ConditionalExecution(parentGroup.parentTask);
         }

         /** Create a condition. */
         public ConditionalExecution.Condition createCondition()
         {
            return cond.createCondition();
         }

         public void setModules(String modules)
         {
            this.modules = modules;
         }

         public void setGroups(String groups)
         {
            this.groups = groups;
         }

         public List getGroups()
         {
            List list = new LinkedList();
            if (!cond.canExecute())
               return list;

            StringTokenizer stok = new StringTokenizer(groups, ",");
            while (stok.hasMoreTokens())
            {
               String name = stok.nextToken().trim();
               Group group = getGroup(name);
               if (group == null)
                  throw new BuildException("Invalid group include: " + name);

               list.add(group);
               log.debug("adding group: " + group);
            }

            return list;
         }

         public List getModules()
         {
            List list = new LinkedList();
            if (!cond.canExecute())
               return list;

            StringTokenizer stok = new StringTokenizer(modules, ",");
            while (stok.hasMoreTokens())
            {
               String name = stok.nextToken().trim();
               Module module = getModule(name);
               if (module == null)
                  throw new BuildException("Invalid module include: " + name);

               list.add(module);
               log.debug("adding module: " + module);
            }

            List groups = getGroups();
            Iterator iter = groups.iterator();
            while (iter.hasNext())
            {
               Group group = (Group) iter.next();
               list.addAll(group.getModules());
            }

            return list;
         }

         public String toString()
         {
            return "{ modules=" + modules + ", groups=" + groups + " }";
         }
      }
   }
}
