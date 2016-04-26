package lordfokas.stargatetech2.lib.block;

import lordfokas.stargatetech2.api.bus.BusEvent;
import lordfokas.stargatetech2.util.MaterialNaquadah;
import lordfokas.stargatetech2.util.StargateTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BaseBlock extends Block{
	private BlockRenderType renderType = BlockRenderType.STANDARD;
	private boolean isAbstractBus = false;
	private String unlocalized;
	
	public BaseBlock(String uName){
		this(uName, false, true);
	}
	
	public BaseBlock(String uName, boolean breakable, boolean requiresTool) {
		this(uName, breakable, requiresTool ? MaterialNaquadah.unbreakable : MaterialNaquadah.breakable);
	}
	
	public BaseBlock(String uName, boolean breakable, Material material){
		super(material);
		unlocalized = uName;
		if(!breakable){
			setBlockUnbreakable();
			setResistance(20000000F);
		}
		setCreativeTab(StargateTab.instance);
		registerBlock();
	}
	
	public final void setRenderType(BlockRenderType renderType){
		this.renderType = renderType;
	}
	
	@Override
	public final int getRenderType() {
		return renderType.value;
	}
	
	// MEH
	@Override
	public String getUnlocalizedName(){
		return "block." + unlocalized;
	}
	
	@Override
	public int getMobilityFlag(){
		return 2;
	}
	
	protected void setIsAbstractBusBlock(){
		isAbstractBus = true;
	}
	
	protected void registerBlock(){
		GameRegistry.registerBlock(this, getUnlocalizedName());
	}
	
	@Override
	public void onBlockAdded(World w, BlockPos pos, IBlockState state) {
		super.onBlockAdded(w, pos, state);
		if(isAbstractBus) MinecraftForge.EVENT_BUS.post(new BusEvent.AddToNetwork(w, pos));
	}
	
	@Override
	public void breakBlock(World w, BlockPos pos, IBlockState state) {
		super.breakBlock(w, pos, state);
		if(isAbstractBus) MinecraftForge.EVENT_BUS.post(new BusEvent.RemoveFromNetwork(w, pos));
	}
	
	public void dropSelf(World w, BlockPos pos){
		w.setBlockToAir(pos);
		dropItemStack(w, pos, new ItemStack(this));
	}
	
	public void dropItemStack(World w, BlockPos pos, ItemStack stack){
		dropStackAt(w, ((double)pos.getX())+0.5D, ((double)pos.getY())+0.5D, ((double)pos.getZ())+0.5D, stack);
	}
	
	public void dropItemStack(World w, EntityPlayer p, ItemStack stack){
		dropStackAt(w, p.posX, p.posY, p.posZ, stack);
	}
	
	private void dropStackAt(World w, double x, double y, double z, ItemStack stack){
		if(w.isRemote) return;
		w.spawnEntityInWorld(new EntityItem(w, x, y, z, stack));
	}
}