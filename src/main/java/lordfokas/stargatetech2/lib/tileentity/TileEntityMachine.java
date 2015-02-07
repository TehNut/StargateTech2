package lordfokas.stargatetech2.lib.tileentity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;

import lordfokas.stargatetech2.lib.tileentity.ITileContext.Client;
import lordfokas.stargatetech2.lib.tileentity.ITileContext.Server;
import lordfokas.stargatetech2.lib.tileentity.component.IAccessibleTileComponent;
import lordfokas.stargatetech2.lib.tileentity.component.IComponentProvider;
import lordfokas.stargatetech2.lib.tileentity.component.IComponentRegistrar;
import lordfokas.stargatetech2.lib.tileentity.component.ITileComponent;
import lordfokas.stargatetech2.lib.tileentity.component.access.ICapacitorComponent;
import lordfokas.stargatetech2.lib.tileentity.component.access.IInventoryComponent;
import lordfokas.stargatetech2.lib.tileentity.component.access.ITankComponent;
import lordfokas.stargatetech2.lib.tileentity.faces.Face;
import lordfokas.stargatetech2.lib.tileentity.faces.FaceColor;
import lordfokas.stargatetech2.lib.tileentity.faces.IFacingAware;
import lordfokas.stargatetech2.lib.tileentity.faces.IFacingProvider;
import lordfokas.stargatetech2.reference.TextureReference;
import lordfokas.stargatetech2.util.IconRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cofh.api.tileentity.IReconfigurableFacing;
import cofh.api.tileentity.IReconfigurableSides;
import cofh.api.tileentity.ISidedTexture;

/**
 * An advanced TileEntity. Supplies services like automatic rotation handling,
 * side colors, components, among others.
 * 
 * If a context implements {@link IFacingAware} it will be provided with access
 * to this TileEntity's facing data.
 * 
 * If a Context implements {@link IComponentProvider} it will be allowed to register
 * components to this machine. Components should implement {@link ITileComponent}.
 * Components that implement {@link IAccessibleTileComponent} will be exposed to
 * the outside of the TileEntity.
 * Components that implement {@link IFacingAware} will be allowed to read this
 * TileEntity's facing data.
 *
 * @param <C> The type {@link BaseTileEntity#getClientContext()} should return.
 * @param <S> The type {@link BaseTileEntity#getServerContext()} should return.
 * 
 * @author LordFokas
 */
