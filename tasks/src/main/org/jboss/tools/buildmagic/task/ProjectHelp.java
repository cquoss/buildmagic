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

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import java.io.PrintWriter;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * Show the targets.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ProjectHelp extends Task
{
   protected PrintWriter out = new PrintWriter(System.out);

   protected List headers = new LinkedList();

   protected List footers = new LinkedList();

   public Heading createHeader()
   {
      Heading header = new Heading();
      headers.add(header);
      return header;
   }

   public Heading createFooter()
   {
      Heading footer = new Heading();
      footers.add(footer);
      return footer;
   }

   /**
    * Execute the task.
    */
   public void execute() throws BuildException
   {
      Map targets = getProject().getTargets();
      List names = new LinkedList();
      int maxlen = 0;
      Iterator iter = targets.keySet().iterator();

      while (iter.hasNext())
      {
         String name = (String) iter.next();
         Target target = (Target) targets.get(name);
         String desc = target.getDescription();

         if (desc != null)
         {
            int len = name.length();
            if (len > maxlen)
               maxlen = len;
            names.add(name);
         }
      }

      printHeading(headers);
      printTargets(out, names, maxlen);
      printHeading(footers);

      out.flush();
   }

   protected void printHeading(List list)
   {
      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         Heading heading = (Heading) iter.next();
         heading.setProject(getProject());
         heading.print(out);
      }
   }

   protected String getDescription(String name)
   {
      Target target = (Target) getProject().getTargets().get(name);
      return target.getDescription();
   }

   protected void printTargets(PrintWriter out, List names, int maxlen)
   {
      String spaces = "    ";
      while (spaces.length() < maxlen)
      {
         spaces += spaces;
      }

      Collections.sort(names);

      for (int i = 0; i < names.size(); i++)
      {
         String name = (String) names.get(i);
         String pad = spaces.substring(0, maxlen - name.length() + 4);

         out.print("    ");
         out.print(name);
         out.print(pad);
         out.print(getDescription(name));
         out.println();
      }
   }

   protected class Heading
   {
      protected StringBuffer buff = new StringBuffer();

      protected Project project;

      public void setProject(Project project)
      {
         this.project = project;
      }

      public void setMessage(final String text)
      {
         addText(text);
      }

      public void addText(final String text)
      {
         buff.append(text);
      }

      public void print(PrintWriter out)
      {
         String msg = buff.toString();
         msg = ResolveProperties.subst(msg, project.getProperties(), false);
         out.println(msg);
      }
   }
}
