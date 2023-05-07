package org.theperkinrex.applications.dhcp.leaser.contract.lease;

import org.theperkinrex.layers.link.LinkAddr;

public class StaticLeaseContract extends LeaseContract {
	public StaticLeaseContract(LinkAddr linkAddr) {
		super(linkAddr);
	}

	@Override
	public String toString() {
		return "StaticLeaseContract{" + "linkAddr=" + linkAddr + '}';
	}
}
