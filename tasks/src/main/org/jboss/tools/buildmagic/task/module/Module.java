package org.jboss.tools.buildmagic.task.module;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Module implements NamedElement
{
   private String name;
   
   private List dependencies = new LinkedList();
   
   private boolean externalBuild;

   public Module( )
   {

   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public boolean isExternalBuild()
   {
      return externalBuild;
   }

   public void setExternalBuild(boolean externalBuild)
   {
      this.externalBuild = externalBuild;
   }

   public void addDependency(String name)
   {
      dependencies.add(name);
   }

   public void setDependencies(String dependencies)
   {
      StringTokenizer stok = new StringTokenizer(dependencies, ",");
      while (stok.hasMoreTokens())
      {
         addDependency(stok.nextToken().trim());
      }
   }
   
   public List getDependencies()
   {
      return this.dependencies;
   }

   public String toString()
   {
      return "{ name=" + name + ", depends=" + dependencies + " }";
   }

}
