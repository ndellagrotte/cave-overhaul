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
 * A 4 element unit quaternion represented by single precision floating
 * point x,y,z,w coordinates.  The quaternion is always normalized.
 *
 */
public class Quat4f extends Tuple4f implements java.io.Serializable {

  // Combatible with 1.1
  @Serial
  private static final long serialVersionUID = 2675933778405442383L;

  final static double EPS = 0.000001;
  final static double EPS2 = 1.0e-30;


    /**
   * Constructs and initializes a Quat4f from the specified Quat4f.
   * @param q1 the Quat4f containing the initialization x y z w data
   */
  public Quat4f(Quat4f q1)
  {
       super(q1);
  }


    /**
   * Sets the value of this quaternion to the quaternion product of
   * quaternions q1 and q2 (this = q1 * q2).
   * Note that this is safe for aliasing (e.g. this can be q1 or q2).
   * @param q1 the first quaternion
   * @param q2 the second quaternion
   */
  public final void mul(Quat4f q1, Quat4f q2)
  {
    if (this != q1 && this != q2) {
      this.w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;
      this.x = q1.w*q2.x + q2.w*q1.x + q1.y*q2.z - q1.z*q2.y;
      this.y = q1.w*q2.y + q2.w*q1.y - q1.x*q2.z + q1.z*q2.x;
      this.z = q1.w*q2.z + q2.w*q1.z + q1.x*q2.y - q1.y*q2.x;
    } else {
      float	x, y, w;

      w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;
      x = q1.w*q2.x + q2.w*q1.x + q1.y*q2.z - q1.z*q2.y;
      y = q1.w*q2.y + q2.w*q1.y - q1.x*q2.z + q1.z*q2.x;
      this.z = q1.w*q2.z + q2.w*q1.z + q1.x*q2.y - q1.y*q2.x;
      this.w = w;
      this.x = x;
      this.y = y;
    }
  }


 /**
   * Sets the value of this quaternion to the quaternion product of
   * itself and q1 (this = this * q1).
   * @param q1 the other quaternion
   */
  public final void mul(Quat4f q1)
  {
      float     x, y, w;

       w = this.w*q1.w - this.x*q1.x - this.y*q1.y - this.z*q1.z;
       x = this.w*q1.x + q1.w*this.x + this.y*q1.z - this.z*q1.y;
       y = this.w*q1.y + q1.w*this.y - this.x*q1.z + this.z*q1.x;
       this.z = this.w*q1.z + q1.w*this.z + this.x*q1.y - this.y*q1.x;
       this.w = w;
       this.x = x;
       this.y = y;
  }


 /**
   * Multiplies quaternion q1 by the inverse of quaternion q2 and places
   * the value into this quaternion.  The value of both argument quaternions
   * is preservered (this = q1 * q2^-1).
   * @param q1 the first quaternion
   * @param q2 the second quaternion
   */
  public final void mulInverse(Quat4f q1, Quat4f q2)
  {
      Quat4f  tempQuat = new Quat4f(q2);

      tempQuat.inverse();
      this.mul(q1, tempQuat);
  }



 /**
   * Multiplies this quaternion by the inverse of quaternion q1 and places
   * the value into this quaternion.  The value of the argument quaternion
   * is preserved (this = this * q^-1).
   * @param q1 the other quaternion
   */
  public final void mulInverse(Quat4f q1)
  {
      Quat4f  tempQuat = new Quat4f(q1);

      tempQuat.inverse();
      this.mul(tempQuat);
  }


    /**
   * Sets the value of this quaternion to the quaternion inverse of itself.
   */
  public final void inverse()
  {
    float norm;

    norm = 1.0f/(this.w*this.w + this.x*this.x + this.y*this.y + this.z*this.z);
    this.w *=  norm;
    this.x *= -norm;
    this.y *= -norm;
    this.z *= -norm;
  }


