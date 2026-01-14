/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package wftech.caveoverhaul.fastnoise.javax.vecmath;


import java.io.Serial;

/**
 * A 3 element point that is represented by single precision floating point
 * x,y,z coordinates.
 *
 */
public class Point3f extends Tuple3f implements java.io.Serializable {


    // Compatible with 1.1
    @Serial
    private static final long serialVersionUID = -8689337816398030143L;


    /**
     * Constructs and initializes a Point3f to (0,0,0).
     */
    public Point3f()
    {
        super();
    }


    /**
   * Computes the distance between this point and point p1.
   * @param p1 the other point
   * @return the distance
   */
  public final float distance(Point3f p1)
    {
      float  dx, dy, dz;

      dx = this.x-p1.x;
      dy = this.y-p1.y;
      dz = this.z-p1.z;
      return (float) Math.sqrt(dx*dx+dy*dy+dz*dz);
    }


}
