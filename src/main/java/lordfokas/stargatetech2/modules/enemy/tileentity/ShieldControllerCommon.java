package lordfokas.stargatetech2.modules.enemy.tileentity;

import lordfokas.stargatetech2.api.bus.IBusInterface;
import lordfokas.stargatetech2.api.shields.ShieldPermissions;
import lordfokas.stargatetech2.lib.tileentity.ISyncedGUI;
import lordfokas.stargatetech2.lib.tileentity.ITile;
import lordfokas.stargatetech2.lib.tileentity.ITileContext;
import lordfokas.stargatetech2.lib.tileentity.component.IComponentProvider;
import lordfokas.stargatetech2.lib.tileentity.component.IComponentRegistrar;
import lordfokas.stargatetech2.lib.tileentity.component.base.BasicFluidFilter;
import lordfokas.stargatetech2.lib.tileentity.component.base.TankComponentFiltered;
import lordfokas.stargatetech2.lib.tileentity.faces.FaceColor;
import lordfokas.stargatetech2.lib.tileentity.faces.FaceColorFilter;
import lordfokas.stargatetech2.modules.automation.ISyncBusDevice;
import lordfokas.stargatetech2.modules.enemy.IonizedParticles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ShieldControllerCommon implements ITileContext, ISyncedGUI.Flow, IComponentProvider{
	protected FluidTank tank = new FluidTank(16000);
	protected ShieldPermissions permissions = ShieldPermissions.getDefault();
	protected boolean active;
	protected boolean enabled;
	protected boolean busEnabled;
	protected short busAddress;
	private ITile tile;
	
	@Override
	public void registerComponents(IComponentRegistrar registrar) {
		TankComponentFiltered component = new TankComponentFiltered(tank, false, new BasicFluidFilter(IonizedParticles.fluid));
		registrar.registerComponent(component.setInputFilter(new FaceColorFilter.MatchColors(FaceColor.BLUE)));
	}
	
	public void readNBTData(NBTTagCompound nbt) {
		permissions = ShieldPermissions.readFromNBT(nbt.getCompoundTag("permissions"));
		active = nbt.getBoolean("active");
		enabled = nbt.getBoolean("enabled");
	}
	
	public void writeNBTData(NBTTagCompound nbt) {
		nbt.setTag("permissions", permissions.writeToNBT());
		nbt.setBoolean("active", active);
		nbt.setBoolean("enabled", enabled);
	}
	
	@Override
	public int getValueCount() {
		return 5;
	}
	@Override
	public int getValue(int key) {
		switch(key){
			case 0: return tank.getFluidAmount();
			case 1: return active ? 1 : 0;
			case 2: return enabled ? 1 : 0;
			case 3: return busEnabled ? 1: 0;
			case 4: return busAddress;
		}
		return -1;
	}
	@Override
	public void setValue(int key, int val) {
		switch(key){
			case 0:
				tank.setFluid(new FluidStack(IonizedParticles.fluid, val));
				break;
			case 1:
				active = (val == 1);
				break;
			case 2:
				enabled = (val == 1);
				break;
			case 3:
				busEnabled = (val == 1);
				break;
			case 4:
				busAddress = (short)(val & 0xFFFF);
				break;
		}
	}

	@Override public void tick(){}
	@Override public boolean canTick() { return false; }
	
	@Override
	public void setTile(ITile tile){
		this.tile = tile;
	}
	
	public boolean isShieldOn(){
		return active;
	}
	
	public ShieldPermissions getPermissions(){
		return permissions;
	}
}