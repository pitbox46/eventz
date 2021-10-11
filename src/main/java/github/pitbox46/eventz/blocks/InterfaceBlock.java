package github.pitbox46.eventz.blocks;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.Registration;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class InterfaceBlock extends Block {
    public InterfaceBlock() {
        super(AbstractBlock.Properties
                .create(Material.IRON, MaterialColor.IRON)
                .setRequiresTool()
                .hardnessAndResistance(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .notSolid()
        );
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote()) {
            if (!(worldIn.getTileEntity(pos) instanceof InterfaceTileEntity))
                return ActionResultType.FAIL;
            InterfaceTileEntity te = (InterfaceTileEntity) worldIn.getTileEntity(pos);
            assert te != null;
            if (te.getContestant() == null && Eventz.activeEvent != null) {
                te.setContestant(Eventz.activeEvent.getContestant((ServerPlayerEntity) player));
                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.FAIL;
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return Registration.INTERFACE_TILE.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
