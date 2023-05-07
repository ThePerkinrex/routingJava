package org.theperkinrex.applications.dhcp.leaser.contract;

import org.theperkinrex.layers.link.LinkAddr;

public abstract class Contract {
	public final LinkAddr linkAddr;

	public Contract(LinkAddr linkAddr) {
		this.linkAddr = linkAddr;
	}

	@Override
	public String toString() {
		return "Contract{" + "linkAddr=" + linkAddr + '}';
	}
}
