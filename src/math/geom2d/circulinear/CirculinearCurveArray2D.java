/**
 * File: 	CirculinearCurveArray2D.java
 * Project: javaGeom
 * 
 * Distributed under the LGPL License.
 *
 * Created: 11 mai 09
 */
package math.geom2d.circulinear;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.Box2D;
import math.geom2d.circulinear.buffer.BufferCalculator;
import math.geom2d.curve.*;
import math.geom2d.transform.CircleInversion2D;


/**
 * A specialization of CurveArray2D that accepts only instances of
 * CirculinearCurve2D.
 * @author dlegland
 *
 */
public class CirculinearCurveArray2D<T extends CirculinearCurve2D>
extends CurveArray2D<T> implements CirculinearCurveSet2D<T> {
	
    // ===================================================================
    // static constructors

    /**
     * Static factory for creating a new CirculinearCurveArray2D from a collection of
     * curves.
     * @since 0.8.1
     */
    public static <T extends CirculinearCurve2D> CirculinearCurveArray2D<T> create(
    		Collection<T> curves) {
    	return new CirculinearCurveArray2D<T>(curves);
    }
    
    /**
     * Static factory for creating a new CirculinearCurveArray2D from an array of
     * curves.
     * @since 0.8.1
     */
    public static <T extends CirculinearCurve2D> CirculinearCurveArray2D<T> create(
    		T[] curves) {
    	return new CirculinearCurveArray2D<T>(curves);
    }

    
    // ===================================================================
    // constructors

	/**
     * Empty constructor. Initializes an empty array of curves.
     */
    public CirculinearCurveArray2D() {
    	this.curves = new ArrayList<T>();
    }

    /**
     * Empty constructor. Initializes an empty array of curves, 
     * with a given size for allocating memory.
     */
    public CirculinearCurveArray2D(int n) {
    	this.curves = new ArrayList<T>(n);
    }

    /**
     * Constructor from an array of curves.
     * 
     * @param curves the array of curves in the set
     */
    public CirculinearCurveArray2D(T[] curves) {
    	this.curves = new ArrayList<T>(curves.length);
        for (T element : curves)
            this.addCurve(element);
    }

    /**
     * Constructor from a collection of curves. The curves are added to the
     * inner collection of curves.
     * 
     * @param curves the collection of curves to add to the set
     */
    public CirculinearCurveArray2D(Collection<? extends T> curves) {
    	this.curves = new ArrayList<T>(curves.size());
        this.curves.addAll(curves);
    }

    
    // ===================================================================
    // methods implementing the CirculinearCurve2D interface

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getLength()
	 */
	public double getLength() {
		double sum = 0;
		for(CirculinearCurve2D curve : this.getCurves())
			sum += curve.getLength();
		return sum;
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getLength(double)
	 */
	public double getLength(double pos) {
		return CirculinearCurve2DUtils.getLength(this, pos);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getPosition(double)
	 */
	public double getPosition(double length) {
		return CirculinearCurve2DUtils.getPosition(this, length);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearShape2D#getBuffer(double)
	 */
	public CirculinearDomain2D getBuffer(double dist) {
		BufferCalculator bc = BufferCalculator.getDefaultInstance();
		return bc.computeBuffer(this, dist);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearContinuousCurve2D#getParallel(double)
	 */
	public CirculinearCurve2D getParallel(double d) {
		BufferCalculator bc = BufferCalculator.getDefaultInstance();
		return bc.createParallel(this, d);
	}
	
	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#transform(math.geom2d.transform.CircleInversion2D)
	 */
	public CirculinearCurveArray2D<CirculinearCurve2D> transform(CircleInversion2D inv) {
    	// Allocate array for result
		CirculinearCurveArray2D<CirculinearCurve2D> result = 
			new CirculinearCurveArray2D<CirculinearCurve2D>(curves.size());
        
        // add each transformed curve
        for (CirculinearCurve2D curve : curves)
            result.addCurve(curve.transform(inv));
        return result;
	}
	
    // ===================================================================
    // methods implementing the Curve2D interface

    @Override
    public Collection<? extends CirculinearContinuousCurve2D>
    getContinuousCurves() {
    	// create array for storing result
        ArrayList<CirculinearContinuousCurve2D> result = 
        	new ArrayList<CirculinearContinuousCurve2D>();
        
        // iterate on curves, and extract each set of continuous curves
        for(CirculinearCurve2D curve : curves)
        	result.addAll(curve.getContinuousCurves());
        
        // return the set of curves
        return result;
    }

	@Override
	public CirculinearCurveArray2D<? extends CirculinearCurve2D> clip(Box2D box) {
        // Clip the curve
        CurveSet2D<? extends Curve2D> set = Curve2DUtils.clipCurve(this, box);

        // Stores the result in appropriate structure
        int n = set.getCurveNumber();
        CirculinearCurveArray2D<CirculinearCurve2D> result = 
        	new CirculinearCurveArray2D<CirculinearCurve2D>(n);

        // convert the result, class cast each curve
        for (Curve2D curve : set.getCurves()) {
            if (curve instanceof CirculinearCurve2D)
                result.addCurve((CirculinearCurve2D) curve);
        }
        
        // return the new set of curves
        return result;
	}
    
	@Override
	public CirculinearCurveArray2D<? extends CirculinearCurve2D> 
	getSubCurve(double t0, double t1) {
		// Call the superclass method
		CurveSet2D<? extends Curve2D> subcurve = super.getSubCurve(t0, t1);
		
		// prepare result
		CirculinearCurveArray2D<CirculinearCurve2D> result = new 
		CirculinearCurveArray2D<CirculinearCurve2D>(subcurve.getCurveNumber());
		
		// add each curve after class,cast
		for(Curve2D curve : subcurve) {
			if(curve instanceof CirculinearCurve2D)
				result.addCurve((CirculinearCurve2D) curve);
			else
				System.err.println("CirculinearCurveArray2D.getSubCurve: error in class cast");
		}
		
		// return the result
		return result;
	}
	
	@Override
	public CirculinearCurveArray2D<? extends CirculinearCurve2D> 
	getReverseCurve(){
    	int n = curves.size();
        // create array of reversed curves
    	CirculinearCurve2D[] curves2 = new CirculinearCurve2D[n];
        
        // reverse each curve
        for (int i = 0; i<n; i++)
            curves2[i] = curves.get(n-1-i).getReverseCurve();
        
        // create the reversed final curve
        return new CirculinearCurveArray2D<CirculinearCurve2D>(curves2);
	}
}