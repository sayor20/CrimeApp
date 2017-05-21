/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package app.sayor.crimeapp.mapproj.com.jhlabs.map.proj;
/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import app.sayor.crimeapp.mapproj.com.jhlabs.map.MapMath;
import android.graphics.PointF;
import app.sayor.crimeapp.mapproj.com.jhlabs.map.AngleFormat;
import app.sayor.crimeapp.mapproj.com.jhlabs.map.MapMath;
import app.sayor.crimeapp.mapproj.com.jhlabs.map.proj.Ellipsoid;

/**
 * The superclass for all map projections
 */
public class Projection implements Cloneable {

	/**
	 * The minimum latitude of the bounds of this projection
	 */
	protected float minLatitude = (float)(-Math.PI/2);

	/**
	 * The minimum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected float minLongitude = (float)-Math.PI;

	/**
	 * The maximum latitude of the bounds of this projection
	 */
	protected float maxLatitude = (float)Math.PI/2;

	/**
	 * The maximum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected float maxLongitude = (float)Math.PI;

	/**
	 * The latitude of the centre of projection
	 */
	protected float projectionLatitude = 0.0f;

	/**
	 * The longitude of the centre of projection
	 */
	protected float projectionLongitude = 0.0f;

	/**
	 * Standard parallel 1 (for projections which use it)
	 */
	protected float projectionLatitude1 = 0.0f;

	/**
	 * Standard parallel 2 (for projections which use it)
	 */
	protected float projectionLatitude2 = 0.0f;

	/**
	 * The projection scale factor
	 */
	protected float scaleFactor = 1.0f;

	/**
	 * The false Easting of this projection
	 */
	protected float falseEasting = 0;

	/**
	 * The false Northing of this projection
	 */
	protected float falseNorthing = 0;

	/**
	 * The latitude of true scale. Only used by specific projections.
	 */
	protected float trueScaleLatitude = 0.0f;

	/**
	 * The equator radius
	 */
	protected float a = 0;

	/**
	 * The eccentricity
	 */
	protected float e = 0;

	/**
	 * The eccentricity squared
	 */
	protected float es = 0;

	/**
	 * 1-(eccentricity squared)
	 */
	protected float one_es = 0;

	/**
	 * 1/(1-(eccentricity squared))
	 */
	protected float rone_es = 0;

	/**
	 * The ellipsoid used by this projection
	 */
	protected Ellipsoid ellipsoid;

	/**
	 * True if this projection is using a sphere (es == 0)
	 */
	protected boolean spherical;

	/**
	 * True if this projection is geocentric
	 */
	protected boolean geocentric;

	/**
	 * The name of this projection
	 */
	protected String name = null;

	/**
	 * Conversion factor from metres to whatever units the projection uses.
	 */
	protected float fromMetres = 1;

	/**
	 * The total scale factor = Earth radius * units
	 */
	private float totalScale = 0;

	/**
	 * falseEasting, adjusted to the appropriate units using fromMetres
	 */
	private float totalFalseEasting = 0;

	/**
	 * falseNorthing, adjusted to the appropriate units using fromMetres
	 */
	private float totalFalseNorthing = 0;

	// Some useful constants
	protected final static float EPS10 = (float) 1e-10;
	protected final static float RTD = (float) (180.0/Math.PI);
	protected final static float DTR = (float) (Math.PI/180.0);

	protected Projection() {
		setEllipsoid( Ellipsoid.SPHERE );
	}

	public Object clone() {
		try {
			Projection e = (Projection)super.clone();
			return e;
		}
		catch ( CloneNotSupportedException e ) {
			throw new InternalError();
		}
	}

	/**
	 * Project a lat/long point (in degrees), producing a result in metres
	 */
	public PointF transform(PointF src, PointF dst ) {
		float x = src.x*DTR;
		if ( projectionLongitude != 0 )
			x = (float) MapMath.normalizeLongitude( x-projectionLongitude );
		project(x, src.y*DTR, dst);
		dst.x = totalScale * dst.x + totalFalseEasting;
		dst.y = totalScale * dst.y + totalFalseNorthing;
		return dst;
	}

	/**
	 * Project a lat/long point, producing a result in metres
	 */
	public PointF transformRadians( PointF src, PointF dst ) {
		float x = src.x;
		if ( projectionLongitude != 0 )
			x = (float) MapMath.normalizeLongitude( x-projectionLongitude );
		project(x, src.y, dst);
		dst.x = totalScale * dst.x + totalFalseEasting;
		dst.y = totalScale * dst.y + totalFalseNorthing;
		return dst;
	}

