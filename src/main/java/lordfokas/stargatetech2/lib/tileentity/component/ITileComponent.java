package lordfokas.stargatetech2.lib.tileentity.component;

import net.minecraft.nbt.NBTTagCompound;

public interface ITileComponent {
	public void readFromNBT(NBTTagCompound nbt);
	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
