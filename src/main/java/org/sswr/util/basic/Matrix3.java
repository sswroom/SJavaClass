package org.sswr.util.basic;

public class Matrix3
{
	public Vector3 vec[];

	public Matrix3()
	{
		this.vec = new Vector3[3];
		this.vec[0] = new Vector3();
		this.vec[1] = new Vector3();
		this.vec[2] = new Vector3();
	}

	public Matrix3(double val[])
	{
		this();
		this.vec[0].val[0] = val[0];
		this.vec[0].val[1] = val[1];
		this.vec[0].val[2] = val[2];
		this.vec[1].val[0] = val[3];
		this.vec[1].val[1] = val[4];
		this.vec[1].val[2] = val[5];
		this.vec[2].val[0] = val[6];
		this.vec[2].val[1] = val[7];
		this.vec[2].val[2] = val[8];
	}

	public void inverse()
	{
		double outVal[] = new double[9];
		double det;
		outVal[0] =  (vec[1].val[1] * vec[2].val[2] - vec[1].val[2] * vec[2].val[1]);
		outVal[1] = -(vec[0].val[1] * vec[2].val[2] - vec[0].val[2] * vec[2].val[1]);
		outVal[2] =  (vec[0].val[1] * vec[1].val[2] - vec[0].val[2] * vec[1].val[1]);
		outVal[3] = -(vec[1].val[0] * vec[2].val[2] - vec[1].val[2] * vec[2].val[0]);
		outVal[4] =  (vec[0].val[0] * vec[2].val[2] - vec[0].val[2] * vec[2].val[0]);
		outVal[5] = -(vec[0].val[0] * vec[1].val[2] - vec[0].val[2] * vec[1].val[0]);
		outVal[6] =  (vec[1].val[0] * vec[2].val[1] - vec[1].val[1] * vec[2].val[0]);
		outVal[7] = -(vec[0].val[0] * vec[2].val[1] - vec[0].val[1] * vec[2].val[0]);
		outVal[8] =  (vec[0].val[0] * vec[1].val[1] - vec[0].val[1] * vec[1].val[0]);
		det = 1 / (vec[0].val[0] * outVal[0] + vec[0].val[1] * outVal[3] + vec[0].val[2] * outVal[6]);
		this.vec[0].val[0] = outVal[0] * det;
		this.vec[0].val[1] = outVal[1] * det;
		this.vec[0].val[2] = outVal[2] * det;
		this.vec[1].val[0] = outVal[3] * det;
		this.vec[1].val[1] = outVal[4] * det;
		this.vec[1].val[2] = outVal[5] * det;
		this.vec[2].val[0] = outVal[6] * det;
		this.vec[2].val[1] = outVal[7] * det;
		this.vec[2].val[2] = outVal[8] * det;
	}

	public void transposition()
	{
		double t;
		t = this.vec[0].val[1];
		this.vec[0].val[1] = this.vec[1].val[0];
		this.vec[1].val[0] = t;
		t = this.vec[0].val[2];
		this.vec[0].val[2] = this.vec[2].val[0];
		this.vec[2].val[0] = t;
		t = this.vec[1].val[2];
		this.vec[1].val[2] = this.vec[2].val[1];
		this.vec[2].val[1] = t;
	}

	public void set(Matrix3 matrix)
	{
		this.vec[0].set(matrix.vec[0]);
		this.vec[1].set(matrix.vec[1]);
		this.vec[2].set(matrix.vec[2]);
	}

