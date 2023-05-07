package org.theperkinrex.applications.dhcp.leaser.contract.offer;

import org.theperkinrex.applications.dhcp.leaser.contract.Contract;
import org.theperkinrex.layers.link.LinkAddr;

public class OfferContract extends Contract {
	public OfferContract(LinkAddr linkAddr) {
		super(linkAddr);
	}

	@Override
	public String toString() {
		return "OfferContract{" + "linkAddr=" + linkAddr + '}';
	}
}
