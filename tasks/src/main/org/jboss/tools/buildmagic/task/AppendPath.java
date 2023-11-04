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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;

/**
 * Append a path to another path
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class AppendPath extends Task
{
   protected List paths = new LinkedList();

   protected String refid;

   public void setRefid(String id)
   {
      this.refid = id;
   }

   /**
    * Create a nested path.
    */
   public Path createPath()
   {
      Path path = new Path(getProject());
      paths.add(path);
      return path;
   }

   /**
    * Create a nested path element.
    */
   public PathElement createPathElement()
   {
      Path path = new Path(getProject());
      paths.add(path);
      return path.createPathElement();
   }

   /**
    * Execute the task.
    */
   public void execute() throws BuildException
   {
      if (refid == null)
         throw new MissingAttributeException("refid", this);

      Path target = (Path) getProject().getReferences().get(refid);
      Iterator iter = paths.iterator();
      while (iter.hasNext())
      {
         Path path = (Path) iter.next();
         target.append(path);
      }
   }
}