  /**
   * Sets the value of this quaternion to the normalized value
   * of quaternion q1.
   * @param q1 the quaternion to be normalized.
   */
  public final void normalize(Quat4f q1)
  {
    float norm;

    norm = (q1.x*q1.x + q1.y*q1.y + q1.z*q1.z + q1.w*q1.w);

    if (norm > 0.0f) {
      norm = 1.0f/(float)Math.sqrt(norm);
      this.x = norm*q1.x;
      this.y = norm*q1.y;
      this.z = norm*q1.z;
      this.w = norm*q1.w;
    } else {
      this.x = (float) 0.0;
      this.y = (float) 0.0;
      this.z = (float) 0.0;
      this.w = (float) 0.0;
    }
  }


  /**
   * Normalizes the value of this quaternion in place.
   */
  public final void normalize()
  {
    float norm;

    norm = (this.x*this.x + this.y*this.y + this.z*this.z + this.w*this.w);

    if (norm > 0.0f) {
      norm = 1.0f / (float)Math.sqrt(norm);
      this.x *= norm;
      this.y *= norm;
      this.z *= norm;
      this.w *= norm;
    } else {
      this.x = (float) 0.0;
      this.y = (float) 0.0;
      this.z = (float) 0.0;
      this.w = (float) 0.0;
    }
  }


  /**
   * Sets the value of this quaternion to the rotational component of
   * the passed matrix.
   * @param m1 the Matrix4f
   */
   public final void set(Matrix4f m1)
   {
       float ww = 0.25f*(m1.m00 + m1.m11 + m1.m22 + m1.m33);

       if (ww >= 0) {
	   if (ww >= EPS2) {
	       this.w = (float) Math.sqrt((double)ww);
	       ww =  0.25f/this.w;
	       this.x = (m1.m21 - m1.m12)*ww;
	       this.y = (m1.m02 - m1.m20)*ww;
	       this.z = (m1.m10 - m1.m01)*ww;
	       return;
	   }
       } else {
	   this.w = 0;
	   this.x = 0;
	   this.y = 0;
	   this.z = 1;
	   return;
       }

       this.w = 0;
       ww = -0.5f*(m1.m11 + m1.m22);

       if (ww >= 0) {
	   if (ww >= EPS2) {
	       this.x = (float) Math.sqrt((double) ww);
	       ww = 1.0f/(2.0f*this.x);
	       this.y = m1.m10*ww;
	       this.z = m1.m20*ww;
	       return;
	   }
       } else {
	   this.x = 0;
	   this.y = 0;
	   this.z = 1;
	   return;
       }

       this.x = 0;
       ww = 0.5f*(1.0f - m1.m22);

       if (ww >= EPS2) {
	   this.y = (float) Math.sqrt((double) ww);
	   this.z = m1.m21/(2.0f*this.y);
	   return;
       }

       this.y = 0;
       this.z = 1;
   }