	public void multiply(Matrix3 matrix)
	{
		Vector3 tmpVec[] = new Vector3[3];
		tmpVec[0] = this.vec[0];
		tmpVec[1] = this.vec[1];
		tmpVec[2] = this.vec[2];
		this.vec[0].val[0] = tmpVec[0].val[0] * matrix.vec[0].val[0] + tmpVec[0].val[1] * matrix.vec[1].val[0] + tmpVec[0].val[2] * matrix.vec[2].val[0];
		this.vec[0].val[1] = tmpVec[0].val[0] * matrix.vec[0].val[1] + tmpVec[0].val[1] * matrix.vec[1].val[1] + tmpVec[0].val[2] * matrix.vec[2].val[1];
		this.vec[0].val[2] = tmpVec[0].val[0] * matrix.vec[0].val[2] + tmpVec[0].val[1] * matrix.vec[1].val[2] + tmpVec[0].val[2] * matrix.vec[2].val[2];
		this.vec[1].val[0] = tmpVec[1].val[0] * matrix.vec[0].val[0] + tmpVec[1].val[1] * matrix.vec[1].val[0] + tmpVec[1].val[2] * matrix.vec[2].val[0];
		this.vec[1].val[1] = tmpVec[1].val[0] * matrix.vec[0].val[1] + tmpVec[1].val[1] * matrix.vec[1].val[1] + tmpVec[1].val[2] * matrix.vec[2].val[1];
		this.vec[1].val[2] = tmpVec[1].val[0] * matrix.vec[0].val[2] + tmpVec[1].val[1] * matrix.vec[1].val[2] + tmpVec[1].val[2] * matrix.vec[2].val[2];
		this.vec[2].val[0] = tmpVec[2].val[0] * matrix.vec[0].val[0] + tmpVec[2].val[1] * matrix.vec[1].val[0] + tmpVec[2].val[2] * matrix.vec[2].val[0];
		this.vec[2].val[1] = tmpVec[2].val[0] * matrix.vec[0].val[1] + tmpVec[2].val[1] * matrix.vec[1].val[1] + tmpVec[2].val[2] * matrix.vec[2].val[1];
		this.vec[2].val[2] = tmpVec[2].val[0] * matrix.vec[0].val[2] + tmpVec[2].val[1] * matrix.vec[1].val[2] + tmpVec[2].val[2] * matrix.vec[2].val[2];
	}

	public void myMultiply(Matrix3 matrix)
	{
		Vector3 tmpVec[] = new Vector3[3];;
		tmpVec[0] = this.vec[0];
		tmpVec[1] = this.vec[1];
		tmpVec[2] = this.vec[2];
		this.vec[0].val[0] = tmpVec[0].val[0] * matrix.vec[0].val[0] + tmpVec[1].val[0] * matrix.vec[0].val[1] + tmpVec[2].val[0] * matrix.vec[0].val[2];
		this.vec[0].val[1] = tmpVec[0].val[1] * matrix.vec[0].val[0] + tmpVec[1].val[1] * matrix.vec[0].val[1] + tmpVec[2].val[1] * matrix.vec[0].val[2];
		this.vec[0].val[2] = tmpVec[0].val[2] * matrix.vec[0].val[0] + tmpVec[1].val[2] * matrix.vec[0].val[1] + tmpVec[2].val[2] * matrix.vec[0].val[2];
		this.vec[1].val[0] = tmpVec[0].val[0] * matrix.vec[1].val[0] + tmpVec[1].val[0] * matrix.vec[1].val[1] + tmpVec[2].val[0] * matrix.vec[1].val[2];
		this.vec[1].val[1] = tmpVec[0].val[1] * matrix.vec[1].val[0] + tmpVec[1].val[1] * matrix.vec[1].val[1] + tmpVec[2].val[1] * matrix.vec[1].val[2];
		this.vec[1].val[2] = tmpVec[0].val[2] * matrix.vec[1].val[0] + tmpVec[1].val[2] * matrix.vec[1].val[1] + tmpVec[2].val[2] * matrix.vec[1].val[2];
		this.vec[2].val[0] = tmpVec[0].val[0] * matrix.vec[2].val[0] + tmpVec[1].val[0] * matrix.vec[2].val[1] + tmpVec[2].val[0] * matrix.vec[2].val[2];
		this.vec[2].val[1] = tmpVec[0].val[1] * matrix.vec[2].val[0] + tmpVec[1].val[1] * matrix.vec[2].val[1] + tmpVec[2].val[1] * matrix.vec[2].val[2];
		this.vec[2].val[2] = tmpVec[0].val[2] * matrix.vec[2].val[0] + tmpVec[1].val[2] * matrix.vec[2].val[1] + tmpVec[2].val[2] * matrix.vec[2].val[2];
	}

	public Vector3 multiply(Double x, Double y, Double z)
	{
		Vector3 ret = new Vector3();
		ret.set(x * this.vec[0].val[0] + y * this.vec[0].val[1] + z * this.vec[0].val[2],
				x * this.vec[1].val[0] + y * this.vec[1].val[1] + z * this.vec[1].val[2],
				x * this.vec[2].val[0] + y * this.vec[2].val[1] + z * this.vec[2].val[2]);
		return ret;
	}

	public Vector3 multiply(Vector3 vec)
	{
		Vector3 ret = new Vector3();
		ret.set(this.vec[0].multiply(vec),
				this.vec[1].multiply(vec),
				this.vec[2].multiply(vec));
		return ret;
	}
	
	public void setIdentity()
	{
		this.vec[0].set(1.0, 0.0, 0.0);
		this.vec[1].set(0.0, 1.0, 0.0);
		this.vec[2].set(0.0, 0.0, 1.0);
	}
}
