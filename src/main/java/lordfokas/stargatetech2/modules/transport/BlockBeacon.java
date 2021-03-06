package lordfokas.stargatetech2.modules.transport;

import java.util.List;

import lordfokas.stargatetech2.ZZ_THRASH.BaseBlockContainer__OLD_AND_FLAWED;
import lordfokas.stargatetech2.ZZ_THRASH.BaseTileEntity__OLD_AND_FLAWED;
import lordfokas.stargatetech2.lib.util.NoDefaultTexture;
import lordfokas.stargatetech2.reference.BlockReference;
import lordfokas.stargatetech2.reference.TextureReference;
import lordfokas.stargatetech2.util.IconRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

@NoDefaultTexture
public class BlockBeacon extends BaseBlockContainer__OLD_AND_FLAWED{
	public static final int META_TRANSCEIVER = 0;
	public static final int META_ANTENNA = 1;
	public static final int META_CONSOLE = 2;
	public static final int META_MATTERGRID = 3;
	
	public BlockBeacon() {
		super(BlockReference.BEACON);
		setRenderer(RenderBeacon.instance());
		setIsAbstractBusBlock();
	}
	
	@Override
	public IIcon getBaseIcon(int side, int meta){
		if(meta == META_ANTENNA) return IconRegistry.blockIcons.get(TextureReference.BEACON_ANTENNA);
		if(meta == META_TRANSCEIVER)
			if(side == 0) return IconRegistry.blockIcons.get(TextureReference.MACHINE_BOTTOM);
			else if(side == 1) return IconRegistry.blockIcons.get(TextureReference.BEACON_TRANSCEIVER_T);
			else return IconRegistry.blockIcons.get(TextureReference.BEACON_TRANSCEIVER);
		if(meta == META_CONSOLE)
			if(side == 0) return IconRegistry.blockIcons.get(TextureReference.MACHINE_BOTTOM);
			else if(side == 1) return IconRegistry.blockIcons.get(TextureReference.MACHINE_TOP);
			else return IconRegistry.blockIcons.get(TextureReference.BEACON_CONSOLE);
		if(meta == META_MATTERGRID)
			if(side == 0) return IconRegistry.blockIcons.get(TextureReference.MACHINE_BOTTOM);
			else if(side == 1) return IconRegistry.blockIcons.get(TextureReference.BEACON_MATTERGRID);
			else return IconRegistry.blockIcons.get(TextureReference.MACHINE_SIDE);
		return blockIcon;
	}
	
	@Override
	public int damageDropped(int meta){
		return meta;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z){
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	}
	
	@Override
	public boolean isOpaqueCube(){
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock(){
		return false;
	}
	
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, META_TRANSCEIVER));
		list.add(new ItemStack(item, 1, META_ANTENNA));
		list.add(new ItemStack(item, 1, META_CONSOLE));
		list.add(new ItemStack(item, 1, META_MATTERGRID));
	}
	
	@Override
	protected void registerBlock(){
		GameRegistry.registerBlock(this, ItemBlockBeacon.class, getUnlocalizedName());
	}
	
	@Override
	protected BaseTileEntity__OLD_AND_FLAWED createTileEntity(int metadata){
		if(metadata == META_TRANSCEIVER){
			return new TileBeaconTransceiver();
		}
		if(metadata == META_CONSOLE){
			return new TileBeaconConsole();
		}
		if(metadata == META_MATTERGRID){
			return new TileBeaconMatterGrid();
		}
		return null;
	}

}
