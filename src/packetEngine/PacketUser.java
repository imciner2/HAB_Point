package packetEngine;



/**
 * this interface defines the methods that a user of the AGWPacketSocket must define
 */
public interface PacketUser
{
/**
 * a packet is available to the user or the state has changed, or both
 * @param pkt a data packet
 */
	public void postPacket(Packet pkt);	// put the packet on a queue of Runnables
	public void runPacket(Packet pkt);	// remove the packet from a queue
}