public class TileEntityMachine<C extends Client, S extends Server> extends BaseTileEntity<C, S>
implements IReconfigurableSides, IReconfigurableFacing, ISidedTexture, IFacingProvider, IComponentRegistrar,
IFluidHandler{
	private static final Class[] INTERFACES = new Class[]{
		ICapacitorComponent.class, IInventoryComponent.class, ITankComponent.class
	};
	private EnumMap<Face, FaceWrapper> faces = new EnumMap(Face.class);
	private Face[] faceMap = new Face[6];
	private ForgeDirection facing;
	
	private ArrayList<ITileComponent> allComponents = new ArrayList();
	private HashMap<Class, ArrayList<? extends IAccessibleTileComponent>> sidedComponents = new HashMap();
	
	public TileEntityMachine(Class<? extends C> client, Class<? extends S> server, FaceColor ... colors) {
		super(client, server);
		facing = ForgeDirection.SOUTH;
		setMap(ForgeDirection.UP, Face.TOP);
		setMap(ForgeDirection.DOWN, Face.BOTTOM);
		remapSides(false); // don't update the clients; we may even be on the client side!
		for(Face face : faceMap){
			faces.put(face, new FaceWrapper(colors));
		}
		if(context instanceof IFacingAware){
			((IFacingAware)context).setProvider(this);
		}
		for(Class iface : INTERFACES){
			sidedComponents.put(iface, new ArrayList());
		}
		if(context instanceof IComponentProvider){
			((IComponentProvider)context).registerComponents(this);
		}
	}
	
	@Override
	public void registerComponent(ITileComponent component) {
		allComponents.add(component);
		if(component instanceof IFacingAware){
			((IFacingAware)component).setProvider(this);
		}
		if(component instanceof IAccessibleTileComponent){
			Class cls = component.getClass();
			for(Class iface : INTERFACES){
				if(iface.isAssignableFrom(cls)){
					((ArrayList<ITileComponent>) sidedComponents.get(iface)).add(component);
					return;
				}
			}
		}
	}
	
	// ##########################################################
	// Reconfigurable Facing
	
	@Override
	public int getFacing() {
		return facing.ordinal();
	}

	@Override
	public boolean allowYAxisFacing() {
		return false;
	}

	@Override
	public boolean rotateBlock() {
		int side = facing.ordinal() + 1;
		if(side == 6) side = 2;
		setFacing(side);
		return true;
	}

	@Override
	public boolean setFacing(int side) {
		facing = ForgeDirection.getOrientation(side);
		remapSides(true);
		return true;
	}
	
	private void remapSides(boolean update){
		setMap(facing, Face.FRONT);
		setMap(facing.getOpposite(), Face.BACK);
		
		ForgeDirection left = facing.getRotation(ForgeDirection.UP); // rotate on Y axis
		setMap(left, Face.LEFT);
		setMap(left.getOpposite(), Face.RIGHT);
		
		if(update) super.updateClients();
	}
	
	private void setMap(ForgeDirection dir, Face face){
		faceMap[dir.ordinal()] = face;
	}
	
	// ##########################################################
	// Reconfigurable Sides
	
	@Override
	public boolean decrSide(int side) {
		FaceWrapper face = getFaceForSide(side);
		if(face.count() < 2) return false;
		face.decrease();
		return true;
	}

	@Override
	public boolean incrSide(int side) {
		FaceWrapper face = getFaceForSide(side);
		if(face.count() < 2) return false;
		face.increase();
		return true;
	}

	@Override
	public boolean setSide(int side, int config) {
		return false;
	}

	@Override
	public boolean resetSides() {
		for(FaceWrapper fw : faces.values()){
			fw.reset();
		}
		return true;
	}

	@Override
	public int getNumConfig(int side) {
		return getFaceForSide(side).count();
	}
	
	private FaceWrapper getFaceForSide(int side){
		return faces.get(faceMap[side]);
	}
	
	@Override
	public FaceColor getColorForSide(int side){
		if(side < 0 || side > 5) return FaceColor.VOID;
		return getFaceForSide(side).getColor();
	}
	
	@Override
	public FaceColor getColorForDirection(ForgeDirection dir){
		return getColorForSide(dir.ordinal());
	}
	
	@Override
	public IIcon getTexture(int side, int pass) {
		return IconRegistry.blockIcons.get(TextureReference.MACHINE_SIDE_I);
	}
	
	// ##########################################################
	// Face Wrapper
	
	private static class FaceWrapper{
		private final FaceColor[] faces;
		private int face;
		
		// NBT ****************************************
		public FaceWrapper(int face, int ... faces){
			this.faces = new FaceColor[faces.length];
			this.face = face;
			for(int i = 0; i < faces.length; i++){
				this.faces[i] = FaceColor.values()[i];
			}
		}
		
		public int getFace(){return face;}
		
		public int[] getColors(){
			int[] faces = new int[this.faces.length];
			for(int i = 0; i < faces.length; i++){
				faces[i] = this.faces[i].ordinal();
			}
			return faces;
		}
		// End NBT ************************************
		
		public FaceWrapper(FaceColor ... faces){
			this.faces = faces;
			this.face = 0;
		}
		
		public FaceColor getColor(){
			return faces[face];
		}
		
		public int count(){
			return faces.length;
		}
		
		public void increase(){
			face++;
			if(face == count()){
				face -= count();
			}
		}
		
		public void decrease(){
			face--;
			if(face < 0){
				face += count();
			}
		}
		
		public void reset(){
			face = 0;
		}
	}

	// ##########################################################
	// Nice little NBT handling
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagCompound facingNBT = nbt.getCompoundTag("facing");
		facing = ForgeDirection.getOrientation(nbt.getInteger("facing"));
		faces = new EnumMap(Face.class);
		for(int i = 0; i < faceMap.length; i++){
			int f = nbt.getInteger("face_" + i);
			Face face = Face.values()[i];
			faceMap[i] = face;
			NBTTagCompound wrapper = nbt.getCompoundTag("fw_" + f);
			int fc = wrapper.getInteger("face");
			int[] c = wrapper.getIntArray("colors");
			faces.put(face, new FaceWrapper(fc, c));
		}
		NBTTagCompound components = nbt.getCompoundTag("components");
		int size = components.getInteger("size");
		for(int i = 0; i < size; i++){
			allComponents.get(i).readFromNBT(components.getCompoundTag("comp_" + i));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagCompound facingNBT = new NBTTagCompound();
		facingNBT.setInteger("facing", facing.ordinal());
		for(int i = 0; i < faceMap.length; i++){
			Face face = faceMap[i];
			facingNBT.setInteger("face_" + i, face.ordinal());
			FaceWrapper fw = faces.get(face);
			NBTTagCompound wrapper = new NBTTagCompound();
			wrapper.setInteger("face", fw.getFace());
			wrapper.setIntArray("colors", fw.getColors());
			facingNBT.setTag("fw_" + face.ordinal(), wrapper);
		}
		nbt.setTag("facing", facingNBT);
		NBTTagCompound components = new NBTTagCompound();
		for(int i = 0; i < allComponents.size(); i++){
			components.setTag("comp_" + i, allComponents.get(i).writeToNBT(new NBTTagCompound()));
		}
		components.setInteger("size", allComponents.size());
		nbt.setTag("components", components);
	}
	
	// ##########################################################
	// COMPONENT: ITankComponent
	
	private ArrayList<ITankComponent> getTanks(){
		return (ArrayList<ITankComponent>) sidedComponents.get(ITankComponent.class);
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		for(ITankComponent tank : getTanks()){
			if(tank.canInputOnSide(from) && tank.canFill(resource.getFluid())){
				return tank.fill(resource, doFill);
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		for(ITankComponent tank : getTanks()){
			if(tank.canOutputOnSide(from) && tank.canDrain()){
				return tank.drain(maxDrain, doDrain);
			}
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		for(ITankComponent tank : getTanks()){
			if(tank.canInputOnSide(from)) return tank.canFill(fluid);
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		for(ITankComponent tank : getTanks()){
			if(tank.canOutputOnSide(from)) return tank.canDrain();
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		LinkedList<FluidTankInfo> infos = new LinkedList();
		for(ITankComponent tank : getTanks()){
			if(tank.canInputOnSide(from) || tank.canOutputOnSide(from)){
				infos.add(tank.getInfo());
			}
		}
		return infos.toArray(new FluidTankInfo[]{});
	}
}