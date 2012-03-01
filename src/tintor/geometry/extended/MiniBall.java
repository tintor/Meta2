// Port of:
//Copright (C) 1999-2006, Bernd Gaertner
//$Revision: 1.3 $
//$Date: 2006/11/16 08:01:52 $
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA,
//or download the License terms from prep.ai.mit.edu/pub/gnu/COPYING-2.0.
//
//Contact:
//--------
//Bernd Gaertner
//Institute of Theoretical Computer Science 
//ETH Zuerich
//CAB G32.2
//CH-8092 Zuerich, Switzerland
//http://www.inf.ethz.ch/personal/gaertner

package tintor.geometry.extended;

import java.util.ArrayList;
import java.util.List;

public class MiniBall {
	public static final int d = 3;

	private final List<double[]> L = new ArrayList<double[]>(); // internal point set
	private final MiniBall_b B = new MiniBall_b(); // the current ball
	private int support_end; // past-the-end iterator of support set

	// private methods
	private void mtf(final int i) {
		support_end = 0;
		if (B.size == d + 1)
			return;
		for (int k = 0; k < i; k++) {
			if (B.excess(L.get(k)) > 0) {
				if (B.push(L.get(k))) {
					mtf(k);
					B.pop();
					move_to_front(k);
				}
			}
		}
	}

	private void pivot(final int i) {
		int t = 1;
		mtf(t);
		double max_e, old_sqr_r = -1;
		final int[] pivot = new int[1];
		do {
			max_e = max_excess(t, i, pivot);
			if (max_e > 0) {
				t = support_end;
				if (t == pivot[0]) {
					++t;
				}
				old_sqr_r = B.squared_radius;
				B.push(L.get(pivot[0]));
				mtf(support_end);
				B.pop();
				move_to_front(pivot[0]);
			}
		} while (max_e > 0 && B.squared_radius > old_sqr_r);
	}

	private void move_to_front(final int j) {
		if (support_end == j) {
			support_end++;
		}
		// move from L[j] to L[0]
		final double[] p = L.get(j);
		for (int i = j; i > 0; i--) {
			L.set(i, L.get(i - 1));
		}
		L.set(0, p);
	}

	// pivot is out parameter
	private double max_excess(final int t, final int i, final int[] pivot) {
		final double[] c = B.center;
		final double sqr_r = B.squared_radius;
		double e, max_e = 0;
		for (int k = t; k < i; ++k) {
			final double[] p = L.get(k);
			e = -sqr_r;
			for (int j = 0; j < d; ++j) {
				e += (p[j] - c[j]) * (p[j] - c[j]);
			}
			if (e > max_e) {
				max_e = e;
				pivot[0] = k;
			}
		}
		return max_e;
	}

	public static String pointToString(final double[] p) {
		final StringBuilder b = new StringBuilder("(");
		for (int i = 0; i < d - 1; ++i) {
			b.append(p[i]).append(", ");
		}
		b.append(p[d - 1]).append(")");
		return b.toString();
	}

	public void add(final double[] p) {
		L.add(p);
	}

	// builds the smallest enclosing ball of the internal point set
	public void build() {
		B.reset();
		support_end = 0;
		pivot(L.size());
	}

	// returns center of the ball (undefined if ball is empty)
	public double[] center() {
		return B.center;
	}

	// returns squared_radius of the ball (-1 if ball is empty)
	public double squared_radius() {
		return B.squared_radius;
	}

	// assesses the quality of the computed ball. The return value is the
	// maximum squared distance of any support point or point outside the 
	// ball to the boundary of the ball, divided by the squared radius of
	// the ball. If everything went fine, this will be less than e-15 and
	// says that the computed ball approximately contains all the internal
	// points and has all the support points on the boundary.
	// 
	public double accuracy() {
		// you've found a non-numerical problem if the following ever fails
		assert support_end == B.support_size;

		double max_e = 0;
		for (int i = 0; i < support_end; ++i) {
			max_e = Math.max(max_e, Math.abs(B.excess(L.get(i))));
		}
		for (int i = support_end; i < L.size(); ++i) {
			max_e = Math.max(max_e, B.excess(L.get(i)));
		}

		return max_e / B.squared_radius;
	}