	/**
	 * The method which actually does the projection. This should be overridden for all projections.
	 */
	public PointF project(float x, float y, PointF dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	/**
	 * Project a number of lat/long points (in degrees), producing a result in metres
	 */
	public void transform(float[] srcPoints, int srcOffset, float[] dstPoints, int dstOffset, int numPoints) {
		PointF in = new PointF();
		PointF out = new PointF();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			transform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Project a number of lat/long points (in radians), producing a result in metres
	 */
	public void transformRadians(float[] srcPoints, int srcOffset, float[] dstPoints, int dstOffset, int numPoints) {
		PointF in = new PointF();
		PointF out = new PointF();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			transform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Inverse-project a point (in metres), producing a lat/long result in degrees
	 */
	public PointF inverseTransform(PointF src, PointF dst) {
		float x = (src.x - totalFalseEasting) / totalScale;
		float y = (src.y - totalFalseNorthing) / totalScale;
		projectInverse(x, y, dst);
		if (dst.x < (float) -Math.PI)
			dst.x = (float) -Math.PI;
		else if (dst.x > Math.PI)
			dst.x = (float) Math.PI;
		if (projectionLongitude != 0)
			dst.x = (float) MapMath.normalizeLongitude(dst.x+projectionLongitude);
		dst.x *= RTD;
		dst.y *= RTD;
		return dst;
	}

	/**
	 * Inverse-project a point (in metres), producing a lat/long result in radians
	 */
	public PointF inverseTransformRadians(PointF src, PointF dst) {
		float x = (src.x - totalFalseEasting) / totalScale;
		float y = (src.y - totalFalseNorthing) / totalScale;
		projectInverse(x, y, dst);
		if (dst.x < -Math.PI)
			dst.x = (float) -Math.PI;
		else if (dst.x > Math.PI)
			dst.x = (float) Math.PI;
		if (projectionLongitude != 0)
			dst.x = (float) MapMath.normalizeLongitude(dst.x+projectionLongitude);
		return dst;
	}

	/**
	 * The method which actually does the inverse projection. This should be overridden for all projections.
	 */
	public PointF projectInverse(float x, float y, PointF dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	/**
	 * Inverse-project a number of points (in metres), producing a lat/long result in degrees
	 */
	public void inverseTransform(float[] srcPoints, int srcOffset, float[] dstPoints, int dstOffset, int numPoints) {
		PointF in = new PointF();
		PointF out = new PointF();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			inverseTransform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Inverse-project a number of points (in metres), producing a lat/long result in radians
	 */
	public void inverseTransformRadians(float[] srcPoints, int srcOffset, float[] dstPoints, int dstOffset, int numPoints) {
		PointF in = new PointF();
		PointF out = new PointF();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			inverseTransformRadians(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Returns true if this projection is conformal
	 */
	public boolean isConformal() {
		return false;
	}

	/**
	 * Returns true if this projection is equal area
	 */
	public boolean isEqualArea() {
		return false;
	}

	/**
	 * Returns true if this projection has an inverse
	 */
	public boolean hasInverse() {
		return false;
	}

	/**
	 * Returns true if lat/long lines form a rectangular grid for this projection
	 */
	public boolean isRectilinear() {
		return false;
	}

	/**
	 * Returns true if latitude lines are parallel for this projection
	 */
	public boolean parallelsAreParallel() {
		return isRectilinear();
	}

	/**
	 * Returns true if the given lat/long point is visible in this projection
	 */
	public boolean inside(float x, float y) {
		x = normalizeLongitude( (float)(x*DTR-projectionLongitude) );
		return minLongitude <= x && x <= maxLongitude && minLatitude <= y && y <= maxLatitude;
	}

	/**
	 * Set the name of this projection.
	 */
	public void setName( String name ) {
		this.name = name;
	}

	public String getName() {
		if ( name != null )
			return name;
		return toString();
	}

	/**
	 * Get a string which describes this projection in PROJ.4 format.
	 */
	public String getPROJ4Description() {
		AngleFormat format = new AngleFormat( AngleFormat.ddmmssPattern, false );
		StringBuffer sb = new StringBuffer();
		sb.append(
				"+proj="+getName()+
						" +a="+a
		);
		if ( es != 0 )
			sb.append( " +es="+es );
		sb.append( " +lon_0=" );
		format.format( projectionLongitude, sb, null );
		sb.append( " +lat_0=" );
		format.format( projectionLatitude, sb, null );
		if ( falseEasting != 1 )
			sb.append( " +x_0="+falseEasting );
		if ( falseNorthing != 1 )
			sb.append( " +y_0="+falseNorthing );
		if ( scaleFactor != 1 )
			sb.append( " +k="+scaleFactor );
		if ( fromMetres != 1 )
			sb.append( " +fr_meters="+fromMetres );
		return sb.toString();
	}

	public String toString() {
		return "None";
	}

	/**
	 * Set the minimum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMinLatitude( float minLatitude ) {
		this.minLatitude = minLatitude;
	}

	public float getMinLatitude() {
		return minLatitude;
	}

	/**
	 * Set the maximum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMaxLatitude( float maxLatitude ) {
		this.maxLatitude = maxLatitude;
	}

	public float getMaxLatitude() {
		return maxLatitude;
	}

	public float getMaxLatitudeDegrees() {
		return maxLatitude*RTD;
	}

	public float getMinLatitudeDegrees() {
		return minLatitude*RTD;
	}

	public void setMinLongitude( float minLongitude ) {
		this.minLongitude = minLongitude;
	}

	public float getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitudeDegrees( float minLongitude ) {
		this.minLongitude = DTR*minLongitude;
	}

	public float getMinLongitudeDegrees() {
		return minLongitude*RTD;
	}

	public void setMaxLongitude( float maxLongitude ) {
		this.maxLongitude = maxLongitude;
	}

	public float getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitudeDegrees( float maxLongitude ) {
		this.maxLongitude = DTR*maxLongitude;
	}

	public float getMaxLongitudeDegrees() {
		return maxLongitude*RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude( float projectionLatitude ) {
		this.projectionLatitude = projectionLatitude;
	}

	public float getProjectionLatitude() {
		return projectionLatitude;
	}

	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitudeDegrees( float projectionLatitude ) {
		this.projectionLatitude = DTR*projectionLatitude;
	}

	public float getProjectionLatitudeDegrees() {
		return projectionLatitude*RTD;
	}

	/**
	 * Set the projection longitude in radians.
	 */
	public void setProjectionLongitude( float projectionLongitude ) {
		this.projectionLongitude = normalizeLongitudeRadians( projectionLongitude );
	}

	public float getProjectionLongitude() {
		return projectionLongitude;
	}

	/**
	 * Set the projection longitude in degrees.
	 */
	public void setProjectionLongitudeDegrees( float projectionLongitude ) {
		this.projectionLongitude = DTR*projectionLongitude;
	}

	public float getProjectionLongitudeDegrees() {
		return projectionLongitude*RTD;
	}

	/**
	 * Set the latitude of true scale in radians. This is only used by certain projections.
	 */
	public void setTrueScaleLatitude( float trueScaleLatitude ) {
		this.trueScaleLatitude = trueScaleLatitude;
	}

	public float getTrueScaleLatitude() {
		return trueScaleLatitude;
	}

	/**
	 * Set the latitude of true scale in degrees. This is only used by certain projections.
	 */
	public void setTrueScaleLatitudeDegrees( float trueScaleLatitude ) {
		this.trueScaleLatitude = DTR*trueScaleLatitude;
	}

	public float getTrueScaleLatitudeDegrees() {
		return trueScaleLatitude*RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude1( float projectionLatitude1 ) {
		this.projectionLatitude1 = projectionLatitude1;
	}

	public float getProjectionLatitude1() {
		return projectionLatitude1;
	}

	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude1Degrees( float projectionLatitude1 ) {
		this.projectionLatitude1 = DTR*projectionLatitude1;
	}

	public float getProjectionLatitude1Degrees() {
		return projectionLatitude1*RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude2( float projectionLatitude2 ) {
		this.projectionLatitude2 = projectionLatitude2;
	}

	public float getProjectionLatitude2() {
		return projectionLatitude2;
	}

	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude2Degrees( float projectionLatitude2 ) {
		this.projectionLatitude2 = DTR*projectionLatitude2;
	}

	public float getProjectionLatitude2Degrees() {
		return projectionLatitude2*RTD;
	}

	/**
	 * Set the false Northing in projected units.
	 */
	public void setFalseNorthing( float falseNorthing ) {
		this.falseNorthing = falseNorthing;
	}

	public float getFalseNorthing() {
		return falseNorthing;
	}

	/**
	 * Set the false Easting in projected units.
	 */
	public void setFalseEasting( float falseEasting ) {
		this.falseEasting = falseEasting;
	}

	public float getFalseEasting() {
		return falseEasting;
	}

	/**
	 * Set the projection scale factor. This is set to 1 by default.
	 */
	public void setScaleFactor( float scaleFactor ) {
		this.scaleFactor = scaleFactor;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public float getEquatorRadius() {
		return a;
	}

	/**
	 * Set the conversion factor from metres to projected units. This is set to 1 by default.
	 */
	public void setFromMetres( float fromMetres ) {
		this.fromMetres = fromMetres;
	}

	public float getFromMetres() {
		return fromMetres;
	}

	public void setEllipsoid( Ellipsoid ellipsoid ) {
		this.ellipsoid = ellipsoid;
		a = (float) ellipsoid.equatorRadius;
		e = (float) ellipsoid.eccentricity;
		es = (float) ellipsoid.eccentricity2;
	}

	public Ellipsoid getEllipsoid() {
		return ellipsoid;
	}

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	public int getEPSGCode() {
		return 0;
	}

	/**
	 * Initialize the projection. This should be called after setting parameters and before using the projection.
	 * This is for performance reasons as initialization may be expensive.
	 */
	public void initialize() {
		spherical = e == 0.0;
		one_es = 1-es;
		rone_es = (float) 1.0/one_es;
		totalScale = a * fromMetres;
		totalFalseEasting = falseEasting * fromMetres;
		totalFalseNorthing = falseNorthing * fromMetres;
	}

	public static float normalizeLongitude(float angle) {
		if ( Float.isInfinite(angle) || Float.isNaN(angle) )
			throw new IllegalArgumentException("Infinite longitude");
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	public static float normalizeLongitudeRadians( float angle ) {
		if ( Float.isInfinite(angle) || Float.isNaN(angle) )
			throw new IllegalArgumentException("Infinite longitude");
		while (angle > Math.PI)
			angle -= MapMath.TWOPI;
		while (angle < -Math.PI)
			angle += MapMath.TWOPI;
		return angle;
	}

}

