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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.ant.BuildException;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;

/**
 * Execute a script.
 *
 * @version $Id$
 * @author  Jason Dillon <A href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</A>
 */
public class Script extends ScriptedTask
{
   /** The script text to execute. */
   protected StringBuffer script = new StringBuffer();

   /**
    * Load the script from an external file.
    *
    * @param file    The file to load the source from.
    */
   public void setSrc(final File file) throws BuildException
   {
      log.verbose("reading script file: " + file);

      if (!file.exists())
         throw new BuildException("file not found: " + file);

      int count = (int) file.length();
      byte data[] = new byte[count];

      try
      {
         InputStream in = new BufferedInputStream(new FileInputStream(file));
         in.read(data);
         in.close();
      }
      catch (IOException e)
      {
         throw new BuildException(e);
      }

      script.append(new String(data));
   }

   /**
    * Add some lines to the script.
    *
    * @param test    A line of text to add to the script.
    */
   public void addText(final String text)
   {
      script.append(text);
   }

   /**
    * Execute the script.
    *
    * @param engine  The scripting engine.
    */
   protected void execute(final BSFEngine engine) throws BSFException, BuildException
   {
      // execute the script
      engine.exec("<SCRIPT>", 0, 0, script.toString());
   }
}