	// The slack parameter that is set by the method says something about
	// whether the computed ball is really the *smallest* enclosing ball 
	// of the support points; if everything went fine, this value will be 0; 
	// a positive value may indicate that the ball is not smallest possible,
	// with the deviation from optimality growing with the slack
	//
	public double slack() {
		return B.slack();
	}

	// returns true if the accuracy is below the given tolerance and the
	// slack is 0
	public boolean is_valid() {
		return is_valid(1e-15);
	}

	public boolean is_valid(final double tolerance) {
		return accuracy() < tolerance && B.slack() == 0;
	}
}

class MiniBall_b {
	public static final int d = MiniBall.d;

	// data members
	int size, support_size; // size and number of support points
	private final double[] q0 = new double[d];

	private final double[] z = new double[d + 1];
	private final double[] f = new double[d + 1];
	private final double[][] v = new double[d + 1][d];
	private final double[][] a = new double[d + 1][d];

	private final double[][] c = new double[d + 1][d];
	private final double[] sqr_r = new double[d + 1];

	double[] center = c[0];
	double squared_radius = -1;

	public double excess(final double[] p) {
		double e = -squared_radius;
		for (int k = 0; k < d; ++k) {
			final double r = p[k] - center[k];
			e += r * r;
		}
		return e;
	}

	// generates empty sphere with m=s=0
	public void reset() {
		size = support_size = 0;
		// we misuse c[0] for the center of the empty sphere
		for (int j = 0; j < d; ++j) {
			c[0][j] = 0;
		}
		center = c[0];
		squared_radius = -1;
	}

	public boolean push(final double[] p) {
		int i, j;
		final double eps = 1e-32;
		if (size == 0) {
			for (i = 0; i < d; ++i) {
				c[0][i] = q0[i] = p[i];
			}
			sqr_r[0] = 0;
		} else {
			// set v_m to Q_m
			for (i = 0; i < d; ++i) {
				v[size][i] = p[i] - q0[i];
			}

			// compute the a_{m,i}, i< m
			for (i = 1; i < size; ++i) {
				a[size][i] = 0;
				for (j = 0; j < d; ++j) {
					a[size][i] += v[i][j] * v[size][j];
				}
				a[size][i] *= 2 / z[i];
			}

			// update v_m to Q_m-\bar{Q}_m
			for (i = 1; i < size; ++i) {
				for (j = 0; j < d; ++j) {
					v[size][j] -= a[size][i] * v[i][j];
				}
			}

			// compute z_m
			z[size] = 0;
			for (j = 0; j < d; ++j) {
				final double r = v[size][j];
				z[size] += r * r;
			}
			z[size] *= 2;

			// reject push if z_m too small
			if (z[size] < eps * squared_radius)
				return false;

			// update c, sqr_r
			double e = -sqr_r[size - 1];
			for (i = 0; i < d; ++i) {
				final double r = p[i] - c[size - 1][i];
				e += r * r;
			}
			f[size] = e / z[size];

			for (i = 0; i < d; ++i) {
				c[size][i] = c[size - 1][i] + f[size] * v[size][i];
			}
			sqr_r[size] = sqr_r[size - 1] + e * f[size] / 2;
		}
		center = c[size];
		squared_radius = sqr_r[size];
		support_size = ++size;
		return true;
	}

	public void pop() {
		--size;
	}

	// checking
	public double slack() {
		final double[] L = new double[d + 1];
		double minL = 0;
		L[0] = 1;
		for (int i = support_size - 1; i > 0; --i) {
			L[i] = f[i];
			for (int k = support_size - 1; k > i; --k) {
				L[i] -= a[k][i] * L[k];
			}
			minL = Math.min(minL, L[i]);
			L[0] -= L[i];
		}
		minL = Math.min(minL, L[0]);
		return minL < 0 ? -minL : 0;
	}
}