    /**
     * Sets the value of this quaternion to the rotational component of
     * the passed matrix.
     * @param m1 the Matrix4d
     */
    public final void set(Matrix4d m1)
    {
	double ww = 0.25*(m1.m00 + m1.m11 + m1.m22 + m1.m33);

	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.w = (float) Math.sqrt(ww);
		ww = 0.25/this.w;
		this.x = (float) ((m1.m21 - m1.m12)*ww);
		this.y = (float) ((m1.m02 - m1.m20)*ww);
		this.z = (float) ((m1.m10 - m1.m01)*ww);
		return;
	    }
	} else {
	    this.w = 0;
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.w = 0;
	ww = -0.5*(m1.m11 + m1.m22);
	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.x = (float) Math.sqrt(ww);
		ww = 0.5/this.x;
		this.y = (float)(m1.m10*ww);
		this.z = (float)(m1.m20*ww);
		return;
	    }
	} else {
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.x = 0;
	ww = 0.5*(1.0 - m1.m22);
	if (ww >= EPS2) {
	    this.y = (float) Math.sqrt(ww);
	    this.z = (float) (m1.m21/(2.0*(double)(this.y)));
	    return;
	}

	this.y = 0;
	this.z = 1;
    }


    /**
     * Sets the value of this quaternion to the rotational component of
     * the passed matrix.
     * @param m1 the Matrix3f
     */
    public final void set(Matrix3f m1)
    {
	float ww = 0.25f*(m1.m00 + m1.m11 + m1.m22 + 1.0f);

	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.w = (float) Math.sqrt(ww);
		ww = 0.25f/this.w;
		this.x = (m1.m21 - m1.m12)*ww;
		this.y = (m1.m02 - m1.m20)*ww;
		this.z = (m1.m10 - m1.m01)*ww;
		return;
	    }
	} else {
	    this.w = 0;
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.w = 0;
	ww = -0.5f*(m1.m11 + m1.m22);
	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.x = (float) Math.sqrt((double) ww);
		ww = 0.5f/this.x;
		this.y = m1.m10*ww;
		this.z = m1.m20*ww;
		return;
	    }
	} else {
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.x = 0;
	ww =  0.5f*(1.0f - m1.m22);
	if (ww >= EPS2) {
	    this.y = (float) Math.sqrt(ww);
	    this.z = m1.m21/(2.0f*this.y);
	    return;
	}

	this.y = 0;
	this.z = 1;
    }


    /**
     * Sets the value of this quaternion to the rotational component of
     * the passed matrix.
     * @param m1 the Matrix3d
     */
    public final void set(Matrix3d m1)
    {
	double ww = 0.25*(m1.m00 + m1.m11 + m1.m22 + 1.0f);

	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.w = (float) Math.sqrt(ww);
		ww = 0.25/this.w;
		this.x = (float) ((m1.m21 - m1.m12)*ww);
		this.y = (float) ((m1.m02 - m1.m20)*ww);
		this.z = (float) ((m1.m10 - m1.m01)*ww);
		return;
	    }
	} else {
	    this.w = 0;
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.w = 0;
	ww = -0.5*(m1.m11 + m1.m22);
	if (ww >= 0) {
	    if (ww >= EPS2) {
		this.x = (float) Math.sqrt(ww);
		ww = 0.5/this.x;
		this.y = (float) (m1.m10*ww);
		this.z = (float) (m1.m20*ww);
		return;
	    }
	} else {
	    this.x = 0;
	    this.y = 0;
	    this.z = 1;
	    return;
	}

	this.x = 0;
	ww = 0.5*(1.0 - m1.m22);
	if (ww >= EPS2) {
	    this.y = (float) Math.sqrt(ww);
	    this.z = (float) (m1.m21/(2.0*(double)(this.y)));
	    return;
	}

	this.y = 0;
	this.z = 1;
    }


    /**
     * Sets the value of this quaternion to the equivalent rotation
     * of the AxisAngle argument.
     * @param a  the AxisAngle to be emulated
     */
    public final void set(AxisAngle4f a)
    {
	float mag,amag;
	// Quat = cos(theta/2) + sin(theta/2)(roation_axis)
	amag = (float)Math.sqrt( a.x*a.x + a.y*a.y + a.z*a.z);
	if (amag < EPS ) {
	    w = 0.0f;
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;
	} else {
	    amag = 1.0f/amag;
	    mag = (float)Math.sin(a.angle/2.0);
	    w = (float)Math.cos(a.angle/2.0);
	    x = a.x*amag*mag;
	    y = a.y*amag*mag;
	    z = a.z*amag*mag;
	}
    }


    /**
     * Sets the value of this quaternion to the equivalent rotation
     * of the AxisAngle argument.
     * @param a  the AxisAngle to be emulated
     */
    public final void set(AxisAngle4d a)
    {
	float mag,amag;
	// Quat = cos(theta/2) + sin(theta/2)(roation_axis)

	amag = (float)(1.0/Math.sqrt( a.x*a.x + a.y*a.y + a.z*a.z));

	if (amag < EPS ) {
	    w = 0.0f;
	    x = 0.0f;
	    y = 0.0f;
	    z = 0.0f;
	} else {
	    amag = 1.0f/amag;
	    mag = (float)Math.sin(a.angle/2.0);
	    w = (float)Math.cos(a.angle/2.0);
	    x = (float)a.x*amag*mag;
	    y = (float)a.y*amag*mag;
	    z = (float)a.z*amag*mag;
	}

    }


}




