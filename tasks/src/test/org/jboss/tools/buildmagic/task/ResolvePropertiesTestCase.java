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

import java.util.Properties;

import junit.framework.TestCase;

public class ResolvePropertiesTestCase extends TestCase
{

   public void testEvalExpr()
   {
      Properties props = new Properties();
      props.put("atest", "test");
      props.put("test1", "one");
      props.put("test2", "two");

      String expression = "This is test ${test1} ${test2} ${test3}";

      String evaluatedExpr = ResolveProperties.subst(expression, props, true);
      this.assertEquals("This is test one two ", evaluatedExpr);

      evaluatedExpr = ResolveProperties.subst(expression, props, false);
      this.assertEquals("This is test one two ${test3}", evaluatedExpr);

      /*evaluatedExpr = ResolveProperties.evalExpr(expression, props, true);
      this.assertEquals("This is test one two ", evaluatedExpr);
      
      evaluatedExpr = ResolveProperties.evalExpr(expression, props, false);
      this.assertEquals("This is test one two ${test3}", evaluatedExpr);*/

      String nestedExpr = "This is test ${${atest}1} ${test2} ${test3}";

      evaluatedExpr = ResolveProperties.subst(nestedExpr, props, false);
      //this.assertEquals("This is test one two ${test3}", evaluatedExpr);

      //System.out.println(evaluatedExpr);
   }
}
