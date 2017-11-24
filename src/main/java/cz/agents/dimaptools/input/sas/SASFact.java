package cz.agents.dimaptools.input.sas;

public class SASFact {

	public final String var;
	public final String val;


	public SASFact(String var, String val) {
		this.var = var;
		this.val = val;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((val == null) ? 0 : val.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SASFact other = (SASFact) obj;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return var + "-" + val;
	}
	
	


}
