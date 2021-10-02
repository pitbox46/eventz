package github.pitbox46.eventz.data;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.EventzScriptException;
import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.contestant.EventContestant;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.*;

public class Condition {
    public final String trigger;
    public final String startMethod;
    public final String triggerMethod;
    public JSObject startObject;
    public JSObject globalData = null;
    public IRecipe<?> recipe = null;
    public Map<EventContestant, JSObject> contestantData = new HashMap<>();
    public JSObject defaultObject;
    public BlockPos genericPos;
    public long endTime = 0;

    public Condition(String trigger, String startMethod, String triggerMethod) {
        this.trigger = trigger;
        this.startMethod = startMethod;
        this.triggerMethod = triggerMethod;
        defaultObject = Eventz.DEFAULT_OBJECT_SUPPLIER.get();
    }

    public void startScript() {
        if(!startMethod.isEmpty()) {
            String[] scriptFunctionPair = startMethod.split("#");
            if(scriptFunctionPair.length != 2)
                throw new RuntimeException(String.format("Start function for condition %s is not in the form \"scriptName.js#functionName\"", trigger));
            CompiledScript script = EventRegistration.SCRIPTS.get(scriptFunctionPair[0]);
            try {
                script.eval();
                Object returnValue = ((Invocable) script.getEngine()).invokeFunction(scriptFunctionPair[1]);
                if (returnValue instanceof JSObject) {
                    startObject = (JSObject) returnValue;
                    defaultObject.setMember("start_data", startObject);
                }
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if(startObject != null){
            if(startObject.hasMember("timer") && startObject.getMember("timer") instanceof Integer) {
                endTime = System.currentTimeMillis() + (Integer) startObject.getMember("timer");
            }
            //Todo make this more moddable
            if(trigger.equals("random_craft")) {
                recipe = recipeFromIngredients(IRecipeType.CRAFTING, startObject);
                ServerEvents.sendGlobalMsg(new StringTextComponent("Random craft goal: " + recipe.getRecipeOutput()));
            }
            else if(trigger.equals("random_smelt")) {
                recipe = recipeFromIngredients(IRecipeType.SMELTING, startObject);
                //Todo make these translation text components
                ServerEvents.sendGlobalMsg(new StringTextComponent("Random smelt goal: " + recipe.getRecipeOutput()));
            }
            else if(trigger.equals("locked_chest")) {
                if(startObject.hasMember("locked_loot")) {
                    List<ServerWorld> worlds = new ArrayList<>();
                    Eventz.getServer().getWorlds().forEach(worlds::add);
                    ServerWorld world = worlds.get(Eventz.RANDOM.nextInt(worlds.size()));
                    BlockPos pos = new BlockPos(Eventz.RANDOM.nextInt(20000) - 10000, 5 + Eventz.RANDOM.nextInt(250), Eventz.RANDOM.nextInt(20000) - 10000);

                    while(!world.getBlockState(pos).isAir(world, pos)) {
                        if(pos.getY() <= 120) {
                            pos.up();
                        }
                        else if(pos.getX() <= 9000){
                            pos.east();
                        }
                    }
                    genericPos = pos;
                    world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 11);
                    ChestTileEntity chestTile = (ChestTileEntity) world.getTileEntity(pos);

                    JSObject loot = (JSObject) startObject.getMember("locked_loot");
                    if(loot.isArray()) {
                        for(int i = 0; loot.hasSlot(i); i++) {
                            JSObject jsItemStack = (JSObject) loot.getSlot(i);
                            ItemStack stack = ItemStack.EMPTY;
                            try {
                            if(jsItemStack.hasMember("nbt"))
                                stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) jsItemStack.getMember("item"))), (Integer) jsItemStack.getMember("count"), JsonToNBT.getTagFromJson((String) jsItemStack.getMember("nbt")));
                            else
                                stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) jsItemStack.getMember("item"))), (Integer) jsItemStack.getMember("count"));
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            chestTile.setInventorySlotContents(i, stack);
                        }
                    }
                    ServerEvents.sendGlobalMsg(new StringTextComponent(String.format("A loot chest has spawned at coordinates: (%s, %s, %s)", pos.getX(), pos.getY(), pos.getZ())));
                }
            }
        }
    }

    public JSObject trigger(List<Object> params) throws EventzScriptException {
        if(!triggerMethod.isEmpty()) {
            String[] triggerFunctionPair = triggerMethod.split("#");
            if(triggerFunctionPair.length != 2)
                throw new RuntimeException(String.format("Start function for trigger %s is not in the form \"scriptName.js#functionName\"", trigger));
            CompiledScript script = EventRegistration.SCRIPTS.get(triggerFunctionPair[0]);
            if(script == null) {
                throw new EventzScriptException("Could not find script file: " + triggerFunctionPair[0]);
            }
            try {
                Object returnValue = ((Invocable) script.getEngine()).invokeFunction(triggerFunctionPair[1], params.toArray());
                if(returnValue instanceof JSObject)
                    return (JSObject) returnValue;
            } catch (ScriptException | NoSuchMethodException e) {
                throw new EventzScriptException(e.getMessage(), e);
            }
        }
        return null;
    }

    public void timesUp() throws EventzScriptException {
        ((ScriptObjectMirror) globalData).put("time_up", true);
        JSObject returnValue = trigger(Arrays.asList(null, globalData));
        if (returnValue.getMember("global_data") instanceof JSObject) {
            JSObject globalDataFinal = (JSObject) returnValue.getMember("global_data");
            //Todo This
            if(globalDataFinal.hasMember("winners")) {
                JSObject winnerJSArray = (JSObject) globalDataFinal.getMember("winners");
                ArrayList<EventContestant> winners = new ArrayList<>();
                int i = 0;
                while(winnerJSArray.hasSlot(i)) {
                    winners.add(Eventz.activeEvent.getContestant((String) winnerJSArray.getSlot(i)));
                }
                Eventz.activeEvent.finishEvent(winners.toArray(new EventContestant[0]));
            }
        }
    }

    public static Condition readCondition(JsonObject jsonObject) {
        try {
            String trigger = jsonObject.get("trigger").getAsString();
            String startMethod = jsonObject.get("start_method").getAsString();
            String triggerMethod = jsonObject.get("trigger_method").getAsString();
            return new Condition(trigger, startMethod, triggerMethod);
        } catch (NullPointerException | UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <C extends IInventory, R extends IRecipe<C>> R recipeFromIngredients(IRecipeType<R> recipeType, JSObject jsObject) {
        JSObject potentialIngredients = (JSObject) jsObject.getMember("potential_ingredients");

        List<R> recipeList = new ArrayList<>();
        for(R recipe: Eventz.getServer().getRecipeManager().getRecipesForType(recipeType)) {
            boolean flag = false;
            for(Ingredient ingredient: recipe.getIngredients()) {
                for(ItemStack itemStack: ingredient.getMatchingStacks()) {
                    int i = 0;
                    while(potentialIngredients.hasSlot(i)) {
                        if(potentialIngredients.getSlot(i).equals(itemStack.getItem().getRegistryName().toString())) {
                            flag = true;
                            break;
                            //Todo this could be written cleaner and more efficiently
                        }
                        i++;
                    }
                    if(flag) break;
                }
                if(flag) break;
            }
            if(flag) recipeList.add(recipe);
        }
        return recipeList.get(Eventz.RANDOM.nextInt(recipeList.size()));
    }
}
