package org.theperkinrex.applications.dhcp.leaser.simple.ipv4;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.theperkinrex.applications.dhcp.leaser.Lease;
import org.theperkinrex.applications.dhcp.leaser.SimpleOffer;
import org.theperkinrex.applications.dhcp.leaser.contract.Contract;
import org.theperkinrex.applications.dhcp.leaser.contract.lease.DynamicLeaseContract;
import org.theperkinrex.applications.dhcp.leaser.contract.lease.LeaseContract;
import org.theperkinrex.applications.dhcp.leaser.contract.offer.OfferContract;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasableAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasedAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoOfferedAddress;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.applications.dhcp.leaser.Leaser;
import org.theperkinrex.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleIPv4Leaser implements Leaser<IPv4Addr>  {
	private final ConcurrentMap<IPv4Addr, Contract> leases;
	private final ConcurrentMap<LinkAddr, IPv4Addr> leaseholders;
	private final Duration leaseTime;

	private final IPv4Addr rangeStart;

	private final IPv4Addr rangeEnd;

	private final Lock mutex;

	public SimpleIPv4Leaser(Duration leaseTime, IPv4Addr rangeStart, IPv4Addr rangeEnd) {
		this.leaseTime = leaseTime;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.leases = new ConcurrentHashMap<>();
		this.leaseholders = new ConcurrentHashMap<>();
		this.mutex = new ReentrantLock();
	}

	@Override
	public @NotNull SimpleOffer<IPv4Addr> offer(@NotNull LinkAddr addr) throws NoLeasableAddress, InterruptedException {
		IPv4Addr found = null;
		mutex.lockInterruptibly();
		// TODO Renewing
		for (int i = rangeStart.addr; i <= rangeEnd.addr && found == null; i++) {
			IPv4Addr a = new IPv4Addr(i);
			var c = leases.compute(a, (k, v) -> (v == null || (v instanceof DynamicLeaseContract dlc && dlc.end.isBefore(
					Instant.now()))) ? null : v);
			if (!leases.containsKey(a) && c == null) {
				leases.put(a, new OfferContract(addr));
				leaseholders.put(addr, a);
				found = a;
			}
		}
		mutex.unlock();
		if(found == null) throw new NoLeasableAddress();
		return new SimpleOffer<>(found, leaseTime);
	}

	@Override
	public void decline(@NotNull LinkAddr addr) throws NoOfferedAddress, InterruptedException {
		try {
			mutex.lockInterruptibly();
			var ip = leaseholders.get(addr);
			if (ip == null) throw new NoOfferedAddress();
			var contract = leases.get(ip);
			if(!(contract instanceof OfferContract)) throw new NoOfferedAddress();
			leaseholders.remove(addr);
			leases.remove(ip);
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public @NotNull Lease<IPv4Addr> lease(@NotNull LinkAddr addr) throws NoOfferedAddress, InterruptedException {
		try {
			mutex.lockInterruptibly();
			var ip = leaseholders.get(addr);
			if (ip == null) throw new NoOfferedAddress();
			var contract = leases.get(ip);
			if(!(contract instanceof OfferContract)) throw new NoOfferedAddress();
			Instant end = Instant.now().plus(leaseTime);
			leases.replace(ip, new DynamicLeaseContract(addr, end));
			return new Lease<>() {
				@Override
				public @NotNull IPv4Addr addr() {
					return ip;
				}

				@Override
				public @Nullable Instant end() {
					return end;
				}
			};
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public void release(@NotNull LinkAddr addr) throws InterruptedException, NoLeasedAddress {
		try {
			mutex.lockInterruptibly();
			var ip = leaseholders.get(addr);
			if (ip == null) throw new NoLeasedAddress();
			var contract = leases.get(ip);
			if(!(contract instanceof LeaseContract)) throw new NoLeasedAddress();
			leaseholders.remove(addr);
			leases.remove(ip);
		} finally {
			mutex.unlock();
		}
	}
}
