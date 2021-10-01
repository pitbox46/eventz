package github.pitbox46.eventz.blocks;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.Registration;
import github.pitbox46.eventz.data.Condition;
import github.pitbox46.eventz.data.contestant.EventContestant;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InterfaceTileEntity extends TileEntity implements ITickableTileEntity {
    protected Energy energyStorage = createEnergy();
    protected FluidTank fluidTank = createTank();
    protected ItemStackHandler itemHandler = (ItemStackHandler) createHandler();

    protected LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    protected LazyOptional<IFluidHandler> fluid = LazyOptional.of(() -> fluidTank);
    protected LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    protected EventContestant contestant;

    public InterfaceTileEntity() {
        super(Registration.INTERFACE_TILE.get());
    }

    @Override
    public void tick() {
        if(world.isRemote()) return;
        //TODO Make this happen less
        if(Eventz.activeEvent != null && contestant != null && contestant.hasUnfilledCondition("interface_block")) {
            ItemStack item = ItemStack.EMPTY;
            if(!itemHandler.getStackInSlot(0).isEmpty()) {
                item = itemHandler.getStackInSlot(0);
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            }
            //JSObject previousValues, JSObject globalData, String contestantName, String fluid, int fluidAmount, int energy, String item, int itemAmount
            Eventz.activeEvent.trigger(contestant, "interface_block", fluidTank.getFluid().getTranslationKey(), fluidTank.getFluid().getAmount(), energyStorage.getEnergyStored(), item.isEmpty() ? "NULL" : item.getItem().getRegistryName().toString(), item.getCount());
        } else {
            energyStorage.setEnergy(0);
            fluidTank.setFluid(FluidStack.EMPTY);
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public EventContestant getContestant() {
        return contestant;
    }

    public void setContestant(EventContestant contestant) {
        this.contestant = contestant;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        energyStorage.deserializeNBT(tag.getCompound("energy"));
        fluidTank.readFromNBT(tag.getCompound("fluid"));
        itemHandler.deserializeNBT(tag.getCompound("inv"));
        super.read(state, tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("energy", energyStorage.serializeNBT());
        tag.put("fluid", fluidTank.writeToNBT(new CompoundNBT()));
        tag.put("inv", itemHandler.serializeNBT());
        return super.write(tag);
    }

    private Energy createEnergy() {
        return new Energy() {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }

            @Override
            public boolean canExtract() {
                return false;
            }
        };
    }

    protected FluidTank createTank() {
        return new FluidTank(Integer.MAX_VALUE) {
            @Override
            protected void onContentsChanged() {
                markDirty();
            }
        }.setValidator(fluidStack -> {
            if(contestant == null) return false;
            if(contestant.conditions.containsKey("interface_block")) {
                Pair<Condition, Boolean> entry = contestant.conditions.get("interface_block");
                Condition condition = entry.getLeft();
                if (!entry.getRight() && condition.startObject != null && condition.startObject.hasMember("fluid_input")) {
                    Object object = condition.startObject.getMember("fluid_input");
                    return object instanceof String && object.equals(fluidStack.getFluid().getRegistryName().toString());
                }
            }
            return false;
        });
    }

    protected IItemHandler createHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(contestant == null) return false;
                if(contestant.conditions.containsKey("interface_block")) {
                    Pair<Condition, Boolean> entry = contestant.conditions.get("interface_block");
                    Condition condition = entry.getKey();
                    if (!entry.getRight() && condition.startObject != null && condition.startObject.hasMember("item_input")) {
                        Object object = condition.startObject.getMember("item_input");
                        return object instanceof String && object.equals(stack.getItem().getRegistryName().toString());
                    }
                }
                return false;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energy.cast();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluid.cast();
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }
}
