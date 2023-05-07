package org.theperkinrex.applications.dhcp.leaser.contract.lease;

import org.theperkinrex.applications.dhcp.leaser.contract.Contract;
import org.theperkinrex.layers.link.LinkAddr;

public abstract class LeaseContract extends Contract {
	public LeaseContract(LinkAddr linkAddr) {
		super(linkAddr);
	}
}
