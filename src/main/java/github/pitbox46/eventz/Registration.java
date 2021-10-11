package github.pitbox46.eventz;

import github.pitbox46.eventz.blocks.InterfaceBlock;
import github.pitbox46.eventz.blocks.InterfaceTileEntity;
import github.pitbox46.monetamoney.MonetaMoney;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "eventz");
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "eventz");
    public static final RegistryObject<InterfaceBlock> INTERFACE_BLOCK = BLOCKS.register("interface", InterfaceBlock::new);
    public static final RegistryObject<Item> INTERFACE_ITEM = ITEMS.register("interface", () -> new BlockItem(INTERFACE_BLOCK.get(), new Item.Properties().group(MonetaMoney.MOD_TAB)));
    static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "eventz");

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
    }
    public static final RegistryObject<TileEntityType<InterfaceTileEntity>> INTERFACE_TILE = TILE_ENTITIES.register("interface", () -> TileEntityType.Builder.create(InterfaceTileEntity::new, INTERFACE_BLOCK.get()).build(null));
}
