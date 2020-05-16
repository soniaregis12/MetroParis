package it.polito.tdp.metroparis.model;

public class CoppiaFermata {
	
	private Fermata fp;
	private Fermata fa;
	public CoppiaFermata(Fermata fp, Fermata fa) {
		this.fp = fp;
		this.fa = fa;
	}
	public Fermata getFp() {
		return fp;
	}
	public void setFp(Fermata fp) {
		this.fp = fp;
	}
	public Fermata getFa() {
		return fa;
	}
	public void setFa(Fermata fa) {
		this.fa = fa;
	}
	

}
