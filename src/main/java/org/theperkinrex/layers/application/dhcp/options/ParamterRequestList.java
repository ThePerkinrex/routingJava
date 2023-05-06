package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class ParamterRequestList implements DhcpOption {


	public final int[] list;

	public ParamterRequestList(int[] list) {
		this.list = list;
	}

	@Override
	public int code() {
		return PARAMETER_REQUEST_LIST;
	}
}
