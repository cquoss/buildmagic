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

import org.apache.tools.ant.*;

import org.jboss.tools.buildmagic.task.util.TaskLogger;

/**
 * Substitute strings inside of a value and set a property with the result.
 *
 * @version <pre>$Id$</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Subst extends Task
{
   /** Instance logger. */
   protected final TaskLogger log = new TaskLogger(this);

   protected String value;

   protected String property;

   protected String from;

   protected String to;

   public void setValue(String value)
   {
      this.value = value;
   }

   public void setProperty(String property)
   {
      this.property = property;
   }

   public void setFrom(String from)
   {
      this.from = from;
   }

   public void setTo(String to)
   {
      this.to = to;
   }

   /**
    * Substitute sub-strings in side of a string.
    *
    * <p>Copied from trinity:planet57.util.Strings to avoid a circular
    *    dependency from this package to it.
    *
    * @param buff    Stirng buffer to use for substitution (buffer is not reset)
    * @param from    String to substitute from
    * @param to      String to substitute to
    * @param string  String to look for from in
    * @return        Substituted string
    */
   public String subst(StringBuffer buff, String from, String to, String string)
   {
      int begin = 0, end = 0;

      while ((end = string.indexOf(from, end)) != -1)
      {
         // append the first part of the string
         buff.append(string.substring(begin, end));

         // append the replaced string
         buff.append(to);

         // update positions
         begin = end + from.length();
         end = begin;
      }

      // append the rest of the string
      buff.append(string.substring(begin, string.length()));

      return buff.toString();
   }

   /**
    * Substitue the value and set property to the result.
    *
    * @throws BuildException
    */
   public void execute() throws BuildException
   {
      if (value == null)
         throw new BuildException("value is null");
      if (property == null)
         throw new BuildException("property is null");
      if (from == null)
         throw new BuildException("form is null");
      if (to == null)
         throw new BuildException("to is null");

      StringBuffer buff = new StringBuffer();
      log.verbose("before: " + value);

      String result = subst(buff, from, to, value);
      log.verbose("after: " + result);

      getProject().setUserProperty(property, result);
   }
}
