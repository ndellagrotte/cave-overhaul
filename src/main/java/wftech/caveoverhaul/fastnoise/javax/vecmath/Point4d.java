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
 * A 4 element vector represented by double precision floating point
 * x,y,z,w coordinates.
 *
 */
public class Point4d extends Tuple4d implements java.io.Serializable {

    // Compatible with 1.1
    @Serial
    private static final long serialVersionUID = 1733471895962736949L;


    /**
     * Constructs and initializes a Point4d to (0,0,0,0).
     */
    public Point4d()
    {
       super();
    }


    /**
     * Sets the x,y,z components of this point to the corresponding
     * components of tuple t1.  The w component of this point
     * is set to 1.
     * @param t1 the tuple to be copied
     *
     * @since vecmath 1.2
     */
    public final void set(Tuple3d t1) {
	this.x = t1.x;
	this.y = t1.y;
	this.z = t1.z;
	this.w = 1.0;
    }


    /**
   * Returns the distance between this point and point p1.
   * @param p1 the first point
   * @return the distance between these this point and point p1.
   */
    public final double distance(Point4d p1)
    {
      double dx, dy, dz, dw;

      dx = this.x-p1.x;
      dy = this.y-p1.y;
      dz = this.z-p1.z;
      dw = this.w-p1.w;
      return Math.sqrt(dx*dx+dy*dy+dz*dz+dw*dw);
    }


}
